import SwiftUI

enum PaquitoTypography {
    static let displayLarge = Font.custom("DMSans-SemiBold", size: 34)
    static let headlineSmall = Font.custom("DMSans-Regular", size: 23)
    static let bodyLarge = Font.custom("DMSans-Regular", size: 17)
    static let bodyMedium = Font.custom("DMSans-Regular", size: 14)
    static let bodySmall = Font.custom("DMSans-Regular", size: 12)
    static let caption = Font.custom("DMSans-Regular", size: 11)
    static let greeting = Font.custom("DMSans-Bold", size: 30)
    static let taskTitle = Font.custom("DMSans-SemiBold", size: 14)
    static let taskSubtitle = Font.custom("DMSans-Regular", size: 12)
    static let dayOfWeek = Font.custom("DMSans-SemiBold", size: 10)
    static let dayNumber = Font.custom("DMSans-SemiBold", size: 13)
    static let timestamp = Font.custom("DMSans-SemiBold", size: 24)
    static let buttonLabel = Font.custom("DMSans-SemiBold", size: 13)
}

extension View {
    func paquitoScreen() -> some View {
        self
            .font(PaquitoTypography.bodyMedium)
            .foregroundStyle(PaquitoColors.textPrimary)
            .background(PaquitoColors.background)
    }
}
