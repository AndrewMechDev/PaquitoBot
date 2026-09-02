import SwiftUI

struct NotificationPreferenceCard: View {
    let preference: NotificationPreference
    let variant: NotificationCardVariant
    let isEnabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            if variant == .compact {
                VStack(alignment: .leading, spacing: 10) {
                    icon
                    Text(preference.title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(PaquitoColors.textPrimary)
                        .multilineTextAlignment(.leading)
                }
                .frame(maxWidth: .infinity, minHeight: 140, alignment: .topLeading)
                .padding(14)
            } else {
                HStack(spacing: 12) {
                    icon
                    Text(preference.title)
                        .font(PaquitoTypography.bodyMedium)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Spacer()
                    Image(systemName: isEnabled ? "checkmark.circle.fill" : "circle")
                        .foregroundStyle(isEnabled ? PaquitoColors.info : PaquitoColors.textMuted)
                }
                .frame(maxWidth: .infinity, minHeight: 62)
                .padding(.horizontal, 16)
            }
        }
        .buttonStyle(.plain)
        .background(isEnabled ? PaquitoColors.brandPrimary.opacity(0.18) : PaquitoColors.surfaceElevated)
        .overlay(
            RoundedRectangle(cornerRadius: variant == .compact ? 20 : 25, style: .continuous)
                .stroke(isEnabled ? PaquitoColors.brandPrimary : .clear, lineWidth: isEnabled ? 2 : 0)
        )
        .clipShape(RoundedRectangle(cornerRadius: variant == .compact ? 20 : 25, style: .continuous))
    }

    private var icon: some View {
        Image(systemName: preference.systemImage)
            .font(.system(size: variant == .compact ? 24 : 18, weight: .semibold))
            .foregroundStyle(PaquitoColors.infoStrong)
            .frame(width: variant == .compact ? 48 : 32, height: variant == .compact ? 48 : 32)
            .background(PaquitoColors.brandPrimary.opacity(0.15))
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }
}

enum NotificationCardVariant: Equatable {
    case compact
    case full
}
