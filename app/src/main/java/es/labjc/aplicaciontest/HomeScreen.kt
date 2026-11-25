package es.labjc.aplicaciontest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.labjc.aplicaciontest.exam.ExamGenerator
import es.labjc.aplicaciontest.ui.LabjcColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pantalla de inicio: fondo azul labjc, logo y los modos de la app. */
@Composable
fun HomeScreen(
    testViewModel: TestViewModel,
    onContinueTest: () -> Unit,
    onNewTest: () -> Unit,
    onStartAt: (index: Int) -> Unit,
    onReview: () -> Unit,
    onRandomMode: () -> Unit,
    onExam: () -> Unit,
    onStats: () -> Unit,
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    val currentIndex by testViewModel.currentIndex.collectAsState()
    val pendingReview by testViewModel.pendingReviewCount.collectAsState()
    val examHistory by testViewModel.examHistory.collectAsState()
    val totalQuestions = testViewModel.questions.size
    val examSize = minOf(ExamGenerator.EXAM_SIZE, totalQuestions)

    val primaryColors = ButtonDefaults.buttonColors(containerColor = LabjcColors.Cyan, contentColor = Color.White)
    val whiteColors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LabjcColors.Navy)
    val wide = Modifier.fillMaxWidth()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(LabjcColors.Navy, LabjcColors.Blue)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_labjc),
                contentDescription = "Logo labjc",
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }

        Text(
            text = testViewModel.testData.title.ifEmpty { "Test" },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "$totalQuestions preguntas disponibles",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 15.sp
        )

        if (totalQuestions == 0) {
            Text(
                text = "No hay preguntas disponibles.",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Spacer(Modifier.height(4.dp))

            // --- Test secuencial ---
            if (currentIndex > 0) {
                Text(
                    text = "Progreso guardado: pregunta ${currentIndex + 1} de $totalQuestions",
                    color = Color.White,
                    fontSize = 15.sp
                )
                Row(modifier = wide, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onContinueTest, colors = primaryColors, modifier = Modifier.weight(1f)) {
                        Text("Continuar Test")
                    }
                    OutlinedButton(onClick = onNewTest, modifier = Modifier.weight(1f)) {
                        Text("Iniciar nuevo", color = Color.White)
                    }
                }
            } else {
                Button(onClick = onNewTest, colors = primaryColors, modifier = wide) {
                    Text("Iniciar Test")
                }
            }
            SelectStartButton(
                totalQuestions = totalQuestions,
                onSelect = onStartAt,
                modifier = wide,
                colors = whiteColors
            )

            // --- Otros modos ---
            Row(modifier = wide, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onReview, colors = whiteColors, modifier = Modifier.weight(1f)) {
                    Text(if (pendingReview > 0) "Modo Repaso ($pendingReview)" else "Modo Repaso")
                }
                Button(onClick = onRandomMode, colors = whiteColors, modifier = Modifier.weight(1f)) {
                    Text("Aleatorio")
                }
            }

            Button(onClick = onExam, colors = primaryColors, modifier = wide) {
                Text("Simulacro de examen ($examSize preguntas)")
            }
            examHistory.firstOrNull()?.let { last ->
                val date = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(Date(last.timestampMillis))
                Text(
                    text = "Último simulacro ($date): ${formatScore(last.score)} / 10",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }

            Button(onClick = onStats, colors = whiteColors, modifier = wide) {
                Text("Estadísticas")
            }
            OutlinedButton(onClick = onExitApp, modifier = wide) {
                Text("Salir", color = Color.White)
            }
        }

        // --- Pie ---
        Spacer(Modifier.height(16.dp))
        Text(
            text = "labjc.es · De la madera al código",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp
        )
        Text(
            text = CONTACT_EMAIL,
            color = LabjcColors.Cyan,
            fontSize = 14.sp,
            modifier = Modifier.clickable { sendEmail(context, subject = testViewModel.testData.title) }
        )
    }
}

/** Nota con una cifra decimal y coma, p. ej. "7,4". */
fun formatScore(score: Double): String = String.format(Locale("es", "ES"), "%.1f", score)
