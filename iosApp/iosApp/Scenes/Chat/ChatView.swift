import SwiftUI

struct ChatView: View {
    let viewModel: ChatViewModel
    let onBack: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            ChatHeaderView(onBack: onBack)
            ScrollViewReader { proxy in
                ScrollView {
                    VStack(alignment: .leading, spacing: 15) {
                        VStack(alignment: .leading, spacing: 6) {
                            Text(viewModel.greeting).font(PaquitoTypography.greeting)
                            Text("Puedes consultarme lo que quieras").font(PaquitoTypography.bodyMedium).foregroundStyle(PaquitoColors.textSecondary)
                        }
                        if viewModel.messages.isEmpty {
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 10) {
                                    SuggestionChip(title: "¿Qué vence esta semana?") { viewModel.send("¿Qué vence esta semana?") }
                                    SuggestionChip(title: "¿Cómo voy?") { viewModel.send("¿Cómo voy?") }
                                }
                            }
                        }
                        ForEach(viewModel.messages) { message in
                            ChatMessageView(message: message).id(message.id)
                        }
                        if viewModel.isSending { TypingIndicatorView().id("typing") }
                    }
                    .padding(.horizontal, PaquitoSpacing.lg)
                    .padding(.top, 10)
                    .padding(.bottom, 18)
                }
                .scrollIndicators(.hidden)
                .onChange(of: viewModel.messages.count) { _, _ in scrollToLast(proxy) }
                .onChange(of: viewModel.isSending) { _, _ in scrollToLast(proxy) }
            }
            ChatInput(enabled: !viewModel.isSending, onSend: viewModel.send)
                .padding(.horizontal, PaquitoSpacing.lg)
                .padding(.vertical, 12)
                .background(PaquitoColors.background)
        }
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
        .scrollDismissesKeyboard(.interactively)
        .toolbar(.hidden, for: .navigationBar)
    }

    private func scrollToLast(_ proxy: ScrollViewProxy) {
        guard let last = viewModel.messages.last?.id else {
            if viewModel.isSending { proxy.scrollTo("typing", anchor: .bottom) }
            return
        }
        withAnimation(.easeInOut(duration: 0.2)) { proxy.scrollTo(last, anchor: .bottom) }
    }
}
