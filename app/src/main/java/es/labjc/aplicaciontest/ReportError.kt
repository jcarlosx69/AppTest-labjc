package es.labjc.aplicaciontest

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import es.labjc.aplicaciontest.data.Question

const val CONTACT_EMAIL = "Contacto@labjc.es"

/** Botón "Reportar error en esta pregunta": abre el correo con la pregunta ya escrita. */
@Composable
fun ReportErrorButton(question: Question, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    TextButton(onClick = { reportError(context, question) }, modifier = modifier) {
        Text("Reportar error en esta pregunta")
    }
}

fun reportError(context: Context, question: Question) {
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
    val subject = "Error en pregunta nº ${question.number} - $appName"
    val body = buildString {
        append("Hola, he encontrado un posible error en la siguiente pregunta:\n\n")
        append("Pregunta nº ${question.number}: ${question.text}\n")
        question.options.forEach { (letter, text) -> append("$letter) $text\n") }
        append("Respuesta marcada como correcta: ${question.correctAnswer}\n")
        append("\nDescripción del error:\n(Escribe aquí qué crees que está mal)\n")
    }
    sendEmail(context, subject, body)
}

/** Abre la app de correo. Si no hay ninguna, avisa en lugar de cerrar la app. */
fun sendEmail(context: Context, subject: String = "", body: String = "") {
    val uri = Uri.parse(
        "mailto:$CONTACT_EMAIL?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
    )
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(CONTACT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay ninguna app de correo. Escribe a $CONTACT_EMAIL", Toast.LENGTH_LONG).show()
    }
}
