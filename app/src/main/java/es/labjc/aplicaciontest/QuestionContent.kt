package es.labjc.aplicaciontest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.labjc.aplicaciontest.data.Question

/**
 * Enunciado + las cuatro opciones + barra de progreso.
 * Lo comparten el test, el modo aleatorio, el repaso y el simulacro.
 *
 * @param progress valor entre 0 y 1 para la barra; null para no mostrarla.
 * @param showReport muestra "Reportar error en esta pregunta" (en el simulacro no).
 */
@Composable
fun QuestionContent(
    title: String,
    question: Question,
    selectedAnswer: Char?,
    showResult: Boolean,
    enabled: Boolean,
    progress: Float?,
    showReport: Boolean = true,
    onSelect: (Char) -> Unit
) {
    Text(
        text = "$title: ${question.text}",
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        question.options.forEach { (letter, text) ->
            AnswerOption(
                optionLetter = letter,
                optionText = text,
                isSelected = selectedAnswer == letter,
                correctAnswer = question.correctAnswer,
                showResult = showResult,
                enabled = enabled,
                onSelect = { onSelect(letter) }
            )
        }
    }
    if (showReport) {
        ReportErrorButton(question)
    }
    if (progress != null) {
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
    }
}
