import SwiftUI

extension Color {
    init(hex: UInt, alpha: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: alpha
        )
    }
}

enum PaquitoColors {
    static let brandPrimary = Color(hex: 0x00C9FB)
    static let textOnPrimary = Color.white
    static let textPrimary = Color(hex: 0x111318)
    static let textSecondary = Color(hex: 0x5E636B)
    static let textMuted = Color(hex: 0x858A91)
    static let background = Color.white
    static let surfaceElevated = Color(hex: 0xEEEEEE)
    static let surfaceSoft = Color(hex: 0xF6F6F6)
    static let darkCard = Color(hex: 0x10151A)
    static let danger = Color(hex: 0xFF445A)
    static let warning = Color(hex: 0xC88C14)
    static let success = Color(hex: 0x15A05A)
    static let info = Color(hex: 0x0393C9)
    static let infoStrong = Color(hex: 0x0277A8)
    static let border = Color(hex: 0xDDE1E5)
}

enum PaquitoSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let xxl: CGFloat = 48
}

enum PaquitoRadius {
    static let small: CGFloat = 8
    static let medium: CGFloat = 12
    static let large: CGFloat = 20
    static let card: CGFloat = 24
    static let alert: CGFloat = 26
    static let week: CGFloat = 35
}
