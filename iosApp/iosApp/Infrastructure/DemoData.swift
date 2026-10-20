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

    static let procedures: [ProcedureItem] = [
        ProcedureItem(
            id: "constancia-estudios",
            code: "TRM-001",
            title: "Constancia de estudios",
            category: "Académico",
            summary: "Solicita una constancia oficial de tu matrícula y condición académica.",
            requirements: ["Documento de identidad vigente", "No tener deuda pendiente"],
            steps: ["Revisa los requisitos", "Ingresa tu solicitud", "Espera la confirmación del área responsable"],
            cost: "Según tarifario institucional",
            responseTime: "Hasta 3 días hábiles",
            responsibleArea: "Secretaría Académica",
            channels: [ProcedureChannel(id: "web-constancia", label: "Abrir Intranet Académica", target: "https://campus-cloud.tecsup.edu.pe/auth/login", kind: .web)]
        ),
        ProcedureItem(
            id: "justificacion-inasistencia",
            code: "TRM-002",
            title: "Justificación de inasistencia",
            category: "Académico",
            summary: "Presenta el sustento de una inasistencia dentro del plazo establecido.",
            requirements: ["Sustento de la inasistencia", "Código y curso involucrado"],
            steps: ["Reúne el documento de sustento", "Contacta al área académica", "Confirma el registro de tu solicitud"],
            cost: "Sin costo",
            responseTime: "Según evaluación del área",
            responsibleArea: "Coordinación Académica",
            channels: [ProcedureChannel(id: "email-justificacion", label: "Enviar correo", target: "mesa.partes@tecsup.edu.pe", kind: .email)]
        )
    ]

    static let enrollmentSteps = [
        EnrollmentStep(id: "requirements", title: "Revisa tus requisitos", body: ["Verifica que no tengas pagos o documentos pendientes.", "Confirma tu situación académica en la intranet."], optional: false),
        EnrollmentStep(id: "payment", title: "Realiza el pago", body: ["Usa los canales oficiales de TECSUP.", "Conserva la constancia de pago."], optional: false),
        EnrollmentStep(id: "validation", title: "Espera la validación", body: ["El área responsable revisará tu información.", "Si falta algo, recibirás una indicación por el canal institucional."], optional: false),
        EnrollmentStep(id: "confirmation", title: "Confirma tu matrícula", body: ["Revisa tus cursos y horarios antes del inicio de clases."], optional: false)
    ]

    static let events: [CampusEvent] = [
        CampusEvent(id: "tech-week", title: "Semana de innovación tecnológica", dateLabel: "18 de noviembre · 10:00", location: "Campus TECSUP", description: "Charlas, talleres y proyectos para conectar a estudiantes con la comunidad tecnológica.", sourceName: "TECSUP", sourceURL: URL(string: "https://www.tecsup.edu.pe")),
        CampusEvent(id: "devfest", title: "DevFest Arequipa 2026", dateLabel: "28 de noviembre · 09:00", location: "Arequipa", description: "Encuentro de desarrolladores con charlas, workshops y espacios para compartir proyectos.", sourceName: "Google Developer Groups", sourceURL: URL(string: "https://gdg.community.dev")),
        CampusEvent(id: "space-apps", title: "NASA Space Apps Challenge", dateLabel: "4 de octubre · 08:00", location: "Evento virtual", description: "Reto internacional para crear soluciones con datos abiertos y trabajo colaborativo.", sourceName: "NASA Space Apps", sourceURL: URL(string: "https://www.spaceappschallenge.org"))
    ]

    static let calendarTasks = [
        CalendarTask(id: "calendar-1", title: "Laboratorio 4", course: "Cálculo II", dueLabel: "Hoy · 23:59", isPast: false),
        CalendarTask(id: "calendar-2", title: "Práctica calificada 2", course: "Programación II", dueLabel: "Mañana · 12:00", isPast: false),
        CalendarTask(id: "calendar-3", title: "Foro de la semana", course: "Comunicación", dueLabel: "Ayer · 18:00", isPast: true)
    ]

    static func procedure(with id: String) -> ProcedureItem? { procedures.first { $0.id == id } }
    static func event(with id: String) -> CampusEvent? { events.first { $0.id == id } }
}
