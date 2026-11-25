package es.labjc.aplicaciontest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Botón que pide un número de pregunta (1..[totalQuestions]) y devuelve
 * su posición empezando en 0 a [onSelect].
 */
@Composable
fun SelectStartButton(
    totalQuestions: Int,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }
    val number = input.toIntOrNull()
    val isValid = number != null && number in 1..totalQuestions

    Button(
        onClick = {
            input = ""
            showDialog = true
        },
        modifier = modifier,
        colors = colors
    ) {
        Text("Seleccionar desde...")
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Selecciona punto de inicio") },
            text = {
                Column {
                    Text("Número de pregunta para empezar (1 a $totalQuestions):")
                    OutlinedTextField(
                        value = input,
                        onValueChange = { value -> input = value.filter { it.isDigit() }.take(4) },
                        label = { Text("Número de pregunta") },
                        isError = input.isNotEmpty() && !isValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (number != null && isValid) {
                            onSelect(number - 1)
                            showDialog = false
                        }
                    },
                    enabled = isValid
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
