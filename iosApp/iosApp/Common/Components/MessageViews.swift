import SwiftUI

private struct ChatBubbleShape: Shape {
    let user: Bool

    func path(in rect: CGRect) -> Path {
        var path = Path()
        let radius: CGFloat = 20
        let tailRadius: CGFloat = 2
        let topLeft = user ? radius : tailRadius
        let topRight = user ? tailRadius : radius
        path.move(to: CGPoint(x: rect.minX + topLeft, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX - topRight, y: rect.minY))
        path.addQuadCurve(to: CGPoint(x: rect.maxX, y: rect.minY + topRight), control: CGPoint(x: rect.maxX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY - radius))
        path.addQuadCurve(to: CGPoint(x: rect.maxX - radius, y: rect.maxY), control: CGPoint(x: rect.maxX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX + radius, y: rect.maxY))
        path.addQuadCurve(to: CGPoint(x: rect.minX, y: rect.maxY - radius), control: CGPoint(x: rect.minX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + topLeft))
        path.addQuadCurve(to: CGPoint(x: rect.minX + topLeft, y: rect.minY), control: CGPoint(x: rect.minX, y: rect.minY))
        return path
    }
}

struct ChatMessageView: View {
    let message: ChatMessage

    var body: some View {
        switch message.role {
        case .bot:
            HStack(alignment: .bottom, spacing: 8) {
                Image(systemName: "sparkles")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(PaquitoColors.brandPrimary)
                    .frame(width: 28, height: 28)
                    .background(Color(hex: 0x1C1B1F))
                    .clipShape(Circle())
                bubble(isUser: false)
                Spacer(minLength: 0)
            }
        case .user:
            HStack {
                Spacer(minLength: 0)
                bubble(isUser: true)
            }
        case .system:
            Text(message.body)
                .font(.system(size: 12))
                .foregroundStyle(PaquitoColors.textMuted)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(PaquitoColors.textMuted.opacity(0.08))
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                .frame(maxWidth: .infinity, alignment: .center)
        }
    }

    @ViewBuilder
    private func bubble(isUser: Bool) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            if isUser {
                Text(message.body)
                    .font(.system(size: 15))
                    .foregroundStyle(.white)
            } else {
                PaquitoMarkdown(content: message.body)
            }
            if let footnote = message.footnote {
                Text(footnote)
                    .font(.system(size: 11))
                    .foregroundStyle(isUser ? .white.opacity(0.8) : PaquitoColors.textMuted)
            }
            if let timestamp = message.timestamp {
                HStack(spacing: 4) {
                    Spacer()
                    Text(timestamp)
                    if let status = message.status {
                        Text(status == .sent ? "✓" : "✓✓")
                            .foregroundStyle(status == .read ? PaquitoColors.brandPrimary : .white.opacity(0.85))
                    }
                }
                .font(.system(size: 11))
                .foregroundStyle(isUser ? .white.opacity(0.85) : PaquitoColors.textMuted)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 13)
        .frame(maxWidth: isUser ? 290 : 258, alignment: .leading)
        .background(isUser ? PaquitoColors.brandPrimary : PaquitoColors.surfaceSoft)
        .clipShape(ChatBubbleShape(user: isUser))
    }
}

struct TypingIndicatorView: View {
    @State private var phase = false

    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            Image(systemName: "sparkles")
                .font(.system(size: 14, weight: .bold))
                .foregroundStyle(PaquitoColors.brandPrimary)
                .frame(width: 28, height: 28)
                .background(Color(hex: 0x1C1B1F))
                .clipShape(Circle())
            HStack(spacing: 4) {
                ForEach(0..<3, id: \.self) { index in
                    Circle()
                        .fill(PaquitoColors.textMuted)
                        .frame(width: 7, height: 7)
                        .opacity(phase ? 1 : 0.25)
                        .animation(.easeInOut(duration: 0.6).delay(Double(index) * 0.16).repeatForever(autoreverses: true), value: phase)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 15)
            .background(PaquitoColors.surfaceSoft)
            .clipShape(ChatBubbleShape(user: false))
            Spacer(minLength: 0)
        }
        .onAppear { phase = true }
    }
}
