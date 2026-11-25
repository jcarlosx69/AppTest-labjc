package es.labjc.aplicaciontest.data

/** Un bloque temático del simulacro: un rango de números de pregunta. */
data class QuestionBlock(val name: String, val range: IntRange)

/** Banco de preguntas de una categoría, ya validado. */
data class TestData(
    val title: String,
    val questions: List<Question>,
    /** Bloques temáticos para el simulacro (los del JSON o, si no hay, automáticos). */
    val blocks: List<QuestionBlock>
)

// ---------------------------------------------------------------------------
// Formato de assets/questions.json, el mismo que generan los conversores:
//
// {
//   "title": "Auxiliar de Enfermería - SCS",
//   "blocks": [ { "name": "Farmacología", "from": 131, "to": 143 } ],   <- opcional
//   "questions": [
//     { "text": "...", "options": [ { "text": "...", "isCorrect": false }, ... ] }
//   ]
// }
//
// Todos los campos tienen valor por defecto para que Gson pueda crear los
// objetos aunque falte alguno en el fichero.
// ---------------------------------------------------------------------------

data class TestDataJson(
    val title: String? = null,
    val blocks: List<BlockJson>? = null,
    val questions: List<QuestionJson>? = null
)

data class QuestionJson(
    val text: String? = null,
    val options: List<OptionJson>? = null
)

data class OptionJson(
    val text: String? = null,
    val isCorrect: Boolean = false
)

data class BlockJson(
    val name: String? = null,
    val from: Int = 0,
    val to: Int = 0
)
