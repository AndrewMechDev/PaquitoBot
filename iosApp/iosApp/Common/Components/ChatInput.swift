import SwiftUI

struct ChatInput: View {
    let enabled: Bool
    let onSend: (String) -> Void

    @State private var text = ""
    @FocusState private var isFocused: Bool

    var body: some View {
        HStack(spacing: 10) {
            TextField("Escribe tu mensaje...", text: $text, axis: .vertical)
                .font(.system(size: 15))
                .lineLimit(1...4)
                .focused($isFocused)
                .disabled(!enabled)
                .padding(.horizontal, 17)
                .frame(minHeight: 47)
                .background(PaquitoColors.surfaceSoft)
                .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))

            Button {
                let value = text.trimmingCharacters(in: .whitespacesAndNewlines)
                guard enabled, !value.isEmpty else { return }
                onSend(value)
                text = ""
                isFocused = false
            } label: {
                Image(systemName: "paperplane.fill")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(width: 47, height: 47)
                    .background(PaquitoColors.brandPrimary.opacity(enabled ? 1 : 0.5))
                    .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            }
            .buttonStyle(.plain)
            .disabled(!enabled)
        }
    }
}
