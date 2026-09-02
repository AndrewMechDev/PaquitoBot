import SwiftUI

struct CanvasMockErrorBanner: View {
    let message: String
    let onRetry: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundStyle(PaquitoColors.danger)
            Text(message)
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.danger)
                .frame(maxWidth: .infinity, alignment: .leading)
            Button("Reintentar", action: onRetry)
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.danger)
        }
        .padding(14)
        .background(PaquitoColors.danger.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))
    }
}
