package es.labjc.aplicaciontest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Cerrar la app = terminar la Activity. Sin System.exit(): Android gestiona el proceso.
            TestApp(onExitApp = { finishAffinity() })
        }
    }
}
