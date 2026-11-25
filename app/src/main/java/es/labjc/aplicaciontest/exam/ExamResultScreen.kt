package es.labjc.aplicaciontest.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.labjc.aplicaciontest.ReportErrorButton
import es.labjc.aplicaciontest.TestViewModel
import es.labjc.aplicaciontest.formatScore

@Composable
fun ExamResultScreen(
    testViewModel: TestViewModel,
    onReview: () -> Unit,
    onHome: () -> Unit
) {
    val lastResult by testViewModel.lastExamResult.collectAsState()
    val result = lastResult
    if (result == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No hay ningún simulacro corregido.")
            Button(onClick = onHome) { Text("Volver al inicio") }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Resultado del simulacro", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${formatScore(result.score)} / 10",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.score >= 5.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Text(
                    text = "Aciertos ${result.correct} · Fallos ${result.wrong} · En blanco ${result.blank}",
                    fontSize = 16.sp
                )
                Text(
                    text = "Tiempo: ${formatDuration(result.elapsedMillis)} de ${formatDuration(result.durationMillis)}" +
                        if (result.timedOut) " (tiempo agotado)" else "",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (result.toReview.isEmpty()) {
                        "¡Pleno! No hay preguntas que repasar."
                    } else {
                        "${result.toReview.size} preguntas (fallos y en blanco) se han añadido al Modo Repaso."
                    },
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (result.toReview.isNotEmpty()) {
                        Button(
                            onClick = onReview,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) { Text("Repasar ahora") }
                    }
                    Button(onClick = onHome) { Text("Volver al inicio") }
                }
            }
        }

        item { SectionTitle("Resultado por bloques") }
        items(result.byBlock) { block ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(block.blockName, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(
                    text = "${block.correct}/${block.total}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (block.correct == block.total) Color(0xFF2E7D32) else Color.Unspecified
                )
            }
        }

        if (result.toReview.isNotEmpty()) {
            item { SectionTitle("Preguntas falladas y en blanco") }
            items(result.toReview) { item -> ReviewItemCard(item) }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
private fun ReviewItemCard(item: ExamItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Pregunta ${item.position} (nº ${item.question.number} del banco)",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(item.question.text, fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
            val answer = item.answer
            Text(
                text = if (answer == null) "Tu respuesta: en blanco"
                else "Tu respuesta: $answer) ${item.question.optionText(answer)}",
                fontSize = 14.sp,
                color = if (answer == null) Color.Gray else Color(0xFFC62828)
            )
            Text(
                text = "Correcta: ${item.question.correctAnswer}) ${item.question.optionText(item.question.correctAnswer)}",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32)
            )
            ReportErrorButton(item.question)
        }
    }
}
