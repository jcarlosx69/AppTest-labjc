package es.labjc.aplicaciontest.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Colores de labjc.es, sacados del logo y del icono de la app (los mismos que res/values/colors.xml). */
object LabjcColors {
    val Blue = Color(0xFF145BA0)
    val Navy = Color(0xFF1B3A5C)
    val Cyan = Color(0xFF29A8D8)
    val Background = Color(0xFFEEF4FA)
    val Correct = Color(0xFF2E7D32)
    val Wrong = Color(0xFFC62828)
}

private val LabjcColorScheme = lightColorScheme(
    primary = LabjcColors.Blue,
    onPrimary = Color.White,
    secondary = LabjcColors.Cyan,
    onSecondary = Color.White,
    tertiary = LabjcColors.Navy,
    background = LabjcColors.Background,
    surface = Color.White,
    onBackground = LabjcColors.Navy,
    onSurface = LabjcColors.Navy
)

@Composable
fun LabjcTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LabjcColorScheme, content = content)
}
