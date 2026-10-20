import Foundation

enum DemoAcademicData {
    static let home = HomeData(
        dateLabel: "Lunes, 5 de enero de 2026",
        weekTitle: "Semana del 5 al 11 de enero",
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
            TaskItem(id: "task-1", label: "Cálculo II · LAB", title: "Laboratorio 4", timestamp: "Hoy 23:59", urgency: .urgent, dayID: "mon"),
            TaskItem(id: "task-2", label: "Comunicación · FORO", title: "Debate de la semana", timestamp: "Mañana", urgency: .normal, dayID: "tue"),
            TaskItem(id: "task-3", label: "Programación · TAREA", title: "Práctica calificada 2", timestamp: "En 3 días", urgency: .future, dayID: "thu"),
            TaskItem(id: "task-4", label: "Cálculo II · PRÁCTICA", title: "Práctica 3", timestamp: "En 5 días", urgency: .future, dayID: "sat")
        ],
        overallRanking: 15.0,
        careerCode: "C-24",
        currentCycle: 6,
        cycleRankings: [
            CycleRanking(cycle: 1, ranking: 14.2),
            CycleRanking(cycle: 2, ranking: 15.6),
            CycleRanking(cycle: 3, ranking: 13.8),
            CycleRanking(cycle: 4, ranking: 16.4),
            CycleRanking(cycle: 5, ranking: 15.1)
        ],
        importantNotices: [
            HomeFocusItem(id: "notice-attendance", title: "Asistencia a vigilar", detail: "3 faltas registradas · C24"),
            HomeFocusItem(id: "notice-grade", title: "Nota por mejorar", detail: "Laboratorio 1 · Redes · 9/20")
        ],
        importantNoticeCount: 2
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

    static let procedureSource = ProcedureSource(
        title: "Manual de Procesos Académicos · TECSUP N.º 2",
        url: URL(string: "https://www.tecsup.edu.pe/wp-content/uploads/2024/07/mpa-tecsup-2.pdf"),
        edition: "Referencia documental 2024",
        reviewedOn: "18 oct 2026"
    )

    static let procedures: [ProcedureItem] = [
        ProcedureItem(
            id: "constancia-estudios",
            code: "TRM-001",
            title: "Constancia de estudios",
            category: "Académico",
            audience: "Estudiantes",
            summary: "Solicita una constancia oficial de tu matrícula y condición académica.",
            requirements: ["Documento de identidad vigente", "No tener deuda pendiente"],
            steps: ["Revisa los requisitos", "Ingresa tu solicitud", "Espera la confirmación del área responsable"],
            cost: "Según tarifario institucional",
            responseTime: "Hasta 3 días hábiles",
            applicationWindow: nil,
            duration: nil,
            responsibleArea: "Secretaría Académica",
            channels: [
                ProcedureChannel(id: "web-constancia", label: "Abrir Intranet Académica", target: "https://campus-cloud.tecsup.edu.pe/auth/login", kind: .web),
                ProcedureChannel(id: "tarifario-constancia", label: "Consultar tarifario oficial 2026", target: "https://www.tecsup.edu.pe/wp-content/uploads/2026/09/TARIFARIO-DE-PROCESOS-Y-TRAMITES-ACADEMICOS-2026.pdf", kind: .web)
            ],
            notes: ["La entrega puede ser por correo institucional o presencial.", "Confirma la tarifa vigente y su aplicación a tu programa antes de realizar cualquier pago."],
            sourcePage: 6,
            source: procedureSource
        ),
        ProcedureItem(
            id: "justificacion-inasistencia",
            code: "TRM-002",
            title: "Justificación de inasistencia",
            category: "Académico",
            audience: "Estudiantes",
            summary: "Presenta el sustento de una inasistencia dentro del plazo establecido.",
            requirements: ["Sustento de la inasistencia", "Código y curso involucrado"],
            steps: ["Reúne el documento de sustento", "Contacta al área académica", "Confirma el registro de tu solicitud"],
            cost: "Sin costo",
            responseTime: "Según evaluación del área",
            applicationWindow: "Dentro del plazo indicado por Coordinación Académica",
            duration: nil,
            responsibleArea: "Coordinación Académica",
            channels: [ProcedureChannel(id: "email-justificacion", label: "Enviar correo", target: "mesa.partes@tecsup.edu.pe", kind: .email)],
            notes: ["Incluye tu código, curso, fecha de la inasistencia y el sustento correspondiente.", "Verifica el destinatario institucional antes de enviar documentos."],
            sourcePage: nil,
            source: procedureSource
        )
    ]

    static let enrollmentSteps = [
        EnrollmentStep(id: "requirements", title: "Revisa tus requisitos", body: ["Verifica que no tengas pagos o documentos pendientes.", "Confirma tu situación académica en la intranet."], optional: false),
        EnrollmentStep(id: "payment", title: "Realiza el pago", body: ["Usa los canales oficiales de TECSUP.", "Conserva la constancia de pago."], optional: false),
        EnrollmentStep(id: "validation", title: "Espera la validación", body: ["El área responsable revisará tu información.", "Si falta algo, recibirás una indicación por el canal institucional."], optional: false),
        EnrollmentStep(id: "confirmation", title: "Confirma tu matrícula", body: ["Revisa tus cursos y horarios antes del inicio de clases."], optional: false)
    ]

