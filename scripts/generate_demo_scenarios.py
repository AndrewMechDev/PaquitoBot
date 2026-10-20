"""Generate the fixed academic demo catalog and its 15 account scenarios."""

import json
from datetime import date, datetime, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1] / "androidApp" / "src" / "main" / "assets"
ACADEMIC = ROOT / "academic"
CATALOG = ACADEMIC / "catalog.json"
SCENARIOS = ACADEMIC / "scenarios"

BASE_SCORES = {
    101: 16, 102: 11, 104: 18, 105: 14, 106: 12, 108: 20,
    201: 15, 202: 17, 204: 17, 205: 16, 301: 12, 302: 13,
    303: 10, 401: 18, 402: 15, 501: 9,
}

# An absence count is per course. Laboratory absences select laboratory sessions first.
DEFINITIONS = [
    {"id": "01", "label": "Estudiante regular", "absences": {1: 1, 3: 1}},
    {"id": "02", "label": "Alto rendimiento", "grade_shift": 3},
    {"id": "03", "label": "Notas bajas", "grade_shift": -6, "absences": {1: 2, 2: 1}},
    {"id": "04", "label": "Faltas en laboratorios de Física", "lab_absences": {2: 4}},
    {"id": "05", "label": "Faltas en laboratorios de varios cursos", "lab_absences": {1: 3, 3: 2, 5: 2}},
    {"id": "06", "label": "Asistencia en riesgo en Algoritmos", "absences": {3: 6}},
    {"id": "07", "label": "Asistencia al límite en Física", "absences": {2: 8}},
    {"id": "08", "label": "Límite de faltas excedido en Cálculo", "absences": {1: 9}},
    {"id": "09", "label": "Entregas vencidas", "absences": {4: 2}, "due_overrides": {103: "2026-10-16T23:59:00-05:00", 203: "2026-10-17T23:59:00-05:00"}},
    {"id": "10", "label": "Varias entregas próximas", "due_overrides": {107: "2026-10-20T23:59:00-05:00", 109: "2026-10-21T23:59:00-05:00", 502: "2026-10-22T23:59:00-05:00"}},
    {"id": "11", "label": "Recuperación académica", "recovery": True, "absences": {1: 2, 3: 2}},
    {"id": "12", "label": "Rendimiento desigual entre cursos", "mixed": True, "absences": {2: 4, 4: 1}},
    {"id": "13", "label": "Laboratorios pendientes", "ungraded": [104, 105, 106, 204, 205, 302, 402, 501], "due_overrides": {104: "2026-10-20T23:59:00-05:00", 204: "2026-10-22T23:59:00-05:00", 302: "2026-10-25T23:59:00-05:00"}},
    {"id": "14", "label": "Inicio de ciclo", "keep_grades": [101, 104, 201, 301, 401]},
    {"id": "15", "label": "Riesgo académico integral", "grade_shift": -7, "absences": {1: 7, 2: 5, 5: 2}, "ungraded": [104, 204, 501], "due_overrides": {104: "2026-10-15T23:59:00-05:00", 204: "2026-10-17T23:59:00-05:00"}},
]


