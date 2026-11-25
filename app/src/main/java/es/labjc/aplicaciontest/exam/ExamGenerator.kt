package es.labjc.aplicaciontest.exam

import es.labjc.aplicaciontest.data.Question
import es.labjc.aplicaciontest.data.QuestionBlock
import kotlin.random.Random

/**
 * Monta simulacros a partir de los bloques temáticos. No depende de Android.
 */
object ExamGenerator {

    const val EXAM_SIZE = 50

    /**
     * Decide cuántas preguntas salen de cada bloque.
     *
     * - Todo bloque con preguntas aporta al menos una.
     * - El resto se reparte en proporción al tamaño del bloque (método del
     *   mayor resto), sin pedir nunca más preguntas de las que tiene el bloque.
     * - Los empates se deshacen al azar con [random].
     */
    fun allocate(blockSizes: List<Int>, total: Int, random: Random = Random.Default): List<Int> {
        val available = blockSizes.sum()
        val nonEmpty = blockSizes.count { it > 0 }
        require(total <= available) { "Se piden $total preguntas pero solo hay $available" }
        require(total >= nonEmpty) { "Con $total preguntas no se pueden cubrir $nonEmpty bloques" }

        val result = blockSizes.map { if (it > 0) 1 else 0 }.toMutableList()
        val quotas = blockSizes.map { total.toDouble() * it / available }
        val tieBreakOrder = blockSizes.indices.shuffled(random)

        repeat(total - nonEmpty) {
            val next = tieBreakOrder
                .filter { result[it] < blockSizes[it] }
                .maxBy { quotas[it] - result[it] }
            result[next]++
        }
        return result
    }

    /**
     * Genera un simulacro: preguntas al azar dentro de cada bloque, presentadas
     * en el orden del temario. Las preguntas que no caen en ningún bloque no se usan.
     */
    fun generate(
        questions: List<Question>,
        blocks: List<QuestionBlock>,
        total: Int = minOf(EXAM_SIZE, questions.size),
        random: Random = Random.Default
    ): List<Question> {
        // Si dos bloques se solapan, la pregunta cuenta solo para el primero (así no sale repetida).
        val assigned = mutableSetOf<Int>()
        val byBlock = blocks.map { block ->
            questions.filter { it.number in block.range && assigned.add(it.number) }
        }
        val counts = allocate(byBlock.map { it.size }, total, random)
        return byBlock.zip(counts)
            .flatMap { (blockQuestions, count) -> blockQuestions.shuffled(random).take(count) }
            .sortedBy { it.number }
    }
}
