package es.labjc.aplicaciontest.exam

import es.labjc.aplicaciontest.data.QuestionBlock

/**
 * Bloques temáticos del simulacro.
 *
 * Cada categoría puede definir los suyos en questions.json ("blocks"). Si no lo
 * hace, se reparten las preguntas en bloques consecutivos de unas
 * [AUTO_BLOCK_SIZE] preguntas: el banco suele venir ordenado por temas, así que
 * el simulacro sigue tocando todo el temario.
 */
object ExamBlocks {

    const val AUTO_BLOCK_SIZE = 10

    /**
     * Bloques consecutivos a partir de los números de pregunta.
     * Nunca hay más de [maxBlocks] bloques (cada bloque tiene que aportar al menos una pregunta).
     */
    fun automatic(
        questionNumbers: List<Int>,
        maxBlocks: Int,
        blockSize: Int = AUTO_BLOCK_SIZE
    ): List<QuestionBlock> {
        if (questionNumbers.isEmpty() || maxBlocks <= 0) return emptyList()
        val sorted = questionNumbers.sorted()
        val blockCount = ceilDiv(sorted.size, blockSize).coerceIn(1, maxBlocks)
        val chunkSize = ceilDiv(sorted.size, blockCount)
        return sorted.chunked(chunkSize).map { chunk ->
            QuestionBlock("Preguntas ${chunk.first()}–${chunk.last()}", chunk.first()..chunk.last())
        }
    }

    /** Bloque al que pertenece una pregunta, o null si no está en ninguno. */
    fun blockOf(questionNumber: Int, blocks: List<QuestionBlock>): QuestionBlock? =
        blocks.firstOrNull { questionNumber in it.range }

    private fun ceilDiv(a: Int, b: Int): Int = (a + b - 1) / b
}
