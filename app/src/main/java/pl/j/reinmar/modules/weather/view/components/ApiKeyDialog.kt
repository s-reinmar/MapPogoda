package pl.j.reinmar.modules.weather.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog umożliwiający wprowadzenie lub zmianę klucza API OpenWeather.
 *
 * @param currentKey Aktualnie ustawiony klucz (lub pusty).
 * @param onDismiss Zamknięcie okna dialogowego bez zapisu.
 * @param onKeySaved Zapisanie nowego klucza API.
 */
@Composable
fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onKeySaved: (String) -> Unit
) {
    var keyText by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Konfiguracja klucza OpenWeather",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Aplikacja korzysta z darmowego API OpenWeather (openweathermap.org). Wprowadź swój klucz appid poniżej:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Klucz API OpenWeather") },
                    placeholder = { Text("np. 8f4b1...2a9c") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_text_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Uwaga: Klucz jest bezpiecznie przechowywany w pamięci aplikacji i w pliku .env.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onKeySaved(keyText) },
                modifier = Modifier.testTag("btn_save_api_key")
            ) {
                Text("Zapisz i zastosuj")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_api_key")
            ) {
                Text("Anuluj")
            }
        }
    )
}
