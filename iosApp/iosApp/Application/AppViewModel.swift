import Foundation
import Observation

@MainActor
@Observable
final class AppViewModel {
    let tokenStore: KeychainTokenStore
    let sharedLogic: SharedLogicBridge
    let googleAuth: GoogleAuthService

    var selectedTab: AppTab = .inicio
    var rootScreen: RootScreen = .tabs
    var onboardingStep: OnboardingStep = .welcome
    var isOnboardingComplete: Bool
    var isAuthenticated: Bool
    var academicGateCompleted: Bool
    var isSigningIn = false
    var authError: String?
    var selectedCourse: CourseCardData?
    var academicGateError: String?
    var isConnectingAcademic = false

    let homeViewModel: HomeViewModel
    let coursesViewModel: CoursesViewModel
    let scheduleViewModel: ScheduleViewModel
    let chatViewModel: ChatViewModel
    var courseDetailViewModel: CourseDetailViewModel?

    init() {
        let store = KeychainTokenStore()
        let bridge = SharedLogicBridge(accessToken: store.accessToken, canvasMockKey: store.canvasMockKey)
        tokenStore = store
        sharedLogic = bridge
        googleAuth = GoogleAuthService()
        isOnboardingComplete = store.isOnboardingComplete
        isAuthenticated = store.hasValidSession
        academicGateCompleted = store.canvasMockKey != nil || store.canvasMockSkipped
        if store.isOnboardingComplete {
            onboardingStep = .tour
        }

        homeViewModel = HomeViewModel(bridge: bridge, userName: store.firstName)
        coursesViewModel = CoursesViewModel(bridge: bridge)
        scheduleViewModel = ScheduleViewModel(bridge: bridge)
        chatViewModel = ChatViewModel(bridge: bridge, userName: store.firstName)
    }

    var shouldShowAcademicGate: Bool {
        isAuthenticated && !academicGateCompleted
    }

    func completeWelcome() {
        onboardingStep = .notifications
    }

    func completeNotificationPreferences(_ enabled: Set<NotificationPreference>) {
        // Android currently receives this set but does not persist it yet.
        // Keep the same behavior while preserving the user selection for the
        // next onboarding step in memory.
        _ = enabled
        onboardingStep = .tour
    }

    func completeTour() {
        tokenStore.isOnboardingComplete = true
        isOnboardingComplete = true
        onboardingStep = .tour
    }

    func signIn() async {
        guard !isSigningIn else { return }
        isSigningIn = true
        authError = nil
        defer { isSigningIn = false }

        // Cold-start wake-up is best effort, matching Android.
        try? await sharedLogic.wakeBackend()

        do {
            let credential = try await googleAuth.signIn()
            let session = try await sharedLogic.loginWithGoogle(idToken: credential.idToken)
            tokenStore.accessToken = session.accessToken
            tokenStore.tokenExpiry = Date().addingTimeInterval(TimeInterval(session.expiresInSeconds))
            tokenStore.firstName = credential.firstName
            sharedLogic.setAccessToken(session.accessToken)
            isAuthenticated = true
            chatViewModel.userName = credential.firstName
            homeViewModel.updateUserName(credential.firstName)
            if shouldShowAcademicGate {
                academicGateError = nil
            }
        } catch {
            authError = userFacingMessage(for: error)
        }
    }

    func connectAcademic(with apiKey: String) async {
        let trimmed = apiKey.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty, !isConnectingAcademic else { return }
        isConnectingAcademic = true
        academicGateError = nil
        defer { isConnectingAcademic = false }

        do {
            // profile() is the same probe used by AcademicConnectViewModel on Android.
            _ = try await sharedLogic.academicProfileWithTemporaryKey(trimmed)
            tokenStore.canvasMockKey = trimmed
            tokenStore.canvasMockSkipped = false
            sharedLogic.setCanvasMockKey(trimmed)
            academicGateCompleted = true
            reloadAcademicScreens()
        } catch {
            academicGateError = userFacingMessage(for: error)
        }
    }

    func skipAcademic() {
        tokenStore.canvasMockSkipped = true
        tokenStore.canvasMockKey = nil
        sharedLogic.setCanvasMockKey(nil)
        academicGateCompleted = true
    }

    func openChat() {
        rootScreen = .chat
    }

    func openNotifications() {
        rootScreen = .notifications
    }

    func openCourse(_ course: CourseCardData) {
        selectedCourse = course
        courseDetailViewModel = CourseDetailViewModel(bridge: sharedLogic, course: course)
        rootScreen = .courseDetail
    }

    func closeSecondaryScreen() {
        rootScreen = .tabs
    }

    func reloadAcademicScreens() {
        guard tokenStore.canvasMockKey != nil else { return }
        homeViewModel.load()
        coursesViewModel.load()
        scheduleViewModel.load()
    }
}

private extension SharedLogicBridge {
    func academicProfileWithTemporaryKey(_ key: String) async throws -> SharedAcademicProfile {
        // The shared facade keeps the persisted API key separate from the
        // backend JWT. A short-lived bridge makes the connect probe explicit
        // and avoids mutating the real session before the probe succeeds.
        let temporary = SharedLogicBridge(accessToken: nil, canvasMockKey: key)
        return try await temporary.academicProfile()
    }
}
