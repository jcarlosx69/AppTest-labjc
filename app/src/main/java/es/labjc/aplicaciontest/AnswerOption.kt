package es.labjc.aplicaciontest

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val CorrectColor = Color(0xFFA5D6A7)
val WrongColor = Color(0xFFEF9A9A)
val SelectedColor = Color(0xFF90CAF9)

/**
 * Una opción de respuesta.
 *
 * @param showResult si es true se colorea la correcta en verde y la elegida (si es
 *   incorrecta) en rojo. En el simulacro va a false: solo se resalta la elegida.
 */
@Composable
fun AnswerOption(
    optionLetter: Char,
    optionText: String,
    isSelected: Boolean,
    correctAnswer: Char,
    showResult: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val isCorrectOption = optionLetter == correctAnswer
    val backgroundColor = when {
        showResult && isCorrectOption -> CorrectColor
        showResult && isSelected -> WrongColor
        !showResult && isSelected -> SelectedColor
        else -> Color.White
    }
    val scale by animateFloatAsState(
        targetValue = if (showResult && isCorrectOption) 1.05f else 1.0f,
        label = "answerScale"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .scale(scale)
            .clickable(enabled = enabled, onClick = onSelect)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "$optionLetter)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}
