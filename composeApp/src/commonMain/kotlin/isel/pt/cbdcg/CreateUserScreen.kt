package isel.pt.cbdcg

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun CreateUserScreen(clientApi: ClientApi) {

    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var statusMessage by remember { mutableStateOf("Fill the fields and press Create user.") }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Create User",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Temporary screen for testing the createUser flow.",
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true,
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )

        Button(
            enabled = !isSubmitting,
            onClick = {
                val validationMessage = when {
                    name.isBlank() -> "Name is required."
                    email.isBlank() -> "Email is required."
                    password.isBlank() -> "Password is required."
                    else -> null
                }

                if (validationMessage != null) {
                    statusMessage = validationMessage
                    return@Button
                }

                isSubmitting = true
                statusMessage = "Creating user..."

                scope.launch {
                    when (val result = clientApi.createUser(name, email, password)) {
                        is ApiResult.Success -> {
                            statusMessage = "User ${result.value} created successfully."
                            name = ""
                            email = ""
                            password = ""
                        }
                        is ApiResult.Failure -> {
                            statusMessage = result.message
                        }
                    }
                    isSubmitting = false
                }
            },
        ) {
            Text(if (isSubmitting) "Creating..." else "Create user")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = statusMessage,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
