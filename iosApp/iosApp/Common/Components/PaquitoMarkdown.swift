import SwiftUI

struct PaquitoMarkdown: View {
    let content: String

    var body: some View {
        if let attributed = try? AttributedString(markdown: content) {
            Text(attributed)
                .font(.system(size: 15))
                .foregroundStyle(PaquitoColors.textPrimary)
        } else {
            Text(content)
                .font(.system(size: 15))
                .foregroundStyle(PaquitoColors.textPrimary)
        }
    }
}
