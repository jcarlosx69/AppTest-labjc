package es.labjc.aplicaciontest.exam

import es.labjc.aplicaciontest.data.Question
import es.labjc.aplicaciontest.data.QuestionBlock

/** Simulacro en curso. Inmutable: cada cambio crea una copia. */
data class ExamSession(
    val questions: List<Question>,
    /** Bloques temáticos de la categoría, para mostrar y corregir por bloques. */
    val blocks: List<QuestionBlock>,
    /** Respuestas marcadas, por posición en [questions]. Las que faltan están en blanco. */
    val answers: Map<Int, Char> = emptyMap(),
    val currentIndex: Int = 0,
    val startedAtMillis: Long,
    val durationMillis: Long
) {
    val answeredCount: Int get() = answers.size
    val blankCount: Int get() = questions.size - answers.size

    fun remainingMillis(nowMillis: Long): Long =
        (startedAtMillis + durationMillis - nowMillis).coerceAtLeast(0L)
}

/** Una pregunta del simulacro ya corregida. */
data class ExamItem(
    val position: Int,
    val question: Question,
    val answer: Char?,
    val blockName: String
) {
    val isBlank: Boolean get() = answer == null
    val isCorrect: Boolean get() = question.isCorrect(answer)
    val isWrong: Boolean get() = !isBlank && !isCorrect
}

data class BlockScore(val blockName: String, val correct: Int, val total: Int)

/** Resultado de un simulacro entregado. Nota sin penalización por fallos. */
data class ExamResult(
    val items: List<ExamItem>,
    val elapsedMillis: Long,
    val timedOut: Boolean,
    /** Tiempo total que tenía el simulacro. */
    val durationMillis: Long
) {
    val total: Int get() = items.size
    val correct: Int get() = items.count { it.isCorrect }
    val wrong: Int get() = items.count { it.isWrong }
    val blank: Int get() = items.count { it.isBlank }

    /** Nota sobre 10. */
    val score: Double get() = if (total == 0) 0.0 else correct * 10.0 / total

    /** Preguntas falladas o en blanco: las que pasan al Modo Repaso. */
    val toReview: List<ExamItem> get() = items.filter { !it.isCorrect }

    /** Aciertos por bloque, en el orden en que aparecen en el simulacro. */
    val byBlock: List<BlockScore>
        get() = items.groupBy { it.blockName }
            .map { (name, blockItems) -> BlockScore(name, blockItems.count { it.isCorrect }, blockItems.size) }

    companion object {
        fun from(session: ExamSession, nowMillis: Long, timedOut: Boolean): ExamResult {
            val items = session.questions.mapIndexed { index, question ->
                ExamItem(
                    position = index + 1,
                    question = question,
                    answer = session.answers[index],
                    blockName = ExamBlocks.blockOf(question.number, session.blocks)?.name ?: "Otros"
                )
            }
            val elapsed = (nowMillis - session.startedAtMillis).coerceIn(0L, session.durationMillis)
            return ExamResult(items, elapsed, timedOut, session.durationMillis)
        }
    }
}
