import Foundation
import UIKit

#if canImport(GoogleSignIn)
import GoogleSignIn
#endif

struct GoogleCredential {
    let idToken: String
    let firstName: String?
}

enum GoogleAuthError: LocalizedError {
    case sdkUnavailable
    case missingIdToken
    case noPresenter

    var errorDescription: String? {
        switch self {
        case .sdkUnavailable:
            return "Configura el client ID OAuth de iOS (GIDClientID) y el paquete GoogleSignIn para habilitar el acceso con Google."
        case .missingIdToken:
            return "Google no devolvió un token de identidad válido."
        case .noPresenter:
            return "No se encontró una pantalla activa para iniciar sesión."
        }
    }
}

final class GoogleAuthService {
    func signIn() async throws -> GoogleCredential {
        #if canImport(GoogleSignIn)
        guard let clientID = Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String,
              !clientID.isEmpty,
              clientID != "REPLACE_WITH_IOS_CLIENT_ID" else {
            throw GoogleAuthError.sdkUnavailable
        }
        let serverClientID = Bundle.main.object(forInfoDictionaryKey: "GIDServerClientID") as? String
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID, serverClientID: serverClientID)

        guard let presenter = await presentingViewController() else {
            throw GoogleAuthError.noPresenter
        }

        let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: presenter)
        guard let token = result.user.idToken?.tokenString else {
            throw GoogleAuthError.missingIdToken
        }
        return GoogleCredential(idToken: token, firstName: result.user.profile?.givenName)
        #else
        throw GoogleAuthError.sdkUnavailable
        #endif
    }

    @MainActor
    private func presentingViewController() -> UIViewController? {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        let window = scenes.flatMap(\.windows).first { $0.isKeyWindow }
        return window?.rootViewController?.topMostViewController
    }
}

private extension UIViewController {
    var topMostViewController: UIViewController {
        if let presented = presentedViewController { return presented.topMostViewController }
        if let navigation = self as? UINavigationController { return navigation.visibleViewController?.topMostViewController ?? navigation }
        if let tab = self as? UITabBarController { return tab.selectedViewController?.topMostViewController ?? tab }
        return self
    }
}
