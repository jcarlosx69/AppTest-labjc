package es.labjc.aplicaciontest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.labjc.aplicaciontest.ui.LabjcColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(testViewModel: TestViewModel, onHome: () -> Unit) {
    val version by testViewModel.statsVersion.collectAsState()
    val history by testViewModel.examHistory.collectAsState()
    val stats = remember(version) { testViewModel.globalStats() }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Estadísticas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 48.dp, bottom = 8.dp)
            )
        }

        item {
            StatsCard {
                StatRow("Preguntas vistas", "${stats.seenQuestions} de ${stats.totalQuestions}")
                StatRow("Respuestas dadas", "${stats.answers}")
                StatRow("Aciertos", "${stats.correctAnswers} (${stats.percentCorrect} %)", LabjcColors.Correct)
                StatRow("Fallos", "${stats.wrongAnswers}", LabjcColors.Wrong)
                StatRow("Pendientes de repaso", "${stats.pendingReview}")
                StatRow("Falladas y ya dominadas", "${stats.mastered}")
            }
        }

        item { SectionTitle("Simulacros") }
        item {
            StatsCard {
                if (history.isEmpty()) {
                    Text("Aún no has hecho ningún simulacro.")
                } else {
                    StatRow("Simulacros hechos", "${history.size}")
                    StatRow("Nota media", "${formatScore(history.map { it.score }.average())} / 10")
                    StatRow("Mejor nota", "${formatScore(history.maxOf { it.score })} / 10")
                    Text("Últimos:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    history.take(5).forEach { record ->
                        StatRow(dateFormat.format(Date(record.timestampMillis)), "${formatScore(record.score)} / 10")
                    }
                }
            }
        }

        item { SectionTitle("Preguntas más falladas") }
        if (stats.mostFailed.isEmpty()) {
            item {
                StatsCard {
                    Text("Aún no tienes fallos registrados. Completa algún test para ver aquí las preguntas que necesitas repasar.")
                }
            }
        }
        items(stats.mostFailed) { (question, stat) ->
            StatsCard {
                Text(
                    text = "Pregunta nº ${question.number}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(question.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${stat.wrong} fallos · ${stat.correct} aciertos",
                    fontSize = 13.sp,
                    color = LabjcColors.Wrong
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onHome) { Text("Volver al inicio") }
                OutlinedButton(onClick = { showResetDialog = true }) {
                    Text("Reiniciar todo", color = LabjcColors.Wrong)
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("¿Reiniciar el test?") },
            text = {
                Text("Se perderá todo el progreso actual, incluyendo estadísticas, preguntas falladas e historial de simulacros. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        testViewModel.resetEverything()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LabjcColors.Wrong)
                ) { Text("Sí, reiniciar") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun StatsCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
}
