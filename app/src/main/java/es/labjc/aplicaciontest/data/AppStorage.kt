package es.labjc.aplicaciontest.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

/** Resumen de un simulacro ya entregado, para el historial. */
data class ExamRecord(
    val timestampMillis: Long = 0L,
    val total: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val blank: Int = 0,
    val elapsedSeconds: Long = 0L
) {
    /** Nota sobre 10, sin penalización por fallos. */
    val score: Double
        get() = if (total == 0) 0.0 else correct * 10.0 / total
}

/**
 * Todo lo que la app guarda en el dispositivo, en un solo sitio.
 *
 * Dos ficheros de SharedPreferences: "test_prefs" (progreso del test) y
 * "TestStats" (estadísticas por pregunta, contadores e historial de simulacros).
 * Si se cambian los nombres o las claves, quien actualice la app pierde sus datos.
 */
class AppStorage(context: Context) {

    private val progressPrefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
    private val statsPrefs = context.getSharedPreferences("TestStats", Context.MODE_PRIVATE)
    private val gson = Gson()

    // --- Test secuencial ---

    var progress: Int
        get() = progressPrefs.getInt(KEY_PROGRESS, 0)
        set(value) = progressPrefs.edit().putInt(KEY_PROGRESS, value).apply()

    var sessionCorrect: Int
        get() = statsPrefs.getInt(KEY_SESSION_CORRECT, 0)
        set(value) = statsPrefs.edit().putInt(KEY_SESSION_CORRECT, value).apply()

    var sessionWrong: Int
        get() = statsPrefs.getInt(KEY_SESSION_WRONG, 0)
        set(value) = statsPrefs.edit().putInt(KEY_SESSION_WRONG, value).apply()

    // --- Estadísticas por pregunta (alimentan el Modo Repaso) ---

    fun loadStats(): MutableMap<Int, QuestionStat> {
        val json = statsPrefs.getString(KEY_STATS, null) ?: return mutableMapOf()
        val type = object : TypeToken<Map<Int, QuestionStat>>() {}.type
        return try {
            gson.fromJson<Map<Int, QuestionStat>>(json, type)?.toMutableMap() ?: mutableMapOf()
        } catch (e: JsonParseException) {
            mutableMapOf()
        }
    }

    fun saveStats(stats: Map<Int, QuestionStat>) {
        statsPrefs.edit().putString(KEY_STATS, gson.toJson(stats)).apply()
    }

    // --- Historial de simulacros ---

    fun loadExamHistory(): List<ExamRecord> {
        val json = statsPrefs.getString(KEY_EXAM_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<ExamRecord>>() {}.type
        return try {
            gson.fromJson<List<ExamRecord>>(json, type) ?: emptyList()
        } catch (e: JsonParseException) {
            emptyList()
        }
    }

    /** Añade un simulacro al principio del historial y conserva los [MAX_EXAM_HISTORY] más recientes. */
    fun addExamRecord(record: ExamRecord): List<ExamRecord> {
        val updated = (listOf(record) + loadExamHistory()).take(MAX_EXAM_HISTORY)
        statsPrefs.edit().putString(KEY_EXAM_HISTORY, gson.toJson(updated)).apply()
        return updated
    }

    /** Borra progreso, estadísticas e historial. */
    fun clearAll() {
        progressPrefs.edit().clear().apply()
        statsPrefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_PROGRESS = "progress"
        const val KEY_STATS = "questionStats"
        const val KEY_SESSION_CORRECT = "sessionCorrect"
        const val KEY_SESSION_WRONG = "sessionWrong"
        const val KEY_EXAM_HISTORY = "examHistory"
        const val MAX_EXAM_HISTORY = 20
    }
}
