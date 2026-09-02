import SwiftUI

struct NotificationBellButton: View {
    let count: Int
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack(alignment: .topTrailing) {
                Image(systemName: "bell.fill")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundStyle(PaquitoColors.textPrimary)
                    .frame(width: 40, height: 40)
                    .background(PaquitoColors.surfaceElevated)
                    .clipShape(Circle())
                    .shadow(color: .black.opacity(0.08), radius: 4, y: 2)

                if count > 0 {
                    Text(count > 9 ? "9+" : "\(count)")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(minWidth: 18, minHeight: 18)
                        .background(PaquitoColors.danger)
                        .clipShape(Capsule())
                        .overlay(Capsule().stroke(.white, lineWidth: 2))
                        .offset(x: 5, y: -5)
                }
            }
            .frame(width: 44, height: 44)
        }
        .buttonStyle(.plain)
    }
}
