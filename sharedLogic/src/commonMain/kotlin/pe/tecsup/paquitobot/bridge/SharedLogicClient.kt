package pe.tecsup.paquitobot.bridge

import pe.tecsup.paquitobot.data.remote.TokenProvider
import pe.tecsup.paquitobot.data.repository.createDefaultAcademicRepository
import pe.tecsup.paquitobot.data.repository.createDefaultAuthRepository
import pe.tecsup.paquitobot.data.repository.createDefaultCanvasRepository
import pe.tecsup.paquitobot.data.repository.createDefaultChatRepository
import pe.tecsup.paquitobot.domain.academic.AcademicAssignment
import pe.tecsup.paquitobot.domain.academic.AcademicAttendance
import pe.tecsup.paquitobot.domain.academic.AcademicClassSession
import pe.tecsup.paquitobot.domain.academic.AcademicCourse
import pe.tecsup.paquitobot.domain.academic.AcademicGrade
import pe.tecsup.paquitobot.domain.academic.AcademicProfile
import pe.tecsup.paquitobot.domain.auth.AuthSession
import pe.tecsup.paquitobot.domain.chat.ChatAnswer

/**
 * Swift-facing facade for the shared repositories.
 *
 * Android keeps its ViewModels in androidApp, so iOS needs its own presentation
 * layer. This class deliberately exposes domain models only and keeps Ktor,
 * Result and TokenProvider implementation details inside sharedLogic.
 */
class SharedLogicClient(
    accessToken: String?,
    canvasMockKey: String?,
) {
    private var backendAccessToken: String? = accessToken
    private var mockApiKey: String? = canvasMockKey

    private val backendTokenProvider = object : TokenProvider {
        override suspend fun getToken(): String? = backendAccessToken
    }

    private val canvasMockTokenProvider = object : TokenProvider {
        override suspend fun getToken(): String? = mockApiKey
    }

    private val authRepository = createDefaultAuthRepository()
    private val chatRepository = createDefaultChatRepository(backendTokenProvider)
    private val academicRepository = createDefaultAcademicRepository(canvasMockTokenProvider)
    private val canvasRepository = createDefaultCanvasRepository(backendTokenProvider)

    fun setAccessToken(accessToken: String?) {
        backendAccessToken = accessToken
    }

    fun setCanvasMockKey(canvasMockKey: String?) {
        mockApiKey = canvasMockKey
    }

    @Throws(Exception::class)
    suspend fun wakeBackend() {
        authRepository.wakeBackend().getOrThrow()
    }

    @Throws(Exception::class)
    suspend fun loginWithGoogle(idToken: String): AuthSession =
        authRepository.loginWithGoogle(idToken).getOrThrow()

    @Throws(Exception::class)
    suspend fun askAssistant(question: String): ChatAnswer =
        chatRepository.askAssistant(question).getOrThrow()

    @Throws(Exception::class)
    suspend fun academicProfile(): AcademicProfile =
        academicRepository.profile().getOrThrow()

    @Throws(Exception::class)
    suspend fun courses(): List<AcademicCourse> =
        academicRepository.courses().getOrThrow()

    @Throws(Exception::class)
    suspend fun grades(): List<AcademicGrade> =
        academicRepository.grades().getOrThrow()

    @Throws(Exception::class)
    suspend fun attendance(days: Int = 30): List<AcademicAttendance> =
        academicRepository.attendance(days).getOrThrow()

    @Throws(Exception::class)
    suspend fun courseAssignments(courseId: Int): List<AcademicAssignment> =
        academicRepository.courseAssignments(courseId).getOrThrow()

    @Throws(Exception::class)
    suspend fun courseGrades(courseId: Int): List<AcademicGrade> =
        academicRepository.courseGrades(courseId).getOrThrow()

    @Throws(Exception::class)
    suspend fun courseAttendance(courseId: Int): List<AcademicAttendance> =
        academicRepository.courseAttendance(courseId).getOrThrow()

    @Throws(Exception::class)
    suspend fun courseClassSessions(courseId: Int): List<AcademicClassSession> =
        academicRepository.courseClassSessions(courseId).getOrThrow()

    @Throws(Exception::class)
    suspend fun connectCanvas(canvasToken: String) {
        canvasRepository.connect(canvasToken).getOrThrow()
    }

    @Throws(Exception::class)
    suspend fun syncCanvas() {
        canvasRepository.sync().getOrThrow()
    }
}
