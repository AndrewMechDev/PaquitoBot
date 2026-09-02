import SwiftUI

struct CustomNavbar: View {
    @Binding var selectedTab: AppTab
    let onChatbot: () -> Void
    let notificationCount: Int

    var body: some View {
        HStack(spacing: 12) {
            HStack(spacing: 6) {
                ForEach(AppTab.allCases) { tab in
                    Button {
                        withAnimation(.easeInOut(duration: 0.2)) { selectedTab = tab }
                    } label: {
                        VStack(spacing: 4) {
                            Image(systemName: tab.systemImage)
                                .font(.system(size: 16, weight: .semibold))
                            Text(tab.title)
                                .font(.system(size: 10, weight: .medium))
                        }
                        .foregroundStyle(selectedTab == tab ? PaquitoColors.infoStrong : PaquitoColors.textSecondary)
                        .frame(width: 72, height: 48)
                        .background(selectedTab == tab ? PaquitoColors.brandPrimary.opacity(0.16) : .clear)
                        .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(4)
            .background(.ultraThinMaterial, in: Capsule())
            .overlay(Capsule().stroke(.white.opacity(0.7), lineWidth: 1))

            Button(action: onChatbot) {
                ChatbotButton(notificationCount: notificationCount)
            }
            .buttonStyle(.plain)
        }
        .frame(maxWidth: .infinity)
    }
}
