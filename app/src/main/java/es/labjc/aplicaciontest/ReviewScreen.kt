package es.labjc.aplicaciontest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Repaso de preguntas falladas. Cada respuesta cuenta: una pregunta sale del
 * repaso al acertarla [QuestionStat.REVIEW_STREAK_TO_MASTER] veces seguidas.
 */
@Composable
fun ReviewScreen(testViewModel: TestViewModel, onExit: () -> Unit) {
    // Lista fija para esta ronda (números de pregunta), aunque cambien las estadísticas al responder.
    var round by rememberSaveable { mutableStateOf(reviewNumbers(testViewModel)) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedAnswer by rememberSaveable { mutableStateOf<Char?>(null) }
    val answered = selectedAnswer != null

    val byNumber = remember(testViewModel) { testViewModel.questions.associateBy { it.number } }

    if (round.isEmpty()) {
        AlertDialog(
            onDismissRequest = onExit,
            title = { Text("No hay repaso") },
            text = { Text("No tienes preguntas pendientes de repasar. ¡Bien hecho!") },
            confirmButton = {
                Button(onClick = onExit) { Text("Volver a Inicio") }
            }
        )
        return
    }

    val question = round.getOrNull(currentIndex)?.let { byNumber[it] }
    if (question == null) {
        val pending = testViewModel.pendingReviewCount.value
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Repaso finalizado") },
            text = {
                Text(
                    if (pending == 0) "Has dominado todas las preguntas del repaso."
                    else "Te quedan $pending preguntas pendientes de repaso. ¿Qué deseas hacer?"
                )
            },
            confirmButton = {
                if (pending > 0) {
                    Button(onClick = {
                        round = reviewNumbers(testViewModel)
                        currentIndex = 0
                        selectedAnswer = null
                    }) { Text("Otra ronda") }
                }
            },
            dismissButton = {
                Button(onClick = onExit) { Text("Volver a Inicio") }
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        QuestionContent(
            title = "Pregunta ${question.number}",
            question = question,
            selectedAnswer = selectedAnswer,
            showResult = answered,
            enabled = !answered,
            progress = (currentIndex + 1).toFloat() / round.size,
            onSelect = { letter ->
                testViewModel.recordAnswer(question, letter)
                selectedAnswer = letter
            }
        )
        Text(
            text = "Repaso: ${currentIndex + 1} de ${round.size}",
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    currentIndex++
                    selectedAnswer = null
                },
                enabled = answered,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Siguiente pregunta")
            }
            Button(onClick = onExit, modifier = Modifier.padding(8.dp)) {
                Text("Salir")
            }
        }
    }
}

private fun reviewNumbers(testViewModel: TestViewModel): ArrayList<Int> =
    ArrayList(testViewModel.getReviewQuestions().map { it.number })
