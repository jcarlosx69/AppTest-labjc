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

@Composable
fun RandomTestScreen(testViewModel: TestViewModel, onExit: () -> Unit) {
    // Se guarda el orden (números de pregunta) para que sobreviva a un giro de pantalla.
    var order by rememberSaveable { mutableStateOf(shuffledNumbers(testViewModel)) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedAnswer by rememberSaveable { mutableStateOf<Char?>(null) }
    val answered = selectedAnswer != null

    val byNumber = remember(testViewModel) { testViewModel.questions.associateBy { it.number } }
    val question = order.getOrNull(currentIndex)?.let { byNumber[it] }

    if (question == null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Test aleatorio finalizado") },
            text = { Text("Has completado el test aleatorio. ¿Qué deseas hacer?") },
            confirmButton = {
                Button(onClick = onExit) { Text("Salir") }
            },
            dismissButton = {
                Button(onClick = {
                    order = shuffledNumbers(testViewModel)
                    currentIndex = 0
                    selectedAnswer = null
                }) { Text("Reiniciar") }
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
            progress = (currentIndex + 1).toFloat() / order.size,
            onSelect = { letter ->
                testViewModel.recordAnswer(question, letter)
                selectedAnswer = letter
            }
        )
        Text(
            text = "${currentIndex + 1} de ${order.size}",
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

private fun shuffledNumbers(testViewModel: TestViewModel): ArrayList<Int> =
    ArrayList(testViewModel.questions.map { it.number }.shuffled())
