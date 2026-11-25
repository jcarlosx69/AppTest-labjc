package es.labjc.aplicaciontest

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import es.labjc.aplicaciontest.exam.ExamGenerator
import es.labjc.aplicaciontest.exam.ExamResult
import es.labjc.aplicaciontest.exam.ExamSession
import es.labjc.aplicaciontest.data.AppStorage
import es.labjc.aplicaciontest.data.ExamRecord
import es.labjc.aplicaciontest.data.Question
import es.labjc.aplicaciontest.data.QuestionRepository
import es.labjc.aplicaciontest.data.QuestionStat
import es.labjc.aplicaciontest.data.TestData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = AppStorage(application)

    /** Banco de preguntas de la categoría (assets/questions.json de cada variante). */
    val testData: TestData = loadTestData(application)

    val questions: List<Question> get() = testData.questions

    private val questionsByNumber: Map<Int, Question> = questions.associateBy { it.number }

    // ---------------------------------------------------------------------
    // Test secuencial
    // ---------------------------------------------------------------------

    private val _currentIndex = MutableStateFlow(clampIndex(storage.progress))
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<Char?>(null)
    val selectedAnswer: StateFlow<Char?> = _selectedAnswer.asStateFlow()

    private val _answered = MutableStateFlow(false)
    val answered: StateFlow<Boolean> = _answered.asStateFlow()

    // Aciertos y fallos del test secuencial en curso (se guardan junto al progreso).
    private val _correctCount = MutableStateFlow(storage.sessionCorrect)
    val correctCount: StateFlow<Int> = _correctCount.asStateFlow()

    private val _wrongCount = MutableStateFlow(storage.sessionWrong)
    val wrongCount: StateFlow<Int> = _wrongCount.asStateFlow()

    /** Continúa el test en la pregunta indicada (posición empezando en 0). */
    fun startTestAt(index: Int) {
        setIndex(clampIndex(index))
    }

    /** Empieza el test desde la primera pregunta. No toca el historial del repaso. */
    fun startNewTest() {
        setIndex(0)
        _correctCount.value = 0
        _wrongCount.value = 0
        storage.sessionCorrect = 0
        storage.sessionWrong = 0
    }

    fun selectAnswer(answer: Char) {
        if (_answered.value) return
        val question = questions.getOrNull(_currentIndex.value) ?: return
        _selectedAnswer.value = answer
        _answered.value = true
        if (recordAnswer(question, answer)) {
            storage.sessionCorrect = ++_correctCount.value
        } else {
            storage.sessionWrong = ++_wrongCount.value
        }
    }

    fun nextQuestion() {
        if (_currentIndex.value < questions.lastIndex) {
            setIndex(_currentIndex.value + 1)
        }
    }

    private fun setIndex(index: Int) {
        _currentIndex.value = index
        storage.progress = index
        _selectedAnswer.value = null
        _answered.value = false
    }

    private fun clampIndex(index: Int): Int = index.coerceIn(0, (questions.size - 1).coerceAtLeast(0))

    // ---------------------------------------------------------------------
    // Estadísticas y Modo Repaso
    // ---------------------------------------------------------------------

    private val stats: MutableMap<Int, QuestionStat> = storage.loadStats()

    private val _pendingReviewCount = MutableStateFlow(countPendingReview())
    val pendingReviewCount: StateFlow<Int> = _pendingReviewCount.asStateFlow()

    /**
     * Registra una respuesta en las estadísticas de la pregunta y las guarda.
     * Lo usan todos los modos. Devuelve true si la respuesta es correcta.
     */
    fun recordAnswer(question: Question, answer: Char?): Boolean {
        val isCorrect = question.isCorrect(answer)
        stats.getOrPut(question.number) { QuestionStat() }.register(isCorrect)
        storage.saveStats(stats)
        onStatsChanged()
        return isCorrect
    }

    /**
     * Preguntas pendientes de repaso: falladas y todavía sin
     * [QuestionStat.REVIEW_STREAK_TO_MASTER] aciertos seguidos. Las más falladas primero.
     */
    fun getReviewQuestions(): List<Question> =
        stats.filterValues { it.needsReview }
            .entries
            .sortedByDescending { it.value.wrong }
            .mapNotNull { questionsByNumber[it.key] }

    private fun countPendingReview(): Int =
        stats.count { (number, stat) -> stat.needsReview && number in questionsByNumber }

    // ---------------------------------------------------------------------
    // Simulacro de examen
    // ---------------------------------------------------------------------

    private val _exam = MutableStateFlow<ExamSession?>(null)
    val exam: StateFlow<ExamSession?> = _exam.asStateFlow()

    private val _lastExamResult = MutableStateFlow<ExamResult?>(null)
    val lastExamResult: StateFlow<ExamResult?> = _lastExamResult.asStateFlow()

    private val _examHistory = MutableStateFlow(storage.loadExamHistory())
    val examHistory: StateFlow<List<ExamRecord>> = _examHistory.asStateFlow()

    fun startExam() {
        val examQuestions = ExamGenerator.generate(questions, testData.blocks)
        _exam.value = ExamSession(
            questions = examQuestions,
            blocks = testData.blocks,
            startedAtMillis = System.currentTimeMillis(),
            durationMillis = examQuestions.size * MILLIS_PER_EXAM_QUESTION
        )
    }

    /** Marca una respuesta; si se pulsa la ya marcada, la pregunta vuelve a quedar en blanco. */
    fun toggleExamAnswer(answer: Char) {
        val session = _exam.value ?: return
        val index = session.currentIndex
        val newAnswers = if (session.answers[index] == answer) {
            session.answers - index
        } else {
            session.answers + (index to answer)
        }
        _exam.value = session.copy(answers = newAnswers)
    }

    fun goToExamQuestion(index: Int) {
        val session = _exam.value ?: return
        _exam.value = session.copy(currentIndex = index.coerceIn(0, session.questions.lastIndex))
    }

    /**
     * Corrige el simulacro, pasa los fallos y las preguntas en blanco al Modo Repaso,
     * lo añade al historial y deja el resultado en [lastExamResult].
     */
    fun finishExam(timedOut: Boolean) {
        val session = _exam.value ?: return
        val result = ExamResult.from(session, System.currentTimeMillis(), timedOut)

        result.items.forEach { item ->
            stats.getOrPut(item.question.number) { QuestionStat() }.register(item.isCorrect)
        }
        storage.saveStats(stats)
        onStatsChanged()

        _examHistory.value = storage.addExamRecord(
            ExamRecord(
                timestampMillis = System.currentTimeMillis(),
                total = result.total,
                correct = result.correct,
                wrong = result.wrong,
                blank = result.blank,
                elapsedSeconds = result.elapsedMillis / 1000
            )
        )
        _lastExamResult.value = result
        _exam.value = null
    }

    /** Abandona el simulacro sin corregirlo ni guardar nada. */
    fun abandonExam() {
        _exam.value = null
    }

    // ---------------------------------------------------------------------
    // Estadísticas
    // ---------------------------------------------------------------------

    /** Cambia cada vez que se guardan estadísticas, para que la pantalla se recalcule. */
    private val _statsVersion = MutableStateFlow(0)
    val statsVersion: StateFlow<Int> = _statsVersion.asStateFlow()

    fun globalStats(): GlobalStats {
        val known = stats.filterKeys { it in questionsByNumber }
        return GlobalStats(
            totalQuestions = questions.size,
            seenQuestions = known.size,
            correctAnswers = known.values.sumOf { it.correct },
            wrongAnswers = known.values.sumOf { it.wrong },
            pendingReview = known.values.count { it.needsReview },
            mastered = known.values.count { it.wrong > 0 && !it.needsReview },
            mostFailed = known.entries
                .filter { it.value.wrong > 0 }
                .sortedWith(compareByDescending<Map.Entry<Int, QuestionStat>> { it.value.wrong }.thenBy { it.key })
                .take(MOST_FAILED_LIMIT)
                .map { (number, stat) -> questionsByNumber.getValue(number) to stat }
        )
    }

    /** Borra todo: progreso, estadísticas, repaso e historial de simulacros. */
    fun resetEverything() {
        stats.clear()
        storage.clearAll()
        _examHistory.value = emptyList()
        _lastExamResult.value = null
        _correctCount.value = 0
        _wrongCount.value = 0
        setIndex(0)
        onStatsChanged()
    }

    private fun onStatsChanged() {
        _pendingReviewCount.value = countPendingReview()
        _statsVersion.value++
    }

    companion object {
        private const val TAG = "TestViewModel"
        /** Un minuto por pregunta: 50 preguntas, 50 minutos. */
        const val MILLIS_PER_EXAM_QUESTION = 60_000L
        private const val MOST_FAILED_LIMIT = 10

        private fun loadTestData(application: Application): TestData {
            val json = try {
                application.assets.open(QuestionRepository.ASSET_FILE).bufferedReader(Charsets.UTF_8).use { it.readText() }
            } catch (e: java.io.IOException) {
                Log.e(TAG, "No se pudo leer ${QuestionRepository.ASSET_FILE}", e)
                "{}"
            }
            return QuestionRepository.parse(json) { Log.w(TAG, it) }
        }
    }
}

/** Resumen global para la pantalla de estadísticas. */
data class GlobalStats(
    val totalQuestions: Int,
    val seenQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val pendingReview: Int,
    val mastered: Int,
    val mostFailed: List<Pair<Question, QuestionStat>>
) {
    val answers: Int get() = correctAnswers + wrongAnswers
    val percentCorrect: Int get() = if (answers == 0) 0 else correctAnswers * 100 / answers
}
