import Foundation

enum DemoAcademicData {
    static let home = HomeData(
        dateLabel: "Lunes, 5 de enero de 2026",
        average: "15.8",
        absences: "2 / 6",
        dueThisWeek: "4",
        days: [
            WeekDayItem(id: "mon", shortLabel: "Lu", dateLabel: "05", isToday: true, hasTask: true, isCritical: false),
            WeekDayItem(id: "tue", shortLabel: "Ma", dateLabel: "06", isToday: false, hasTask: true, isCritical: false),
            WeekDayItem(id: "wed", shortLabel: "Mi", dateLabel: "07", isToday: false, hasTask: false, isCritical: false),
            WeekDayItem(id: "thu", shortLabel: "Ju", dateLabel: "08", isToday: false, hasTask: true, isCritical: true),
            WeekDayItem(id: "fri", shortLabel: "Vi", dateLabel: "09", isToday: false, hasTask: false, isCritical: false),
            WeekDayItem(id: "sat", shortLabel: "Sa", dateLabel: "10", isToday: false, hasTask: true, isCritical: false),
            WeekDayItem(id: "sun", shortLabel: "Do", dateLabel: "11", isToday: false, hasTask: false, isCritical: false)
        ],
        tasks: [
            TaskItem(id: "task-1", label: "Cálculo II · LAB", title: "Laboratorio 4", timestamp: "Hoy 23:59", urgency: .urgent),
            TaskItem(id: "task-2", label: "Comunicación · FORO", title: "Debate de la semana", timestamp: "Mañana", urgency: .normal),
            TaskItem(id: "task-3", label: "Programación · TAREA", title: "Práctica calificada 2", timestamp: "En 3 días", urgency: .future)
        ]
    )

    static let courses = [
        CourseItem(id: 1, code: "C24", name: "Cálculo II", projectedGrade: "16.4", trackHint: "Vas bien", absences: 1, absenceLimit: 3, nextDueLabel: "Lab 4 · Hoy"),
        CourseItem(id: 2, code: "P24", name: "Programación II", projectedGrade: "14.8", trackHint: "Puedes mejorar", absences: 1, absenceLimit: 3, nextDueLabel: "Práctica · Mañana"),
        CourseItem(id: 3, code: "C18", name: "Comunicación", projectedGrade: "17.2", trackHint: "Excelente", absences: 0, absenceLimit: 2, nextDueLabel: "Foro · Viernes")
    ]

    static let schedule = ScheduleData(
        weekLabel: "Semana del 5 al 11 de enero",
        days: [
            ScheduleDay(id: "monday", label: "Lunes 05", events: [
                ScheduleEvent(id: "m1", time: "08:00", title: "Cálculo II", subtitle: "Aula B-204 · Clase", kind: .classSession),
                ScheduleEvent(id: "m2", time: "23:59", title: "Laboratorio 4", subtitle: "Cálculo II · Entrega", kind: .assignment)
            ]),
            ScheduleDay(id: "tuesday", label: "Martes 06", events: [
                ScheduleEvent(id: "t1", time: "10:00", title: "Programación II", subtitle: "Laboratorio 3 · Clase", kind: .classSession),
                ScheduleEvent(id: "t2", time: "18:00", title: "Comunicación", subtitle: "Aula A-108 · Clase", kind: .classSession)
            ]),
            ScheduleDay(id: "thursday", label: "Jueves 08", events: [
                ScheduleEvent(id: "j1", time: "08:00", title: "Cálculo II", subtitle: "Aula B-204 · Clase", kind: .classSession),
                ScheduleEvent(id: "j2", time: "12:00", title: "Práctica calificada", subtitle: "Programación II · Entrega", kind: .assignment)
            ])
        ]
    )

    static let notifications = [
        NotificationItem(id: "notice-1", title: "Entrega próxima", body: "Laboratorio 4 de Cálculo II vence hoy a las 23:59.", timestamp: "Hace 2 h", severity: .urgent),
        NotificationItem(id: "notice-2", title: "Nuevo promedio", body: "Tu promedio proyectado de Comunicación subió a 17.2.", timestamp: "Ayer", severity: .info),
        NotificationItem(id: "notice-3", title: "Recordatorio", body: "Tienes una práctica de Programación II esta semana.", timestamp: "Hace 2 d", severity: .info)
    ]

    static func detail(for course: CourseItem) -> CourseDetailData {
        CourseDetailData(
            course: course,
            headline: "Tu promedio proyectado es \(course.projectedGrade)",
            projectionHint: "Si mantienes tus próximas entregas, estás encaminado a aprobar el curso.",
            practices: [
                EvaluationItem(id: "\(course.id)-p1", name: "Práctica 1", weightLabel: "20%", scoresLabel: "15 / 20", state: .graded),
                EvaluationItem(id: "\(course.id)-p2", name: "Práctica 2", weightLabel: "20%", scoresLabel: "Pendiente", state: .pending)
            ],
            labs: [
                EvaluationItem(id: "\(course.id)-l1", name: "Laboratorio 3", weightLabel: "15%", scoresLabel: "16 / 20", state: .graded),
                EvaluationItem(id: "\(course.id)-l2", name: "Laboratorio 4", weightLabel: "15%", scoresLabel: "Sin entregar", state: .missing)
            ],
            forums: [
                EvaluationItem(id: "\(course.id)-f1", name: "Foro de la semana", weightLabel: "10%", scoresLabel: "Pendiente", state: .pending)
            ]
        )
    }
}
