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
    @Published private(set) var notificationPreferences = NotificationPreferenceStore.load()
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
        phase = ProcessInfo.processInfo.arguments.contains("-PaquitoDemo")
            ? .main
            : .onboarding(.welcome)
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
            phase = .auth
        }
    }

    func completeNotificationSetup(_ preferences: Set<NotificationPreference>) {
        guard case .onboarding(.notifications) = phase, !isRequestingNotificationPermission else { return }
        notificationPreferences = preferences
        NotificationPreferenceStore.save(preferences)

        guard !preferences.isEmpty else {
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
                    ScheduleView(data: DemoAcademicData.schedule)
                }
            }
            .id(app.selectedTab)
            .transition(.opacity)
        }
        .animation(.easeOut(duration: 0.2), value: app.selectedTab)
    }
}
