import SwiftUI

struct SuggestionChip: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 13))
                .foregroundStyle(PaquitoColors.textPrimary)
                .padding(.horizontal, 15)
                .padding(.vertical, 10)
                .background(PaquitoColors.surfaceSoft)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}
