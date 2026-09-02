import SwiftUI

struct ChatHeaderView: View {
    let onBack: () -> Void

    var body: some View {
        HStack {
            PaquitoBackButton(action: onBack)
            Spacer()
        }
        .padding(.horizontal, 12)
    }
}
