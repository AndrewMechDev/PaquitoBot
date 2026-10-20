import SwiftUI

@MainActor
struct ContentView: View {
    @StateObject private var app: AppViewModel

    init() {
        _app = StateObject(wrappedValue: AppViewModel())
    }

    init(app: AppViewModel) {
        _app = StateObject(wrappedValue: app)
    }

    var body: some View {
        Group {
            switch app.phase {
            case .onboarding(.welcome):
                WelcomeView(onContinue: app.advanceOnboarding)
            case .onboarding(.notifications):
                OnboardingNotificationsView(isRequestingPermission: app.isRequestingNotificationPermission) { preferences in
                    app.completeNotificationSetup(preferences)
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
                                NotificationsInboxView(items: app.notifications)
                            case .courseDetail(let id):
                                if let course = app.course(with: id) {
                                    CourseDetailView(data: DemoAcademicData.detail(for: course))
                                } else {
                                    ContentUnavailableView("Curso no encontrado", systemImage: "book.closed")
                                }
                            }
                        }
                }
            }
        }
        .preferredColorScheme(.light)
    }
}

@MainActor
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(app: AppViewModel(authService: DemoAuthService()))
    }
}
