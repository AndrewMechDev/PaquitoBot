import SwiftUI

struct ChatbotButton: View {
    var notificationCount: Int = 0

    var body: some View {
        ZStack(alignment: .topTrailing) {
            ZStack {
                Circle()
                    .fill(.black.opacity(0.9))
                    .frame(width: 60, height: 60)
                Image(systemName: "sparkles")
                    .font(.system(size: 27, weight: .semibold))
                    .foregroundStyle(PaquitoColors.brandPrimary)
            }
            .frame(width: 76, height: 76)
            .background(.ultraThinMaterial, in: Circle())
            .overlay(Circle().stroke(.white.opacity(0.22), lineWidth: 1))
            .shadow(color: .black.opacity(0.18), radius: 8, y: 4)

            if notificationCount > 0 {
                Text(notificationCount > 9 ? "9+" : "\(notificationCount)")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(minWidth: 22, minHeight: 22)
                    .background(PaquitoColors.danger)
                    .clipShape(Capsule())
                    .overlay(Capsule().stroke(.white, lineWidth: 2))
                    .offset(x: 5, y: -3)
            }
        }
        .accessibilityLabel("Abrir chat con Paquito")
    }
}
