package pe.tecsup.paquitobot.data.local

import android.content.Context
import java.security.MessageDigest

/** Vincula cada cuenta de Clerk a uno de los 15 escenarios de demostración. */
class AcademicScenarioAssignment(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("academic_scenarios", Context.MODE_PRIVATE)

    @Synchronized
    fun scenarioFor(clerkUserId: String): String {
        require(clerkUserId.isNotBlank())
        val digest = MessageDigest.getInstance("SHA-256").digest(clerkUserId.toByteArray(Charsets.UTF_8))
        val key = digest.joinToString("") { "%02x".format(it) }
        preferences.getString(key, null)?.takeIf { it in SCENARIO_IDS }?.let { return it }

        // El hash da la misma asignación en otra instalación con este banco fijo.
        val bucket = digest.fold(0) { remainder, byte -> (remainder * 256 + (byte.toInt() and 0xff)) % SCENARIO_IDS.size }
        val scenarioId = SCENARIO_IDS[bucket]
        preferences.edit().putString(key, scenarioId).apply()
        return scenarioId
    }

    private companion object {
        val SCENARIO_IDS = (1..15).map { it.toString().padStart(2, '0') }
    }
}
