package pe.tecsup.paquitobot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform