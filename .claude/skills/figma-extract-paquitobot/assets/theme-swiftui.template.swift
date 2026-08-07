import SwiftUI

/// Paleta de colores centralizada para PaquitoBot.
///
/// Los hex se extraen de la página "Main" del archivo Figma "Paquito (copia)"
/// (fileKey=Piy1K37xHS9jB1qXaaVtuQ, nodeId=109:97) usando get_variable_defs.
///
/// PENDIENTE: reemplazar los strings vacíos por los valores reales una vez
/// que se extraigan las variables con selección activa en Figma.
enum PaquitoColor {
    // Marca principal
    static let brandPrimary   = Color(hex: "") // token: brand/primary
    static let brandSecondary = Color(hex: "") // token: brand/secondary
    static let brandAccent    = Color(hex: "") // token: brand/accent

    // Estados semánticos (semáforo de aprobación)
    static let stateSuccess = Color(hex: "") // token: state/success (verde)
    static let stateWarning = Color(hex: "") // token: state/warning (amarillo)
    static let stateDanger  = Color(hex: "") // token: state/danger (rojo)

    // Superficies
    static let background      = Color(hex: "") // token: surface/background
    static let surfaceElevated = Color(hex: "") // token: surface/elevated
    static let surfaceOverlay  = Color(hex: "") // token: surface/overlay

    // Texto
    static let textPrimary   = Color(hex: "") // token: text/primary
    static let textSecondary = Color(hex: "") // token: text/secondary
    static let textDisabled  = Color(hex: "") // token: text/disabled
    static let textOnPrimary = Color(hex: "") // token: text/on-primary

    // Bordes
    static let borderDefault = Color(hex: "") // token: border/default
    static let borderSubtle  = Color(hex: "") // token: border/subtle
}

/// Escala tipográfica. Los pesos y tamaños exactos vienen de la extracción
/// de variables en Figma; mientras no estén, mantenemos valores por defecto
/// de SwiftUI para no bloquear desarrollo.
enum PaquitoFont {
    static let displayLarge   = Font.system(size: 44, weight: .bold)
    static let headlineMedium = Font.system(size: 28, weight: .semibold)
    static let titleLarge     = Font.system(size: 22, weight: .semibold)
    static let titleMedium    = Font.system(size: 16, weight: .medium)
    static let bodyLarge      = Font.system(size: 16, weight: .regular)
    static let bodyMedium     = Font.system(size: 14, weight: .regular)
    static let caption        = Font.system(size: 11, weight: .regular)
    static let labelSmall     = Font.system(size: 10, weight: .medium)
}

/// Escala de espaciados. Multiplicador base 4.
enum PaquitoSpacing {
    static let xs  : CGFloat = 4
    static let sm  : CGFloat = 8
    static let md  : CGFloat = 16
    static let lg  : CGFloat = 24
    static let xl  : CGFloat = 32
    static let xxl : CGFloat = 48
}

/// Radios de borde.
enum PaquitoRadius {
    static let small  : CGFloat = 8
    static let medium : CGFloat = 12
    static let large  : CGFloat = 20
    static let pill   : CGFloat = 999
}

/// Helper para inicializar Color desde un string hex sin signo "#".
extension Color {
    init(hex: String) {
        var sanitized = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        if sanitized.hasPrefix("#") { sanitized.removeFirst() }
        guard sanitized.count == 6, let value = UInt32(sanitized, radix: 16) else {
            self = .clear
            return
        }
        let r = Double((value & 0xFF0000) >> 16) / 255
        let g = Double((value & 0x00FF00) >>  8) / 255
        let b = Double( value & 0x0000FF       ) / 255
        self = Color(red: r, green: g, blue: b)
    }
}
