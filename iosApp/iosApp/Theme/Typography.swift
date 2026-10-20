import SwiftUI

enum PaquitoTypography {
    // DM Sans is the product font. The system fallback keeps previews and
    // development builds readable until the font files are bundled in iOS.
    static let display = Font.custom("DMSans", size: 34, relativeTo: .largeTitle).weight(.semibold)
    static let headline = Font.custom("DMSans", size: 23, relativeTo: .title2)
    static let greeting = Font.custom("DMSans", size: 30, relativeTo: .title).weight(.bold)
    static let body = Font.custom("DMSans", size: 16, relativeTo: .body)
    static let bodyMedium = Font.custom("DMSans", size: 14, relativeTo: .subheadline)
    static let bodySmall = Font.custom("DMSans", size: 13, relativeTo: .footnote)
    static let caption = Font.custom("DMSans", size: 11, relativeTo: .caption)
    static let taskTitle = Font.custom("DMSans", size: 15, relativeTo: .headline).weight(.semibold)
    static let taskSubtitle = Font.custom("DMSans", size: 12, relativeTo: .subheadline)
    static let dayLabel = Font.custom("DMSans", size: 10, relativeTo: .caption)
    static let dayNumber = Font.custom("DMSans", size: 14, relativeTo: .subheadline).weight(.semibold)
    static let button = Font.custom("DMSans", size: 16, relativeTo: .headline).weight(.semibold)
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
}
