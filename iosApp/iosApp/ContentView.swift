import SwiftUI

@MainActor
struct ContentView: View {
    @StateObject private var app: AppViewModel
    @State private var showPublicEnrollmentGuide = false

    init() {
        _app = StateObject(wrappedValue: AppViewModel())
    }

    init(app: AppViewModel) {
        _app = StateObject(wrappedValue: app)
    }

    var body: some View {
        Group {
            caseOnboardingAndApp
        }
        .sheet(isPresented: $showPublicEnrollmentGuide) {
            NavigationStack {
                EnrollmentGuideView(onClose: { showPublicEnrollmentGuide = false })
            }
        }
        .preferredColorScheme(.light)
    }

    @ViewBuilder
    private var caseOnboardingAndApp: some View {
        switch app.phase {
        case .onboarding(.intro):
            LaunchIntroView(onFinished: app.advanceOnboarding)
        case .onboarding(.welcome):
            WelcomeView(
                onContinue: app.advanceOnboarding,
                onEnrollmentGuide: { showPublicEnrollmentGuide = true }
            )
        case .onboarding(.notifications):
            OnboardingNotificationsView(
                isRequestingPermission: app.isRequestingNotificationPermission,
                initialSetup: app.notificationSetup
            ) { setup in
                app.completeNotificationSetup(setup)
            }
        case .onboarding(.tour):
            OnboardingTourView(onContinue: app.advanceOnboarding)
        case .auth:
            if app.authService is ClerkAuthService {
                ClerkAuthGateView(app: app)
            } else {
                AuthGateView(
                    isSigningIn: app.isSigningIn,
                    isUnauthorized: app.isUnauthorized,
                    errorMessage: app.authError,
                    onSignIn: app.signIn,
                    onSignOut: app.signOut
                )
            }
        case .main:
            NavigationStack(path: $app.path) {
                MainShellView(app: app)
                    .navigationDestination(for: AppRoute.self) { route in
                        switch route {
                        case .chat:
                            ChatView(viewModel: app.chatViewModel, userName: app.userName)
                        case .notifications:
                            NotificationsInboxView(
                                items: app.notifications,
                                setup: app.notificationSetup,
                                onSaveSetup: app.updateNotificationSetup
                            )
                        case .courseDetail(let id):
                            if let course = app.course(with: id) {
                                CourseDetailView(data: DemoAcademicData.detail(for: course))
                            } else {
                                ContentUnavailableView("Curso no encontrado", systemImage: "book.closed")
                            }
                        case .calendar:
                            CalendarSyncView()
                        case .procedures:
                            ProceduresView()
                        case .procedureDetail(let id):
                            ProcedureDetailView(procedure: DemoAcademicData.procedure(with: id) ?? DemoAcademicData.procedures[0])
                        case .enrollment:
                            EnrollmentGuideView()
                        case .events:
                            EventsView()
                        case .eventDetail(let id):
                            EventDetailView(event: DemoAcademicData.event(with: id) ?? DemoAcademicData.events[0])
                        }
                    }
                }
            }
    }
}

@MainActor
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(app: AppViewModel(authService: DemoAuthService()))
    }
}
