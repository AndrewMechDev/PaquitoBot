import SwiftUI

enum PaquitoColors {
    static let brandPrimary = Color(hex: 0x00C9FB)
    static let textOnPrimary = Color.white
    static let textOnPrimaryDim = Color.white.opacity(0.88)
    static let stateInfo = Color(hex: 0x0393C9)
    static let stateInfoStrong = Color(hex: 0x0277A8)
    static let stateDanger = Color(hex: 0xFF445A)
    static let stateWarning = Color(hex: 0xC88C14)
    static let stateNeutral = Color(hex: 0x4D5866)
    static let stateSuccess = Color(hex: 0x15A05A)

    static let background = Color.white
    static let surfaceElevated = Color(hex: 0xEEEEEE)
    static let surfaceHomeWeek = Color(hex: 0x10151A)
    static let surfaceGlass = Color(hex: 0xF6F7F9)
    static let surfaceOverlay = Color.black.opacity(0.03)

    static let textPrimary = Color(hex: 0x0D1520)
    static let textSecondary = Color(hex: 0x6E7885)
    static let textMuted = Color(hex: 0x98A1AC)
    static let textOnDark = Color(hex: 0xF4F7FA)
    static let textOnDarkMuted = Color(hex: 0x93A3B4)
    static let textBubble = Color(hex: 0x16202C)
    static let textInputPlaceholder = Color(hex: 0x757575)
    static let textLink = Color(hex: 0x0393C9)

    static let borderDefault = Color(hex: 0x0D1520).opacity(0.08)
    static let borderSubtle = Color(hex: 0x0D1520).opacity(0.05)
}

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
