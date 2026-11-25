package es.labjc.aplicaciontest.exam

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.labjc.aplicaciontest.QuestionContent
import es.labjc.aplicaciontest.SelectedColor
import es.labjc.aplicaciontest.TestViewModel
import kotlinx.coroutines.delay

/**
 * Simulacro "como examen real": no se muestra si aciertas, se puede dejar en
 * blanco, ir adelante y atrás y cambiar respuestas. Se corrige al entregar o
 * cuando se acaba el tiempo.
 */
@Composable
fun ExamScreen(
    testViewModel: TestViewModel,
    onFinished: () -> Unit,
    onAbandoned: () -> Unit
) {
    val session by testViewModel.exam.collectAsState()
    val exam = session
    if (exam == null) {
        // Sin simulacro activo (p. ej. Android cerró la app en segundo plano): volvemos al inicio.
        LaunchedEffect(Unit) { onAbandoned() }
        return
    }

    var showSubmitDialog by rememberSaveable { mutableStateOf(false) }
    var showAbandonDialog by rememberSaveable { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Temporizador: al llegar a 0 se entrega automáticamente.
    LaunchedEffect(exam.startedAtMillis) {
        while (true) {
            now = System.currentTimeMillis()
            if (exam.remainingMillis(now) <= 0L) {
                testViewModel.finishExam(timedOut = true)
                onFinished()
                break
            }
            delay(1_000L)
        }
    }

    BackHandler { showAbandonDialog = true }

    val index = exam.currentIndex
    val question = exam.questions[index]
    val selected = exam.answers[index]
    val remaining = exam.remainingMillis(now)
    val chipsState = rememberLazyListState()

    LaunchedEffect(index) {
        chipsState.animateScrollToItem((index - 3).coerceAtLeast(0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Simulacro de examen", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "⏱ ${formatDuration(remaining)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (remaining < 5 * 60_000L) Color(0xFFC62828) else Color.Unspecified
            )
        }
        Text(
            text = "Respondidas ${exam.answeredCount} de ${exam.questions.size} · En blanco ${exam.blankCount}",
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )

        // Índice de preguntas: rellenas = respondidas; borde grueso = la actual.
        LazyRow(
            state = chipsState,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(exam.questions.size) { i ->
                QuestionChip(
                    number = i + 1,
                    answered = exam.answers.containsKey(i),
                    current = i == index,
                    onClick = { testViewModel.goToExamQuestion(i) }
                )
            }
        }

        Text(
            text = ExamBlocks.blockOf(question.number, exam.blocks)?.name.orEmpty(),
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        QuestionContent(
            title = "Pregunta ${index + 1} de ${exam.questions.size}",
            question = question,
            selectedAnswer = selected,
            showResult = false,
            enabled = true,
            progress = null,
            showReport = false,
            onSelect = { testViewModel.toggleExamAnswer(it) }
        )

        // Pulsar otra vez la opción marcada también la deja en blanco.
        TextButton(
            onClick = { selected?.let { testViewModel.toggleExamAnswer(it) } },
            enabled = selected != null
        ) {
            Text("Dejar en blanco")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { testViewModel.goToExamQuestion(index - 1) },
                enabled = index > 0
            ) { Text("Anterior") }
            OutlinedButton(
                onClick = { testViewModel.goToExamQuestion(index + 1) },
                enabled = index < exam.questions.lastIndex
            ) { Text("Siguiente") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showSubmitDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) { Text("Entregar") }
            Button(
                onClick = { showAbandonDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
            ) { Text("Abandonar") }
        }
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Entregar simulacro") },
            text = {
                Text(
                    if (exam.blankCount > 0) "Tienes ${exam.blankCount} preguntas en blanco. ¿Quieres entregar igualmente?"
                    else "Has respondido todas las preguntas. ¿Entregar el simulacro?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showSubmitDialog = false
                    testViewModel.finishExam(timedOut = false)
                    onFinished()
                }) { Text("Entregar") }
            },
            dismissButton = {
                Button(onClick = { showSubmitDialog = false }) { Text("Seguir") }
            }
        )
    }

    if (showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            title = { Text("Abandonar simulacro") },
            text = { Text("Si sales ahora, el simulacro no se corrige ni se guarda. ¿Abandonar?") },
            confirmButton = {
                Button(onClick = {
                    showAbandonDialog = false
                    testViewModel.abandonExam()
                    onAbandoned()
                }) { Text("Abandonar") }
            },
            dismissButton = {
                Button(onClick = { showAbandonDialog = false }) { Text("Seguir") }
            }
        )
    }
}

@Composable
private fun QuestionChip(number: Int, answered: Boolean, current: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (answered) SelectedColor else Color.White)
            .border(
                width = if (current) 3.dp else 1.dp,
                color = if (current) Color(0xFF1565C0) else Color.Gray,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Text(text = "$number", fontSize = 12.sp)
    }
}

/** Formato mm:ss. */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis + 999) / 1000
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
