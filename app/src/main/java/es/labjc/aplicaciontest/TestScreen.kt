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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TestScreen(testViewModel: TestViewModel, onExit: () -> Unit) {
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }

    val currentIndex by testViewModel.currentIndex.collectAsState()
    val selectedAnswer by testViewModel.selectedAnswer.collectAsState()
    val answered by testViewModel.answered.collectAsState()
    val total = testViewModel.questions.size
    val question = testViewModel.questions.getOrNull(currentIndex)
    val isLast = currentIndex >= total - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        if (question == null) {
            Text("No hay preguntas disponibles.")
        } else {
            QuestionContent(
                title = "Pregunta ${currentIndex + 1} de $total",
                question = question,
                selectedAnswer = selectedAnswer,
                showResult = answered,
                enabled = !answered,
                progress = (currentIndex + 1).toFloat() / total,
                onSelect = { testViewModel.selectAnswer(it) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (isLast) showRestartDialog = true else testViewModel.nextQuestion()
                },
                enabled = answered,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(if (isLast) "Terminar" else "Siguiente pregunta")
            }
            Button(onClick = { showExitDialog = true }, modifier = Modifier.padding(8.dp)) {
                Text("Salir")
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Confirmar salida") },
            text = { Text("Tu progreso queda guardado. ¿Quieres salir del test?") },
            confirmButton = {
                Button(onClick = {
                    showExitDialog = false
                    onExit()
                }) { Text("Sí") }
            },
            dismissButton = {
                Button(onClick = { showExitDialog = false }) { Text("No") }
            }
        )
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Test completado") },
            text = {
                Text("Has contestado la última pregunta. Tus fallos siguen disponibles en el Modo Repaso. ¿Qué quieres hacer?")
            },
            confirmButton = {
                Button(onClick = {
                    showRestartDialog = false
                    onExit()
                }) { Text("Ver resumen") }
            },
            dismissButton = {
                Button(onClick = {
                    showRestartDialog = false
                    testViewModel.startNewTest()
                }) { Text("Empezar de nuevo") }
            }
        )
    }
}
