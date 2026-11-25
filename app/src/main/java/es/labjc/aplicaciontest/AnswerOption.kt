package es.labjc.aplicaciontest

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.labjc.aplicaciontest.ui.LabjcColors

val CorrectColor = Color(0xFFA5D6A7)
val WrongColor = Color(0xFFEF9A9A)
val SelectedColor = Color(0xFF90CAF9)

/** Borde de las opciones sin marcar: separa la tarjeta blanca del fondo claro. */
private val OptionBorderColor = Color(0xFFB7CCE3)

/**
 * Una opción de respuesta: tarjeta blanca con borde y la letra en un círculo azul.
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
    val borderColor = when {
        showResult && isCorrectOption -> LabjcColors.Correct
        showResult && isSelected -> LabjcColors.Wrong
        isSelected -> LabjcColors.Blue
        else -> OptionBorderColor
    }
    val badgeColor = when {
        showResult && isCorrectOption -> LabjcColors.Correct
        showResult && isSelected -> LabjcColors.Wrong
        else -> LabjcColors.Blue
    }
    val scale by animateFloatAsState(
        targetValue = if (showResult && isCorrectOption) 1.03f else 1.0f,
        label = "answerScale"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected || (showResult && isCorrectOption)) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .fillMaxWidth()
            .scale(scale)
            .clickable(enabled = enabled, onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            ) {
                Text(
                    text = optionLetter.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = optionText,
                color = LabjcColors.Navy,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
