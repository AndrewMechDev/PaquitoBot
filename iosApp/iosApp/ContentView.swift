import SwiftUI

// MARK: - App Navigation (Root)
struct ContentView: View {
    var body: some View {
        NavigationStack {
            MainView()
        }
    }
}

// MARK: - Vista Principal (main.jpeg)
struct MainView: View {
    let dias = [("Lu", "03"), ("Ma", "04"), ("Mi", "05"), ("Ju", "06"), ("Vi", "07"), ("Sa", "08"), ("Do", "09")]

    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Header
                    VStack(alignment: .leading, spacing: 5) {
                        Text("¡Bienvenido, Usuario!")
                            .font(.system(size: 28, weight: .bold))
                        Text("Lunes, 5 de enero de 2026")
                            .foregroundColor(.gray)
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)

                    // Calendario
                    Text("Semana 10")
                        .font(.headline)
                        .padding(.horizontal)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(0..<dias.count, id: \.self) { index in
                                VStack {
                                    Text(dias[index].0).font(.caption).foregroundColor(index == 0 ? .white : .gray)
                                    Text(dias[index].1).font(.title3).bold().foregroundColor(index == 0 ? .cyan : .black)
                                }
                                .padding(.vertical, 12)
                                .padding(.horizontal, 15)
                                .background(index == 0 ? Color(white: 0.2) : Color.white)
                                .cornerRadius(25)
                            }
                        }
                        .padding(.horizontal)
                    }
                    .padding(.vertical, 10)
                    .background(Color(white: 0.1).cornerRadius(30))
                    .padding(.horizontal)

                    // Tareas Pendientes
                    Text("Tareas Pendientes")
                        .font(.title2)
                        .bold()
                        .padding(.horizontal)
                        .padding(.top, 10)

                    VStack(spacing: 0) {
                        ForEach(0..<4) { _ in
                            TaskRow()
                            Divider().padding(.leading, 50)
                        }
                    }
                    .padding(.horizontal)

                    Spacer(minLength: 100) // Espacio para el menú inferior
                }
            }

            // Custom Bottom Navigation Bar
            VStack {
                Spacer()
                HStack {
                    HStack(spacing: 30) {
                        BottomNavItem(icon: "bookmark.fill", title: "Inicio")
                        BottomNavItem(icon: "book.fill", title: "Cursos")
                        BottomNavItem(icon: "calendar", title: "Horarios")
                    }
                    .padding(.vertical, 15)
                    .padding(.horizontal, 30)
                    .background(Color(white: 0.95))
                    .cornerRadius(40)
                    .shadow(color: .black.opacity(0.1), radius: 5, y: 5)

                    Spacer()

                    // Botón flotante que lleva al Chat
                    NavigationLink(destination: ChatView()) {
                        ZStack(alignment: .topTrailing) {
                            Image(systemName: "face.smiling.inverse") // Placeholder del bot
                                .font(.system(size: 35))
                                .padding(15)
                                .background(Color(white: 0.2))
                                .foregroundColor(.white)
                                .clipShape(Circle())

                            Text("3")
                                .font(.caption2).bold()
                                .foregroundColor(.white)
                                .padding(6)
                                .background(Color.red)
                                .clipShape(Circle())
                                .offset(x: 5, y: -5)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.bottom, 10)
            }
        }
        .navigationBarHidden(true)
    }
}

struct TaskRow: View {
    var body: some View {
        HStack {
            Image(systemName: "doc.text")
                .font(.title2)
            VStack(alignment: .leading) {
                Text("Curso").font(.caption).foregroundColor(.gray)
                Text("Nombre Tarea").font(.subheadline).bold()
            }
            .padding(.leading, 10)

            Spacer()

            Text("12 h")
                .font(.title2).bold()
                .foregroundColor(.cyan)
        }
        .padding(.vertical, 15)
    }
}

struct BottomNavItem: View {
    let icon: String
    let title: String
    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon).font(.system(size: 20))
            Text(title).font(.caption2)
        }
        .foregroundColor(.black)
    }
}

// MARK: - Vista de Chat (chat.jpeg)
struct ChatView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var mensaje = ""
    @State private var mensajes = [
        ChatMessage(texto: "Hola Andrea. Revise tu LMS: 3 cosas por vencer esta semana...", isUser: false),
        ChatMessage(texto: "Lo mas urgente:\nLaboratorio 4 - Calculo II\nHoy 23:59 - 15% de la nota final", isUser: false),
        ChatMessage(texto: "¿cómo voy en calculo?", isUser: true),
        ChatMessage(texto: "Vas en 14.8 con 3 de 5 evaluaciones. Si entregas el Lab 4 completo subes a ~15.6.", isUser: false)
    ]

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { presentationMode.wrappedValue.dismiss() }) {
                    Image(systemName: "arrow.left")
                        .font(.title2)
                        .foregroundColor(.black)
                }
                Spacer()
            }
            .padding()

            VStack(alignment: .leading) {
                Text("Hola, nombre")
                    .font(.largeTitle)
                    .bold()
                Text("Puedes consultarme lo que quieras")
                    .foregroundColor(.gray)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal)

            // Área de Mensajes
            ScrollViewReader { proxy in
                ScrollView {
                    VStack(alignment: .leading, spacing: 15) {
                        ForEach(mensajes) { msg in
                            ChatBubble(message: msg)
                        }
                    }
                    .padding()
                    .id("Bottom")
                }
                .onChange(of: mensajes.count) { _ in
                    withAnimation {
                        proxy.scrollTo("Bottom", anchor: .bottom)
                    }
                }
            }

            // Sugerencias
            ScrollView(.horizontal, showsIndicators: false) {
                HStack {
                    SuggestionChip(text: "Que vence esta semana?")
                    SuggestionChip(text: "Como voy?")
                }
                .padding(.horizontal)
            }
            .padding(.bottom, 10)

            // Input Text (Se ajusta solo con el teclado en SwiftUI)
            HStack {
                TextField("Escribe tu mensaje...", text: $mensaje)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .background(Color(white: 0.95))
                    .cornerRadius(25)

                Button(action: {
                    if !mensaje.isEmpty {
                        mensajes.append(ChatMessage(texto: mensaje, isUser: true))
                        mensaje = ""
                    }
                }) {
                    Image(systemName: "location.fill") // Icono de enviar
                        .rotationEffect(.degrees(45))
                        .padding(15)
                        .background(Color.cyan)
                        .foregroundColor(.white)
                        .clipShape(Circle())
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 10)
        }
        .navigationBarHidden(true)
    }
}

struct ChatMessage: Identifiable {
    let id = UUID()
    let texto: String
    let isUser: Bool
}

struct ChatBubble: View {
    let message: ChatMessage

    var body: some View {
        HStack {
            if message.isUser { Spacer() }

            Text(message.texto)
                .padding()
                .background(message.isUser ? Color.cyan : Color(white: 0.92))
                .foregroundColor(message.isUser ? .white : .black)
                .cornerRadius(15)
                .frame(maxWidth: 280, alignment: message.isUser ? .trailing : .leading)

            if !message.isUser { Spacer() }
        }
    }
}

struct SuggestionChip: View {
    let text: String
    var body: some View {
        Text(text)
            .font(.subheadline)
            .padding(.horizontal, 15)
            .padding(.vertical, 10)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color(white: 0.9), lineWidth: 1)
            )
    }
}

#Preview {
    ContentView()
}