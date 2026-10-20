import ClerkKit
import ClerkKitUI
import SwiftUI

@MainActor
struct ClerkAuthGateView: View {
    @Environment(Clerk.self) private var clerk
    @ObservedObject var app: AppViewModel

    var body: some View {
        Group {
            if app.isUnauthorized {
                AuthGateView(
                    isSigningIn: app.isSigningIn,
                    isUnauthorized: true,
                    errorMessage: app.authError,
                    onSignIn: app.signIn,
                    onSignOut: app.signOut
                )
            } else if clerk.user == nil {
                AuthView(mode: .signInOrUp, isDismissible: false) {
                    app.synchronizeSession()
                }
            } else {
                ProgressView("Validando tu cuenta…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(PaquitoColors.background)
            }
        }
        .task {
            app.synchronizeSession()
        }
        .onChange(of: clerk.user) { _, _ in
            app.synchronizeSession()
        }
    }
}
