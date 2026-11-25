package es.labjc.aplicaciontest.data

/**
 * Estadísticas de una pregunta. Solo se guardan contadores: la pregunta en sí
 * se busca por su número en questions.json, así una corrección en el JSON se ve
 * también en el repaso.
 *
 * Todos los parámetros tienen valor por defecto para que Kotlin genere un
 * constructor vacío y Gson pueda leer el JSON antiguo (que no tenía [streak]).
 */
data class QuestionStat(
    var correct: Int = 0,
    var wrong: Int = 0,
    /** Aciertos seguidos desde el último fallo. */
    var streak: Int = 0
) {
    /** Una pregunta está pendiente de repaso si se ha fallado y aún no se ha "dominado". */
    val needsReview: Boolean
        get() = wrong > 0 && streak < REVIEW_STREAK_TO_MASTER

    fun register(isCorrect: Boolean) {
        if (isCorrect) {
            correct++
            streak++
        } else {
            wrong++
            streak = 0
        }
    }

    companion object {
        /** Aciertos seguidos necesarios para que una pregunta salga del Modo Repaso. */
        const val REVIEW_STREAK_TO_MASTER = 2
    }
}
