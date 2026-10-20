import SwiftUI

struct ChatView: View {
    @ObservedObject var viewModel: ChatViewModel
    let userName: String
    @Environment(\.dismiss) private var dismiss
    @State private var draft = ""

    var body: some View {
        VStack(spacing: 0) {
            header

            ScrollViewReader { proxy in
                ScrollView {
                    VStack(alignment: .leading, spacing: 14) {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Hola, \(userName)")
                                .font(PaquitoTypography.greeting)
                                .foregroundStyle(PaquitoColors.textPrimary)
                            Text("Puedes consultarme lo que quieras")
                                .font(PaquitoTypography.body)
                                .foregroundStyle(PaquitoColors.textSecondary)
                        }

                        if viewModel.messages.isEmpty {
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 10) {
                                    SuggestionChip(title: "¿Qué vence esta semana?") {
                                        send("¿Qué vence esta semana?")
                                    }
                                    SuggestionChip(title: "¿Cómo voy?") {
                                        send("¿Cómo voy?")
                                    }
                                }
                            }
                        }

                        ForEach(viewModel.messages) { message in
                            ChatMessageView(message: message)
                                .id(message.id)
                        }

                        if viewModel.isSending {
                            TypingIndicatorView()
                                .id("typing")
                        }
                    }
                    .padding(.horizontal, PaquitoSpacing.lg)
                    .padding(.top, PaquitoSpacing.md)
                    .padding(.bottom, 18)
                }
                .scrollIndicators(.hidden)
                .scrollDismissesKeyboard(.interactively)
                .onChange(of: viewModel.messages.count) { _, _ in
                    scrollToLast(using: proxy)
                }
                .onChange(of: viewModel.isSending) { _, _ in
                    scrollToLast(using: proxy)
                }
            }

            ChatInput(text: $draft, isSending: viewModel.isSending) {
                send(draft)
            }
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.vertical, 10)
            .background(PaquitoColors.background)
        }
        .background(PaquitoColors.background)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    dismiss()
                } label: {
                    PaquitoAssetIcon(name: "PaquitoArrowBack")
                        .frame(width: 21, height: 21)
                }
                .accessibilityLabel("Volver")
            }
        }
    }

    private var header: some View {
        HStack(spacing: 10) {
            Image(systemName: "sparkles")
                .foregroundStyle(PaquitoColors.stateInfoStrong)
            Text("Paquito")
                .font(PaquitoTypography.headline.weight(.semibold))
                .foregroundStyle(PaquitoColors.textPrimary)
            Spacer()
            Text("Asistente académico")
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.textSecondary)
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.top, 10)
        .padding(.bottom, 8)
    }

    private func send(_ text: String) {
        viewModel.send(text)
        draft = ""
    }

    private func scrollToLast(using proxy: ScrollViewProxy) {
        if let last = viewModel.messages.last?.id {
            withAnimation(.easeOut(duration: 0.2)) {
                proxy.scrollTo(last, anchor: .bottom)
            }
        } else if viewModel.isSending {
            withAnimation(.easeOut(duration: 0.2)) {
                proxy.scrollTo("typing", anchor: .bottom)
            }
        }
    }
}
