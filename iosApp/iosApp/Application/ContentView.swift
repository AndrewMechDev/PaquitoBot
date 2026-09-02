import SwiftUI
import Observation

struct ContentView: View {
    @State private var app = AppViewModel()

    var body: some View {
        Group {
            if !app.isOnboardingComplete {
                switch app.onboardingStep {
                case .welcome:
                    WelcomeView(title: "PaquitoBot", subtitle: "Tu copiloto académico", buttonTitle: "Empezar", onContinue: app.completeWelcome)
                case .notifications:
                    OnboardingNotificationsView(onContinue: app.completeNotificationPreferences)
                case .tour:
                    OnboardingTourView(onContinue: app.completeTour)
                }
            } else if !app.isAuthenticated {
                AuthGateView(isSigningIn: app.isSigningIn, errorMessage: app.authError) {
                    Task { await app.signIn() }
                }
            } else if app.shouldShowAcademicGate {
                AcademicConnectView(
                    isConnecting: app.isConnectingAcademic,
                    errorMessage: app.academicGateError,
                    onConnect: { key in Task { await app.connectAcademic(with: key) } },
                    onSkip: app.skipAcademic
                )
            } else {
                switch app.rootScreen {
                case .tabs:
                    MainTabsView(app: app)
                case .chat:
                    ChatView(viewModel: app.chatViewModel, onBack: app.closeSecondaryScreen)
                case .courseDetail:
                    if let courseDetailViewModel = app.courseDetailViewModel {
                        CourseDetailView(viewModel: courseDetailViewModel, onBack: app.closeSecondaryScreen)
                    } else {
                        MainTabsView(app: app)
                    }
                case .notifications:
                    NotificationsInboxView(onBack: app.closeSecondaryScreen)
                }
            }
        }
        .preferredColorScheme(.light)
    }
}

@MainActor
private struct MainTabsView: View {
    @Bindable var app: AppViewModel

    var body: some View {
        Group {
            switch app.selectedTab {
            case .inicio:
                HomeView(
                    selectedTab: $app.selectedTab,
                    viewModel: app.homeViewModel,
                    onChatbot: app.openChat,
                    onNotifications: app.openNotifications,
                    onTabSelected: { app.selectedTab = $0 }
                )
            case .cursos:
                CoursesView(
                    selectedTab: $app.selectedTab,
                    viewModel: app.coursesViewModel,
                    onChatbot: app.openChat,
                    onCourseSelected: app.openCourse,
                    onTabSelected: { app.selectedTab = $0 }
                )
            case .horarios:
                ScheduleView(
                    selectedTab: $app.selectedTab,
                    viewModel: app.scheduleViewModel,
                    onChatbot: app.openChat,
                    onTabSelected: { app.selectedTab = $0 }
                )
            }
        }
        .task {
            guard app.tokenStore.canvasMockKey != nil else { return }
            app.reloadAcademicScreens()
        }
    }
}
