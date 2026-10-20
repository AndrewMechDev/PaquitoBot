package pe.tecsup.paquitobot.data.local

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Combina el catálogo común con las notas, asistencias y fechas del escenario asignado. */
fun assembleAcademicScenario(catalogJson: String, scenarioJson: String): String {
    val catalog = Json.parseToJsonElement(catalogJson).jsonObject
    val scenario = Json.parseToJsonElement(scenarioJson).jsonObject
    val overrides = scenario["due_at_overrides"]?.jsonObject.orEmpty()
    val assignments = catalog.getValue("assignments").jsonArray.map { element ->
        val assignment = element.jsonObject
        val id = assignment.getValue("id").jsonPrimitive.content
        val dueAt = overrides[id]
        if (dueAt == null) assignment else JsonObject(assignment + ("due_at" to dueAt))
    }
    val assignmentIds = assignments.map { it.getValue("id").jsonPrimitive.content }.toSet()
    require(overrides.keys.all { it in assignmentIds }) { "El escenario contiene fechas para tareas inexistentes" }

    return JsonObject(catalog + mapOf(
        "assignments" to JsonArray(assignments),
        "profile" to scenario.getValue("profile"),
        "grades" to scenario.getValue("grades"),
        "attendance" to scenario.getValue("attendance"),
    )).toString()
}
