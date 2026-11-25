package es.labjc.aplicaciontest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Resumen del test secuencial en curso (desde que se pulsó "Iniciar"). */
@Composable
fun SummaryScreen(testViewModel: TestViewModel, onHome: () -> Unit, onExitApp: () -> Unit) {
    val correctCount by testViewModel.correctCount.collectAsState()
    val wrongCount by testViewModel.wrongCount.collectAsState()
    val currentIndex by testViewModel.currentIndex.collectAsState()
    val answeredCount = correctCount + wrongCount
    val percent = if (answeredCount == 0) 0 else correctCount * 100 / answeredCount

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Resumen del test", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Vas por la pregunta ${currentIndex + 1} de ${testViewModel.questions.size}", fontSize = 18.sp)
        Text(text = "Respondidas: $answeredCount", fontSize = 20.sp)
        Text(text = "Correctas: $correctCount", fontSize = 20.sp, color = Color(0xFF2E7D32))
        Text(text = "Incorrectas: $wrongCount", fontSize = 20.sp, color = Color(0xFFC62828))
        Text(text = "Acierto: $percent %", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onHome) { Text("Volver al inicio") }
            Button(onClick = onExitApp) { Text("Salir") }
        }
    }
}
