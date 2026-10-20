import Foundation
import SwiftUI

enum AppPhase: Equatable {
    case onboarding(OnboardingStep)
    case auth
    case main
}

@MainActor
final class AppViewModel: ObservableObject {
    @Published var phase: AppPhase
    @Published var selectedTab: AppTab = .home
    @Published var path: [AppRoute] = []
    @Published var userName = "estudiante"
    @Published var isSigningIn = false
    @Published var isSigningOut = false
    @Published var authError: String?
    @Published var isUnauthorized = false
    @Published var notifications = DemoAcademicData.notifications
    @Published private(set) var notificationSetup = NotificationPreferenceStore.load()
    @Published private(set) var isRequestingNotificationPermission = false

    let authService: AuthService
    let chatViewModel: ChatViewModel
    private let notificationPermissionService: NotificationPermissionService

    init(
        authService: AuthService? = nil,
        chatService: ChatService? = nil,
        notificationPermissionService: NotificationPermissionService = SystemNotificationPermissionService()
    ) {
        self.authService = authService ?? AuthServiceFactory.makeDefault()
        self.notificationPermissionService = notificationPermissionService
        chatViewModel = ChatViewModel(service: chatService ?? ChatServiceFactory.makeDefault())
        if ProcessInfo.processInfo.arguments.contains("-PaquitoDemo") {
            phase = .main
        } else if UserDefaults.standard.bool(forKey: "paquitobot.onboardingComplete") {
            phase = .auth
        } else {
            phase = .onboarding(.welcome)
        }
    }

    func synchronizeSession() {
        switch authService.currentSessionState {
        case .authorized(let user):
            userName = user.firstName
            isUnauthorized = false
            authError = nil
            phase = .main
        case .unauthorized:
            isUnauthorized = true
            phase = .auth
        case .signedOut:
            isUnauthorized = false
        }
    }

    func advanceOnboarding() {
        guard case let .onboarding(step) = phase else { return }
        switch step {
        case .welcome:
            phase = .onboarding(.notifications)
        case .notifications:
            phase = .onboarding(.tour)
        case .tour:
            UserDefaults.standard.set(true, forKey: "paquitobot.onboardingComplete")
            phase = .auth
        }
    }

    func completeNotificationSetup(_ setup: NotificationSetup) {
        guard case .onboarding(.notifications) = phase, !isRequestingNotificationPermission else { return }
        notificationSetup = setup
        NotificationPreferenceStore.save(setup)

        guard !setup.enabled.isEmpty else {
            advanceOnboarding()
            return
        }

        isRequestingNotificationPermission = true
        Task { [weak self] in
            guard let self else { return }
            _ = await notificationPermissionService.requestAuthorization()
            isRequestingNotificationPermission = false
            advanceOnboarding()
        }
    }

    func updateNotificationSetup(_ setup: NotificationSetup) {
        notificationSetup = setup
        NotificationPreferenceStore.save(setup)
    }

    func signIn() {
        guard !isSigningIn else { return }
        isSigningIn = true
        authError = nil
        isUnauthorized = false

        Task { [weak self] in
            guard let self else { return }
            do {
                let user = try await authService.signIn()
                userName = user.firstName
                phase = .main
            } catch {
                authError = error.localizedDescription
            }
            isSigningIn = false
        }
    }

    func signOut() {
        guard !isSigningOut else { return }
        isSigningOut = true
        authError = nil

        Task { [weak self] in
            guard let self else { return }
            do {
                try await authService.signOut()
                path.removeAll()
                selectedTab = .home
                userName = "estudiante"
                phase = .auth
            } catch {
                authError = error.localizedDescription
            }
            isSigningOut = false
        }
    }

    func openChat() {
        path.append(.chat)
    }

    func openNotifications() {
        path.append(.notifications)
    }

    func openCourse(_ course: CourseItem) {
        path.append(.courseDetail(course.id))
    }

    func openCalendar() { path.append(.calendar) }
    func openProcedures() { path.append(.procedures) }
    func openEnrollment() { path.append(.enrollment) }
    func openEvents() { path.append(.events) }

    func course(with id: Int) -> CourseItem? {
        DemoAcademicData.courses.first { $0.id == id }
    }
}

struct MainShellView: View {
    @ObservedObject var app: AppViewModel

    var body: some View {
        AppTabScaffold(selection: $app.selectedTab, onChat: app.openChat) {
            Group {
                switch app.selectedTab {
                case .home:
                    HomeView(
                        userName: app.userName,
                        data: DemoAcademicData.home,
                        unreadNotifications: app.notifications.count,
                        onNotifications: app.openNotifications,
                        onSignOut: app.signOut
                    )
                case .courses:
                    CoursesView(courses: DemoAcademicData.courses, onCourseSelected: app.openCourse)
                case .schedule:
                    ScheduleView(data: DemoAcademicData.schedule, onCalendar: app.openCalendar)
                case .profile:
                    ProfileView(
                        data: ProfileData(name: app.userName, email: app.authService.currentUser?.email ?? "estudiante@tecsup.edu.pe"),
                        isSigningOut: app.isSigningOut,
                        errorMessage: app.authError,
                        onProcedures: app.openProcedures,
                        onEnrollment: app.openEnrollment,
                        onEvents: app.openEvents,
                        onSignOut: app.signOut
                    )
                }
            }
            .id(app.selectedTab)
            .transition(.opacity)
        }
        .animation(.easeOut(duration: 0.2), value: app.selectedTab)
    }
}