    static let events: [CampusEvent] = [
        CampusEvent(
            id: "piu-2026",
            title: "Encuentro Anual PIU 2026",
            dateLabel: "20–23 oct",
            dateRangeLabel: "20 al 23 de octubre de 2026",
            location: "Arequipa · presencial o virtual",
            category: "Investigación e innovación",
            summary: "Congreso de investigación, producción intelectual, innovación y emprendimiento con ponencias, pósteres y talleres.",
            description: "El Encuentro Anual de Producción Intelectual Universitaria reúne actividades de investigación, desarrollo, innovación y emprendimiento, incluido un simposio sobre fondos concursables, un congreso de producción intelectual y el Premio PIU 2026.",
            whyWorthIt: "Una oportunidad para presentar proyectos académicos, conocer iniciativas de otras disciplinas y conectar con personas que trabajan en investigación e innovación.",
            registrationNote: "El registro ordinario cerró el 10 de octubre. Consulta con la organización si aceptan inscripciones tardías.",
            actionLabel: "Consultar inscripción tardía",
            actionURL: URL(string: "https://www.encuentroanualpiu.com/"),
            sourceName: "Sitio oficial de Encuentro Anual PIU",
            sourceURL: URL(string: "https://www.encuentroanualpiu.com/")
        ),
        CampusEvent(
            id: "devfest-arequipa-2026",
            title: "DevFest Arequipa 2026",
            dateLabel: "7 nov",
            dateRangeLabel: "7 de noviembre de 2026 · jornada presencial",
            location: "Tecsup Arequipa · presencial",
            category: "Conferencia y talleres",
            summary: "Charlas, lightning talks y workshops sobre IA, desarrollo web y móvil, cloud, ciberseguridad y software.",
            description: "La jornada de GDG Arequipa reunirá a estudiantes, desarrolladores y entusiastas de tecnología con charlas, workshops y un mini hackatón de aplicaciones.",
            whyWorthIt: "Muy relevante para estudiantes de Software y Redes. La ficha de GDG Arequipa anuncia registro gratuito.",
            registrationNote: "La página del evento muestra el RSVP para reservar un lugar.",
            actionLabel: "Ver evento e inscripción",
            actionURL: URL(string: "https://gdg.community.dev/events/details/google-gdg-arequipa-presents-devfest-arequipa-2026/"),
            sourceName: "Google Developer Groups · GDG Arequipa",
            sourceURL: URL(string: "https://gdg.community.dev/events/details/google-gdg-arequipa-presents-devfest-arequipa-2026/")
        ),
        CampusEvent(
            id: "jinis-2026",
            title: "JINIS 2026",
            dateLabel: "11–13 nov",
            dateRangeLabel: "11 al 13 de noviembre de 2026",
            location: "Universidad Católica de Santa María · Arequipa",
            category: "Congreso de ingeniería de sistemas",
            summary: "Jornada internacional sobre inteligencia artificial, gobernanza, ética, regulación y ciberseguridad.",
            description: "La Jornada Internacional de Ingeniería de Sistemas reúne a investigadores, reguladores, profesionales y estudiantes para tratar IA, ciberseguridad, privacidad, gobernanza y regulación.",
            whyWorthIt: "Especialmente interesante para Software, Redes y Comunicaciones y para quienes quieran profundizar en las implicaciones de la IA.",
            registrationNote: "Confirma las condiciones vigentes de inscripción antes de realizar cualquier pago.",
            actionLabel: "Consultar programa e inscripción",
            actionURL: URL(string: "https://jinis.ucsm.edu.pe/Jinis/"),
            sourceName: "Sitio oficial de JINIS · UCSM",
            sourceURL: URL(string: "https://jinis.ucsm.edu.pe/Jinis/")
        ),
        CampusEvent(
            id: "nasa-space-apps-arequipa-2026",
            title: "NASA Space Apps Challenge 2026",
            dateLabel: "14–15 nov",
            dateRangeLabel: "14 y 15 de noviembre de 2026",
            location: "Arequipa · convocatoria local",
            category: "Hackatón interdisciplinaria",
            summary: "Equipos multidisciplinarios crean soluciones a desafíos globales usando datos abiertos de la NASA.",
            description: "Durante dos jornadas, participantes de distintas disciplinas trabajan en equipo en desafíos relacionados con la Tierra y el espacio utilizando datos y recursos abiertos de la NASA.",
            whyWorthIt: "Puede conectar software, electrónica, sensores, análisis de datos y soluciones ambientales.",
            registrationNote: "La página global permite registrarse y elegir un evento local. Confirma allí los detalles vigentes de Arequipa.",
            actionLabel: "Consultar convocatoria y registro",
            actionURL: URL(string: "https://www.spaceappschallenge.org/2026/local-events/arequipa/"),
            sourceName: "UNSA · anuncio de la sede Arequipa",
            sourceURL: URL(string: "https://www.unsa.edu.pe/estudiantes-de-la-unsa-podran-participar-en-el-nasa-space-apps-challenge-2026/")
        )
    ]

    static let calendarTasks = [
        CalendarTask(id: "calendar-1", title: "Laboratorio 4", course: "Cálculo II", dueLabel: "Hoy · 23:59", isPast: false, notificationCategory: .laboratories),
        CalendarTask(id: "calendar-2", title: "Práctica calificada 2", course: "Programación II", dueLabel: "Mañana · 12:00", isPast: false),
        CalendarTask(id: "calendar-3", title: "Foro de la semana", course: "Comunicación", dueLabel: "Ayer · 18:00", isPast: true)
    ]

    static func procedure(with id: String) -> ProcedureItem? { procedures.first { $0.id == id } }
    static func event(with id: String) -> CampusEvent? { events.first { $0.id == id } }
}
