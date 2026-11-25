package es.labjc.aplicaciontest.data

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import es.labjc.aplicaciontest.exam.ExamBlocks
import es.labjc.aplicaciontest.exam.ExamGenerator

/**
 * Lee y valida el banco de preguntas (assets/questions.json).
 *
 * [parse] no depende de Android, así que se prueba con tests unitarios normales.
 * Las preguntas mal formadas se descartan y se avisa con onError; nunca se
 * inventa una respuesta correcta.
 */
object QuestionRepository {

    const val ASSET_FILE = "questions.json"

    private val gson = Gson()

    fun parse(json: String, onError: (String) -> Unit = {}): TestData {
        val root = try {
            JsonParser.parseString(json.removePrefix("﻿"))
        } catch (e: JsonParseException) {
            onError("$ASSET_FILE no es un JSON válido: ${e.message}")
            return TestData("", emptyList(), emptyList())
        }

        // Formato normal: objeto { title, blocks?, questions }.
        // También se admite una lista de preguntas suelta (formato antiguo).
        val raw: TestDataJson = when {
            root.isJsonObject -> gson.fromJson(root, TestDataJson::class.java)
            root.isJsonArray -> TestDataJson(
                questions = gson.fromJson(root, Array<QuestionJson>::class.java).toList()
            )
            else -> TestDataJson()
        }

        // El número de pregunta es su posición en el fichero (1, 2, 3...), aunque se
        // descarte alguna, para que los rangos de los bloques sigan cuadrando.
        val questions = raw.questions.orEmpty().mapIndexedNotNull { index, json ->
            toQuestion(index + 1, json, onError)
        }

        val examSize = minOf(ExamGenerator.EXAM_SIZE, questions.size)
        val blocks = validBlocks(raw.blocks, questions, examSize, onError)
            ?: ExamBlocks.automatic(questions.map { it.number }, maxBlocks = examSize)

        return TestData(
            title = raw.title?.trim().orEmpty(),
            questions = questions,
            blocks = blocks
        )
    }

    private fun toQuestion(number: Int, json: QuestionJson, onError: (String) -> Unit): Question? {
        val text = json.text?.normalizeSpaces().orEmpty()
        val options = json.options.orEmpty()
        val correctCount = options.count { it.isCorrect }

        if (text.isEmpty() || options.size != 4 || correctCount != 1) {
            onError("Pregunta $number descartada: ${options.size} opciones, $correctCount correctas")
            return null
        }
        val texts = options.map { it.text?.normalizeSpaces().orEmpty() }
        val correctLetter = "ABCD"[options.indexOfFirst { it.isCorrect }]

        return Question(
            number = number,
            text = text,
            optionA = texts[0],
            optionB = texts[1],
            optionC = texts[2],
            optionD = texts[3],
            correctAnswer = correctLetter
        )
    }

    /**
     * Devuelve los bloques del JSON si son utilizables, o null para usar los automáticos.
     * Tienen que cubrir todas las preguntas válidas y no ser más que las preguntas del simulacro.
     */
    private fun validBlocks(
        json: List<BlockJson>?,
        questions: List<Question>,
        examSize: Int,
        onError: (String) -> Unit
    ): List<QuestionBlock>? {
        if (json.isNullOrEmpty()) return null
        val blocks = json.mapIndexed { i, b ->
            QuestionBlock(b.name?.trim().takeUnless { it.isNullOrEmpty() } ?: "Bloque ${i + 1}", b.from..b.to)
        }
        val uncovered = questions.filter { q -> blocks.none { q.number in it.range } }
        val nonEmpty = blocks.count { block -> questions.any { it.number in block.range } }
        return when {
            uncovered.isNotEmpty() -> {
                onError("Bloques ignorados: ${uncovered.size} preguntas no están en ningún bloque")
                null
            }
            nonEmpty > examSize -> {
                onError("Bloques ignorados: hay $nonEmpty bloques y el simulacro tiene $examSize preguntas")
                null
            }
            else -> blocks
        }
    }

    private fun String.normalizeSpaces(): String = trim().replace(Regex("\\s+"), " ")
}
