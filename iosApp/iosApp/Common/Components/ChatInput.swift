import SwiftUI

struct ChatInput: View {
    @Binding var text: String
    let isSending: Bool
    let onSend: () -> Void
    @FocusState private var isFocused: Bool

    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            TextField("Pregúntale a Paquito…", text: $text, axis: .vertical)
                .font(PaquitoTypography.body)
                .foregroundStyle(PaquitoColors.textPrimary)
                .lineLimit(1...5)
                .focused($isFocused)
                .submitLabel(.send)
                .onSubmit(onSend)
                .accessibilityLabel("Pregunta para Paquito")

            Button(action: onSend) {
                if isSending {
                    ProgressView()
                        .tint(.white)
                } else {
                    PaquitoAssetIcon(name: "PaquitoSend")
                        .frame(width: 19, height: 16)
                }
            }
            .foregroundStyle(.white)
            .frame(width: 40, height: 40)
            .background(text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isSending ? PaquitoColors.textMuted : PaquitoColors.stateInfoStrong)
            .clipShape(Circle())
            .disabled(text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isSending)
            .accessibilityLabel(isSending ? "Enviando" : "Enviar mensaje")
        }
        .padding(.leading, 16)
        .padding(.trailing, 6)
        .padding(.vertical, 6)
        .background(PaquitoColors.surfaceGlass)
        .clipShape(Capsule())
        .overlay(Capsule().stroke(PaquitoColors.borderDefault, lineWidth: 1))
        .onTapGesture { isFocused = true }
    }
}
