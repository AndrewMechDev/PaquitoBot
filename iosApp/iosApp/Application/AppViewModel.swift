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
    @Published var notifications: [NotificationItem]
    @Published private(set) var notificationSetup = NotificationPreferenceStore.load()
    @Published private(set) var isRequestingNotificationPermission = false
    @Published private(set) var notificationPermissionState: NotificationAuthorizationState = .notDetermined

    let authService: AuthService
    let chatViewModel: ChatViewModel
    private let notificationPermissionService: NotificationPermissionService
    private let notificationScheduleService: NotificationScheduleService

    init(
        authService: AuthService? = nil,
        chatService: ChatService? = nil,
        notificationPermissionService: NotificationPermissionService = SystemNotificationPermissionService(),
        notificationScheduleService: NotificationScheduleService? = nil
    ) {
        self.authService = authService ?? AuthServiceFactory.makeDefault()
        self.notificationPermissionService = notificationPermissionService
        self.notificationScheduleService = notificationScheduleService ?? LocalNotificationScheduleService()
        chatViewModel = ChatViewModel(service: chatService ?? ChatServiceFactory.makeDefault())
        notifications = NotificationInboxStore.load()
        if ProcessInfo.processInfo.arguments.contains("-PaquitoDemo") {
            phase = .main
        } else if UserDefaults.standard.bool(forKey: "paquitobot.onboardingComplete") {
            phase = .auth
        } else {
            phase = .onboarding(.intro)
        }

        Task { [permissionService = self.notificationPermissionService, scheduleService = self.notificationScheduleService] in
            notificationPermissionState = await permissionService.authorizationState()
            await scheduleService.synchronize(
                setup: notificationSetup,
                tasks: AcademicDemoStore.calendarTasks
            )
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
        case .intro:
            phase = .onboarding(.welcome)
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
            Task { [weak self] in
                guard let self else { return }
                await notificationScheduleService.synchronize(setup: setup, tasks: AcademicDemoStore.calendarTasks)
                advanceOnboarding()
            }
            return
        }

        isRequestingNotificationPermission = true
        Task { [weak self] in
            guard let self else { return }
            _ = await notificationPermissionService.requestAuthorization()
            notificationPermissionState = await notificationPermissionService.authorizationState()
            await notificationScheduleService.synchronize(
                setup: setup,
                tasks: AcademicDemoStore.calendarTasks
            )
            isRequestingNotificationPermission = false
            advanceOnboarding()
        }
    }

    func requestNotificationPermission() {
        guard !isRequestingNotificationPermission else { return }
        isRequestingNotificationPermission = true
        Task { [weak self] in
            guard let self else { return }
            _ = await notificationPermissionService.requestAuthorization()
            notificationPermissionState = await notificationPermissionService.authorizationState()
            await notificationScheduleService.synchronize(
                setup: notificationSetup,
                tasks: AcademicDemoStore.calendarTasks
            )
            isRequestingNotificationPermission = false
        }
    }

    func openNotificationSettings() {
        notificationPermissionService.openSettings()
    }

    func sendTestNotification() {
        Task { [weak self] in
            guard let self else { return }
            _ = await notificationPermissionService.sendTestNotification()
            notificationPermissionState = await notificationPermissionService.authorizationState()
        }
    }

    func updateNotificationSetup(_ setup: NotificationSetup) {
        notificationSetup = setup
        NotificationPreferenceStore.save(setup)
        Task { [notificationScheduleService] in
            await notificationScheduleService.synchronize(
                setup: setup,
                tasks: AcademicDemoStore.calendarTasks
            )
        }
    }

    func updateNotifications(_ items: [NotificationItem]) {
        notifications = items
        NotificationInboxStore.save(items)
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
        AcademicDemoStore.courses.first { $0.id == id }
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
                        data: AcademicDemoStore.home,
                        unreadNotifications: app.notifications.count,
                        onNotifications: app.openNotifications,
                        onEvents: app.openEvents,
                        onSignOut: app.signOut
                    )
                case .courses:
                    CoursesView(courses: AcademicDemoStore.courses, onCourseSelected: app.openCourse)
                case .schedule:
                    ScheduleView(data: AcademicDemoStore.schedule, onCalendar: app.openCalendar)
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
