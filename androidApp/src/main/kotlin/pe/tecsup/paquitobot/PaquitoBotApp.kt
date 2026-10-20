package pe.tecsup.paquitobot

import android.app.Application
import com.clerk.api.Clerk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.tecsup.paquitobot.data.repository.LocalJsonAcademicRepository
import pe.tecsup.paquitobot.data.local.AcademicScenarioAssignment
import pe.tecsup.paquitobot.data.local.assembleAcademicScenario
import pe.tecsup.paquitobot.data.repository.LocalJsonProcedureRepository
import pe.tecsup.paquitobot.domain.procedures.ProcedureRepository
import pe.tecsup.paquitobot.domain.academic.AcademicRepository

class PaquitoBotApp : Application() {
    val procedureRepository: ProcedureRepository by lazy {
        LocalJsonProcedureRepository {
            withContext(Dispatchers.IO) {
                assets.open("procedures.json").bufferedReader().use { it.readText() }
            }
        }
    }


    private val scenarioAssignment by lazy { AcademicScenarioAssignment(this) }
    private var academicOwner: String? = null
    private var activeAcademicRepository: AcademicRepository? = null

    /** ID visible del mismo escenario que usa el repositorio académico de la cuenta. */
    fun academicScenarioIdFor(clerkUserId: String): String = scenarioAssignment.scenarioFor(clerkUserId)

    /** Una instancia y caché por la cuenta activa; nunca reutiliza datos de otra sesión. */
    @Synchronized
    fun academicRepositoryFor(clerkUserId: String): AcademicRepository {
        require(clerkUserId.isNotBlank())
        if (academicOwner == clerkUserId) activeAcademicRepository?.let { return it }
        val scenarioId = academicScenarioIdFor(clerkUserId)
        val repository = LocalJsonAcademicRepository {
            withContext(Dispatchers.IO) {
                val catalog = assets.open("academic/catalog.json").bufferedReader().use { it.readText() }
                val scenario = assets.open("academic/scenarios/scenario-$scenarioId.json").bufferedReader().use { it.readText() }
                assembleAcademicScenario(catalog, scenario)
            }
        }
        academicOwner = clerkUserId
        activeAcademicRepository = repository
        return repository
    }

    override fun onCreate() {
        super.onCreate()
        Clerk.initialize(
            this,
            publishableKey = BuildConfig.CLERK_PUBLISHABLE_KEY
        )
    }

}
