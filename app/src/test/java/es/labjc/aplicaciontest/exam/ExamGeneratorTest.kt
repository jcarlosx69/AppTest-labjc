package es.labjc.aplicaciontest.exam

import es.labjc.aplicaciontest.data.Question
import es.labjc.aplicaciontest.data.QuestionBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ExamGeneratorTest {

    private val bank = (1..300).map { Question(it, "P$it", "a", "b", "c", "d", 'A') }
    private val autoBlocks = ExamBlocks.automatic(bank.map { it.number }, maxBlocks = 50)

    @Test
    fun `los bloques automaticos son 30 de 10 y cubren las 300 sin solapes`() {
        assertEquals(30, autoBlocks.size)
        assertTrue(autoBlocks.all { it.range.count() == 10 })
        assertEquals((1..300).toList(), autoBlocks.flatMap { it.range })
    }

    @Test
    fun `los bloques automaticos nunca superan el maximo`() {
        val big = ExamBlocks.automatic((1..1000).toList(), maxBlocks = 50)
        assertEquals(50, big.size)
        assertEquals((1..1000).toList(), big.flatMap { it.range })

        val small = ExamBlocks.automatic((1..12).toList(), maxBlocks = 12)
        assertEquals((1..12).toList(), small.flatMap { it.range })
    }

    @Test
    fun `el reparto suma el total y da al menos una a cada bloque`() {
        val sizes = listOf(15, 10, 14, 10, 16, 14, 12, 13, 13, 13, 13, 12, 12, 25, 12, 12, 12, 12, 12, 12, 12, 12, 12)
        repeat(20) { seed ->
            val counts = ExamGenerator.allocate(sizes, 50, Random(seed))
            assertEquals(50, counts.sum())
            assertTrue(counts.all { it >= 1 })
            assertTrue(counts.zip(sizes).all { (n, size) -> n <= size })
        }
    }

    @Test
    fun `un simulacro tiene 50 preguntas distintas y toca todos los bloques`() {
        val exam = ExamGenerator.generate(bank, autoBlocks, random = Random(42))

        assertEquals(50, exam.size)
        assertEquals(50, exam.map { it.number }.toSet().size)
        autoBlocks.forEach { block ->
            assertTrue("Falta el bloque ${block.name}", exam.any { it.number in block.range })
        }
        assertEquals(exam.sortedBy { it.number }, exam)
    }

    @Test
    fun `con bloques solapados ninguna pregunta sale repetida`() {
        val overlapping = listOf(QuestionBlock("A", 1..200), QuestionBlock("B", 150..300))
        repeat(10) { seed ->
            val exam = ExamGenerator.generate(bank, overlapping, random = Random(seed))
            assertEquals(50, exam.map { it.number }.toSet().size)
        }
    }

    @Test
    fun `con menos de 50 preguntas el simulacro las usa todas`() {
        val small = bank.take(20)
        val blocks = ExamBlocks.automatic(small.map { it.number }, maxBlocks = 20)
        assertEquals(20, ExamGenerator.generate(small, blocks).size)
    }

    @Test
    fun `dos simulacros seguidos no salen iguales`() {
        val random = Random(7)
        val first = ExamGenerator.generate(bank, autoBlocks, random = random)
        val second = ExamGenerator.generate(bank, autoBlocks, random = random)
        assertNotEquals(first, second)
    }

    @Test
    fun `la nota no penaliza los fallos`() {
        val questions = bank.take(4)
        val session = ExamSession(
            questions = questions,
            blocks = listOf(QuestionBlock("Todo", 1..4)),
            answers = mapOf(0 to 'A', 1 to 'A', 2 to 'B'), // 2 aciertos, 1 fallo, 1 en blanco
            startedAtMillis = 0L,
            durationMillis = 60_000L
        )

        val result = ExamResult.from(session, nowMillis = 30_000L, timedOut = false)

        assertEquals(2, result.correct)
        assertEquals(1, result.wrong)
        assertEquals(1, result.blank)
        assertEquals(5.0, result.score, 0.001)
        assertEquals(2, result.toReview.size)
        assertEquals(60_000L, result.durationMillis)
    }
}
