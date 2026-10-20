import SwiftUI
import ClerkKit

@main
@MainActor
struct iOSApp: App {
    private let clerk: Clerk?

    init() {
        guard let publishableKey = Bundle.main.object(forInfoDictionaryKey: "CLERK_PUBLISHABLE_KEY") as? String,
              publishableKey.hasPrefix("pk_") else {
            clerk = nil
            return
        }

        clerk = Clerk.configure(publishableKey: publishableKey)
    }

    var body: some Scene {
        WindowGroup {
            if let clerk {
                ContentView()
                    .environment(clerk)
                    .onOpenURL { url in
                        Task {
                            try? await clerk.handle(url)
                        }
                    }
            } else {
                ContentView()
            }
        }
    }
}
