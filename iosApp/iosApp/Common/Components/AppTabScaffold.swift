import SwiftUI

private struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}

struct AppTabScaffold<Content: View>: View {
    @Binding var selectedTab: AppTab
    let pendingCount: Int
    let onChatbot: () -> Void
    let onTabSelected: (AppTab) -> Void
    let content: () -> Content

    @State private var navbarVisible = true
    @State private var lastOffset: CGFloat = 0

    init(
        selectedTab: Binding<AppTab>,
        pendingCount: Int,
        onChatbot: @escaping () -> Void,
        onTabSelected: @escaping (AppTab) -> Void,
        @ViewBuilder content: @escaping () -> Content
    ) {
        _selectedTab = selectedTab
        self.pendingCount = pendingCount
        self.onChatbot = onChatbot
        self.onTabSelected = onTabSelected
        self.content = content
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(alignment: .leading, spacing: PaquitoSpacing.lg) {
                    GeometryReader { proxy in
                        Color.clear.preference(key: ScrollOffsetPreferenceKey.self, value: proxy.frame(in: .named("paquito-scroll")).minY)
                    }
                    .frame(height: 0)
                    content()
                }
                .padding(.horizontal, PaquitoSpacing.lg)
                .padding(.top, PaquitoSpacing.lg)
                .padding(.bottom, 112)
            }
            .coordinateSpace(name: "paquito-scroll")
            .scrollIndicators(.hidden)
            .background(PaquitoColors.background)
            .onPreferenceChange(ScrollOffsetPreferenceKey.self) { offset in
                let delta = offset - lastOffset
                guard abs(delta) > 12 else { return }
                withAnimation(.easeOut(duration: 0.22)) {
                    navbarVisible = delta > 0
                }
                lastOffset = offset
            }

            if navbarVisible {
                CustomNavbar(
                    selectedTab: $selectedTab,
                    onChatbot: onChatbot,
                    notificationCount: pendingCount
                )
                .padding(.horizontal, 16)
                .padding(.bottom, 20)
                .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
        }
        .onChange(of: selectedTab) { _, tab in onTabSelected(tab) }
        .ignoresSafeArea(.keyboard, edges: .bottom)
    }
}
