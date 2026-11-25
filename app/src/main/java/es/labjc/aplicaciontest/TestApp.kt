package es.labjc.aplicaciontest

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import es.labjc.aplicaciontest.exam.ExamResultScreen
import es.labjc.aplicaciontest.exam.ExamScreen
import es.labjc.aplicaciontest.ui.LabjcColors
import es.labjc.aplicaciontest.ui.LabjcTheme

enum class Screen {
    Home, Test, Random, Review, Summary, Exam, ExamResult, Stats
}

@Composable
fun TestApp(
    onExitApp: () -> Unit,
    testViewModel: TestViewModel = viewModel()
) {
    // rememberSaveable: al girar la pantalla no se vuelve al inicio.
    var currentScreen by rememberSaveable { mutableStateOf(Screen.Home) }

    // El botón "atrás" del sistema vuelve al inicio en lugar de cerrar la app.
    // (El simulacro tiene su propio BackHandler, que pide confirmación.)
    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = Screen.Home
    }

    LabjcTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = LabjcColors.Background
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    testViewModel = testViewModel,
                    onContinueTest = { currentScreen = Screen.Test },
                    onNewTest = {
                        testViewModel.startNewTest()
                        currentScreen = Screen.Test
                    },
                    onStartAt = { index ->
                        testViewModel.startTestAt(index)
                        currentScreen = Screen.Test
                    },
                    onReview = { currentScreen = Screen.Review },
                    onRandomMode = { currentScreen = Screen.Random },
                    onExam = {
                        testViewModel.startExam()
                        currentScreen = Screen.Exam
                    },
                    onStats = { currentScreen = Screen.Stats },
                    onExitApp = onExitApp
                )
                Screen.Test -> TestScreen(testViewModel, onExit = { currentScreen = Screen.Summary })
                Screen.Random -> RandomTestScreen(testViewModel, onExit = { currentScreen = Screen.Home })
                Screen.Review -> ReviewScreen(testViewModel, onExit = { currentScreen = Screen.Home })
                Screen.Summary -> SummaryScreen(
                    testViewModel,
                    onHome = { currentScreen = Screen.Home },
                    onExitApp = onExitApp
                )
                Screen.Exam -> ExamScreen(
                    testViewModel,
                    onFinished = { currentScreen = Screen.ExamResult },
                    onAbandoned = { currentScreen = Screen.Home }
                )
                Screen.Stats -> StatsScreen(testViewModel, onHome = { currentScreen = Screen.Home })
                Screen.ExamResult -> ExamResultScreen(
                    testViewModel,
                    onReview = { currentScreen = Screen.Review },
                    onHome = { currentScreen = Screen.Home }
                )
            }
        }
    }
}
