package es.labjc.aplicaciontest.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuestionRepositoryTest {

    private fun question(text: String, correct: Int?) = """
        { "text": "$text", "options": [
            { "text": "uno", "isCorrect": ${correct == 0} },
            { "text": "dos", "isCorrect": ${correct == 1} },
            { "text": "tres", "isCorrect": ${correct == 2} },
            { "text": "cuatro", "isCorrect": ${correct == 3} } ] }
    """.trimIndent()

    @Test
    fun `lee el formato de los conversores`() {
        val json = """{ "title": "Celador - SCS", "questions": [ ${question("¿Primera?", 2)} ] }"""

        val data = QuestionRepository.parse(json)

        assertEquals("Celador - SCS", data.title)
        val q = data.questions.single()
        assertEquals(1, q.number)
        assertEquals("¿Primera?", q.text)
        assertEquals("dos", q.optionB)
        assertEquals('C', q.correctAnswer)
    }

    @Test
    fun `admite una lista de preguntas suelta`() {
        val json = "[ ${question("A", 0)}, ${question("B", 3)} ]"
        assertEquals(listOf('A', 'D'), QuestionRepository.parse(json).questions.map { it.correctAnswer })
    }

    @Test
    fun `descarta preguntas sin respuesta correcta y conserva la numeracion`() {
        val errors = mutableListOf<String>()
        val json = """{ "questions": [ ${question("ok", 0)}, ${question("mal", null)}, ${question("ok2", 1)} ] }"""

        val data = QuestionRepository.parse(json) { errors += it }

        assertEquals(listOf(1, 3), data.questions.map { it.number })
        assertEquals(1, errors.size)
    }

    @Test
    fun `sin bloques en el JSON genera bloques automaticos`() {
        val json = """{ "questions": [ ${(1..25).joinToString { question("P$it", 0) }} ] }"""
        val data = QuestionRepository.parse(json)
        assertEquals(3, data.blocks.size)
        assertEquals((1..25).toList(), data.blocks.flatMap { it.range })
    }

    @Test
    fun `ignora bloques que dejan preguntas fuera`() {
        val errors = mutableListOf<String>()
        val json = """{ "blocks": [ { "name": "Solo una", "from": 1, "to": 1 } ],
            "questions": [ ${question("A", 0)}, ${question("B", 0)} ] }"""

        val data = QuestionRepository.parse(json) { errors += it }

        assertEquals(1, errors.size)
        assertEquals((1..2).toList(), data.blocks.flatMap { it.range })
    }

    @Test
    fun `JSON roto no rompe la app`() {
        val errors = mutableListOf<String>()
        val data = QuestionRepository.parse("{ esto no es json") { errors += it }
        assertTrue(data.questions.isEmpty())
        assertEquals(1, errors.size)
    }

    @Test
    fun `el banco de Auxiliar de Enfermeria tiene 300 preguntas validas y 23 bloques`() {
        // Los tests unitarios de Gradle se ejecutan desde la carpeta del módulo (app/).
        val json = File("src/auxiliarEnfermeria/assets/questions.json").readText()
        val errors = mutableListOf<String>()

        val data = QuestionRepository.parse(json) { errors += it }

        assertEquals(emptyList<String>(), errors)
        assertEquals("Auxiliar de Enfermería - SCS", data.title)
        assertEquals((1..300).toList(), data.questions.map { it.number })
        assertEquals(23, data.blocks.size)
        assertEquals((1..300).toList(), data.blocks.flatMap { it.range })
    }
}
