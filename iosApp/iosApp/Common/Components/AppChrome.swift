import SwiftUI

struct PaquitoMascotView: View {
    var body: some View {
        Image("PaquitoCharacter")
            .resizable()
            .scaledToFit()
            .frame(width: 150, height: 170)
            .accessibilityLabel("PaquitoBot")
    }
}

struct NotificationBellButton: View {
    let count: Int
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack(alignment: .topTrailing) {
                PaquitoAssetIcon(name: "PaquitoBellOutline")
                    .frame(width: 20, height: 20)
                    .foregroundStyle(PaquitoColors.textPrimary)
                    .frame(width: 44, height: 44)
                    .background(PaquitoColors.surfaceGlass)
                    .clipShape(Circle())
                if count > 0 {
                    Text("\(count)")
                        .font(PaquitoTypography.caption.weight(.bold))
                        .foregroundStyle(.white)
                        .frame(minWidth: 20, minHeight: 20)
                        .background(PaquitoColors.stateDanger)
                        .clipShape(Circle())
                        .offset(x: 5, y: -4)
                }
            }
        }
        .accessibilityLabel(count > 0 ? "Notificaciones, \(count) sin leer" : "Notificaciones")
    }
}

struct ChatbotButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image("PaquitoBotIcon")
                .resizable()
                .scaledToFill()
                .frame(width: 56, height: 56)
                .clipShape(Circle())
                .shadow(color: .black.opacity(0.18), radius: 8, y: 4)
        }
        .accessibilityLabel("Abrir chat con Paquito")
    }
}

struct PaquitoAssetIcon: View {
    let name: String

    var body: some View {
        Image(name)
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
    }
}

struct SuggestionChip: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.textBubble)
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(PaquitoColors.surfaceGlass)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(PaquitoColors.borderDefault, lineWidth: 1))
        }
        .accessibilityLabel(title)
    }
}

struct AppTabBar: View {
    @Binding var selection: AppTab
    let onChat: () -> Void

    var body: some View {
        HStack(spacing: 4) {
            ForEach(AppTab.allCases) { tab in
                Button {
                    withAnimation(.easeOut(duration: 0.2)) {
                        selection = tab
                    }
                } label: {
                    Label(tab.title, systemImage: tab.iconName)
                        .font(PaquitoTypography.caption.weight(.semibold))
                        .foregroundStyle(selection == tab ? PaquitoColors.textPrimary : PaquitoColors.textSecondary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 9)
                        .background(selection == tab ? PaquitoColors.surfaceElevated : .clear)
                        .clipShape(Capsule())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(tab.title)
                .accessibilityValue(selection == tab ? "Seleccionado" : "")
            }
            ChatbotButton(action: onChat)
                .padding(.leading, 4)
        }
        .padding(8)
        .background(.ultraThinMaterial)
        .clipShape(Capsule())
        .overlay(Capsule().stroke(PaquitoColors.borderDefault, lineWidth: 1))
        .shadow(color: .black.opacity(0.12), radius: 14, y: 5)
        .padding(.horizontal, 16)
        .padding(.bottom, 8)
    }
}

struct AppTabScaffold<Content: View>: View {
    @Binding var selection: AppTab
    let onChat: () -> Void
    @ViewBuilder let content: () -> Content

    var body: some View {
        content()
            .safeAreaInset(edge: .bottom, spacing: 0) {
                AppTabBar(selection: $selection, onChat: onChat)
            }
    }
}

extension View {
    func paquitoCard() -> some View {
        self
            .padding(PaquitoSpacing.md)
            .background(PaquitoColors.surfaceGlass)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous)
                    .stroke(PaquitoColors.borderSubtle, lineWidth: 1)
            )
    }
}
