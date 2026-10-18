package pe.tecsup.paquitobot

import android.app.Application
import com.clerk.api.Clerk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.tecsup.paquitobot.data.repository.LocalJsonAcademicRepository
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


    /**
     * Fuente unica de datos academicos para toda la app: lee
     * `assets/academic.json` (sin red ni backend) y el repositorio lo parsea
     * una sola vez. Se crea de forma perezosa y vive tanto como el proceso,
     * asi los 4 ViewModels academicos comparten la misma instancia.
     */
    val academicRepository: AcademicRepository by lazy {
        LocalJsonAcademicRepository(
            readJson = {
                withContext(Dispatchers.IO) {
                    assets.open(ACADEMIC_JSON_ASSET).bufferedReader().use { it.readText() }
                }
            },
        )
    }

    override fun onCreate() {
        super.onCreate()
        Clerk.initialize(
            this,
            publishableKey = BuildConfig.CLERK_PUBLISHABLE_KEY
        )
    }

    private companion object {
        const val ACADEMIC_JSON_ASSET = "academic.json"
    }
}
