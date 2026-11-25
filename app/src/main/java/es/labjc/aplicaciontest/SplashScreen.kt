package es.labjc.aplicaciontest

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.labjc.aplicaciontest.ui.LabjcColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Tiempo que se ve la pantalla de bienvenida, sin contar la animación de entrada. */
private const val SPLASH_MILLIS = 1_400L

/**
 * Pantalla de bienvenida: fondo azul labjc, el logo aparece con un pequeño zoom
 * y a los pocos segundos pasa sola al inicio. Solo se ve al abrir la app
 * (al girar la pantalla no vuelve a salir).
 */
@Composable
fun SplashScreen(title: String, onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(durationMillis = 700)) }
        alpha.animateTo(1f, tween(durationMillis = 700))
        delay(SPLASH_MILLIS)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Empieza en el mismo azul que el arranque del sistema (windowBackground),
            // así no se nota el salto entre las dos pantallas.
            .background(Brush.verticalGradient(listOf(LabjcColors.Navy, LabjcColors.Blue))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .alpha(alpha.value)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_labjc),
                    contentDescription = "Logo labjc",
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                )
            }
            Text(
                text = title.ifEmpty { "Test" },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "De la madera al código",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp
            )
        }

        CircularProgressIndicator(
            color = LabjcColors.Cyan,
            strokeWidth = 3.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .size(28.dp)
                .alpha(alpha.value)
        )
    }
}
