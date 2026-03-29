package isel.pt.cbdcg.views.startMenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import isel.pt.cbdcg.ClientApi
import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.isEmailLengthValid
import isel.pt.cbdcg.domain.isEmailValid
import isel.pt.cbdcg.domain.isPasswordLengthValid
import isel.pt.cbdcg.views.utils.displayError
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(clientApi: ClientApi, back: () -> Unit, login: (AuthUser) -> Unit) {

    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    var password by rememberSaveable { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ){

        Button(onClick = back) {
            Text("Back")
        }

        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = if(!it.isEmailValid()) "Email format is invalid."
                else if(!it.isEmailLengthValid()) "Email is too long."
                else null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            supportingText = {
                if(emailError != null) { Text(emailError!!) }
            },
            enabled = !isSubmitting,
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = if(!it.isPasswordLengthValid()) "Password must have at least 5 characters."
                else null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            supportingText = {
                if(passwordError != null) { Text(passwordError!!) }
            },
            enabled = !isSubmitting,
        )

        Button(
            enabled = !isSubmitting,
            onClick = {

                isSubmitting = true
                submitError = null

                scope.launch {

                    val result = clientApi.login(email, password)
                    result.onSuccess{ user -> login(user) }
                    result.onFailure { error -> submitError = error.message ?: "Could not login."  }

                    isSubmitting = false
                }
            },
        ) {
            Text(if (isSubmitting) "Logging in..." else "Log In")
        }

        displayError(submitError)

    }

}