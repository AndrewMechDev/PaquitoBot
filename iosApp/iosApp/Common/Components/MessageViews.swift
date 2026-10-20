import SwiftUI

struct ChatMessageView: View {
    let message: ChatMessage

    var body: some View {
        switch message.role {
        case .system:
            Text(message.body)
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.textSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 4)
                .accessibilityLabel("Aviso: \(message.body)")
        case .bot:
            bubble(alignment: .leading, background: PaquitoColors.surfaceGlass, foreground: PaquitoColors.textBubble)
        case .user:
            bubble(alignment: .trailing, background: PaquitoColors.brandPrimary, foreground: PaquitoColors.textOnPrimary)
        }
    }

    @ViewBuilder
    private func bubble(alignment: HorizontalAlignment, background: Color, foreground: Color) -> some View {
        VStack(alignment: alignment, spacing: 4) {
            Text(markdown: message.body)
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(foreground)
                .frame(maxWidth: 315, alignment: alignment == .leading ? .leading : .trailing)
            Text(message.timestamp, style: .time)
                .font(PaquitoTypography.caption)
                .foregroundStyle(foreground.opacity(0.65))
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(background)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .frame(maxWidth: .infinity, alignment: alignment == .leading ? .leading : .trailing)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(message.body)
    }
}

private extension Text {
    init(markdown: String) {
        if let attributed = try? AttributedString(markdown: markdown) {
            self.init(attributed)
        } else {
            self.init(markdown)
        }
    }
}

struct TypingIndicatorView: View {
    @State private var activeDot = 0

    var body: some View {
        HStack(spacing: 5) {
            ForEach(0..<3, id: \.self) { index in
                Circle()
                    .fill(PaquitoColors.textSecondary)
                    .frame(width: 6, height: 6)
                    .opacity(activeDot == index ? 1 : 0.35)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(PaquitoColors.surfaceGlass)
        .clipShape(Capsule())
        .accessibilityLabel("Paquito está escribiendo")
        .task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 280_000_000)
                withAnimation(.easeInOut(duration: 0.18)) {
                    activeDot = (activeDot + 1) % 3
                }
            }
        }
    }
}
