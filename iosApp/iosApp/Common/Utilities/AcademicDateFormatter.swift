import Foundation

enum AcademicDateFormatter {
    private static let calendar: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.locale = Locale(identifier: "es_PE")
        calendar.timeZone = TimeZone(secondsFromGMT: 0) ?? .gmt
        return calendar
    }()

    private static let inputFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.calendar = calendar
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    private static let shortFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.calendar = calendar
        formatter.dateFormat = "dd/MM/yy"
        return formatter
    }()

    private static let fullFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.calendar = calendar
        formatter.dateFormat = "EEEE, d 'de' MMMM 'de' yyyy"
        return formatter
    }()

    static func dateKey(from isoDate: String) -> String {
        String(isoDate.prefix(10))
    }

    static func time(from isoDate: String) -> String {
        let value = String(isoDate.dropFirst(min(11, isoDate.count)).prefix(5))
        return value.isEmpty ? "--:--" : value
    }

    static func weekdayName(_ key: String) -> String {
        guard let date = inputFormatter.date(from: key) else { return key }
        return fullFormatter.string(from: date).split(separator: ",", maxSplits: 1).first.map(String.init) ?? key
    }

    static func weekdayWithDay(_ key: String) -> String {
        guard let date = inputFormatter.date(from: key) else { return key }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.calendar = calendar
        formatter.dateFormat = "EEEE d"
        return formatter.string(from: date).capitalized
    }

    static func shortDateLabel(_ key: String) -> String {
        guard let date = inputFormatter.date(from: key) else { return key }
        return shortFormatter.string(from: date)
    }

    static func fullDateLabel(_ key: String) -> String {
        guard let date = inputFormatter.date(from: key) else { return key }
        return fullFormatter.string(from: date).capitalized
    }
}