def write_json(path: Path, value: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def add_historical_sessions(catalog: dict) -> None:
    """Give attendance-risk scenarios enough completed sessions before 19 Oct."""
    schedules = {
        1: ("2026-09-07", 2, ("08:00:00", "10:00:00")),
        2: ("2026-09-08", 3, ("10:00:00", "12:00:00")),
        3: ("2026-09-08", 2, ("14:00:00", "16:00:00")),
        4: ("2026-09-09", 2, ("14:00:00", "16:00:00")),
        5: ("2026-09-07", 2, ("16:00:00", "18:00:00")),
    }
    known = {session["id"] for session in catalog["class_sessions"]}
    for course_id, (start, second_offset, hours) in schedules.items():
        for week in range(4):
            for within_week in range(2):
                index = week * 2 + within_week
                session_id = course_id * 1000 + 100 + index + 1
                if session_id in known:
                    continue
                day = date.fromisoformat(start) + timedelta(days=week * 7 + (second_offset if within_week else 0))
                first_hour, last_hour = hours
                if course_id == 4 and within_week:
                    first_hour, last_hour = "08:00:00", "10:00:00"
                catalog["class_sessions"].append({
                    "id": session_id,
                    "course_id": course_id,
                    "start_at": f"{day}T{first_hour}-05:00",
                    "end_at": f"{day}T{last_hour}-05:00",
                    "kind": "laboratory" if course_id == 5 or within_week else "class",
                })
                known.add(session_id)
    catalog["class_sessions"].sort(key=lambda session: (session["start_at"], session["course_id"]))


def main() -> None:
    if CATALOG.exists():
        catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    else:
        original = json.loads((ROOT / "academic.json").read_text(encoding="utf-8"))
        catalog = {key: original[key] for key in ("term", "courses", "assignments", "class_sessions")}
        for session in catalog["class_sessions"]:
            number = session["id"] % 100
            session["kind"] = "laboratory" if session["course_id"] == 5 or number % 2 == 0 else "class"
    add_historical_sessions(catalog)
    write_json(CATALOG, catalog)

    sessions_by_course = {course["id"]: sorted(
        (session for session in catalog["class_sessions"] if session["course_id"] == course["id"]),
        key=lambda session: session["start_at"],
    ) for course in catalog["courses"]}
    assignment_course = {assignment["id"]: assignment["course_id"] for assignment in catalog["assignments"]}

    for definition in DEFINITIONS:
        absent_ids = set()
        for course_id, count in definition.get("absences", {}).items():
            absent_ids.update(session["id"] for session in sessions_by_course[course_id][:count])
        for course_id, count in definition.get("lab_absences", {}).items():
            labs = [session for session in sessions_by_course[course_id] if session["kind"] == "laboratory"]
            absent_ids.update(session["id"] for session in labs[:count])

        grades = []
        for assignment_id, base_score in BASE_SCORES.items():
            if assignment_id in definition.get("ungraded", []):
                continue
            if "keep_grades" in definition and assignment_id not in definition["keep_grades"]:
                continue
            score = base_score + definition.get("grade_shift", 0)
            if definition.get("recovery"):
                score = base_score - 5 if assignment_id % 100 <= 2 else base_score + 2
            if definition.get("mixed"):
                score = base_score + (3 if assignment_course[assignment_id] in (1, 3) else -5)
            grades.append({"assignment_id": assignment_id, "score": float(max(0, min(20, score)))})

        scenario = {
            "scenario_id": definition["id"],
            "label": definition["label"],
            "profile": {"id": 100 + int(definition["id"]), "name": "Estudiante de demostración", "email": f"demo{definition['id']}@example.invalid"},
            "grades": grades,
            "attendance": [
                {"class_session_id": session["id"], "status": "absent" if session["id"] in absent_ids else "present"}
                for session in catalog["class_sessions"] if session["start_at"][:10] < "2026-10-19"
            ],
            "due_at_overrides": {str(key): value for key, value in definition.get("due_overrides", {}).items()},
        }
        write_json(SCENARIOS / f"scenario-{definition['id']}.json", scenario)

    assert len(DEFINITIONS) == 15
    assignment_ids = {item["id"] for item in catalog["assignments"]}
    session_ids = {item["id"] for item in catalog["class_sessions"] if item["start_at"][:10] < "2026-10-19"}
    fingerprints = set()
    for path in sorted(SCENARIOS.glob("scenario-*.json")):
        scenario = json.loads(path.read_text(encoding="utf-8"))
        assert {item["assignment_id"] for item in scenario["grades"]} <= assignment_ids
        assert {int(item) for item in scenario["due_at_overrides"]} <= assignment_ids
        assert {item["class_session_id"] for item in scenario["attendance"]} == session_ids
        assert all(0 <= item["score"] <= 20 for item in scenario["grades"])
        assert all(datetime.fromisoformat(value) for value in scenario["due_at_overrides"].values())
        fingerprint = json.dumps((scenario["grades"], scenario["attendance"], scenario["due_at_overrides"]), sort_keys=True)
        assert fingerprint not in fingerprints, f"Escenario duplicado: {path.name}"
        fingerprints.add(fingerprint)
    assert len(fingerprints) == 15
    print(f"15 escenarios válidos; {len(catalog['class_sessions'])} sesiones en el catálogo")


if __name__ == "__main__":
    main()
