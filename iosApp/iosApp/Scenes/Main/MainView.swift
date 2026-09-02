import SwiftUI

/// Compatibility wrapper for the previous iOS entry point.
/// The runtime now uses `Scenes/Home/HomeView.swift` to mirror HomeScreen.kt.
struct MainView: View {
    let viewModel: HomeViewModel
    let selectedTab: Binding<AppTab>
    let onChatbot: () -> Void
    let onNotifications: () -> Void
    let onTabSelected: (AppTab) -> Void

    var body: some View {
        HomeView(
            selectedTab: selectedTab,
            viewModel: viewModel,
            onChatbot: onChatbot,
            onNotifications: onNotifications,
            onTabSelected: onTabSelected
        )
    }
}
