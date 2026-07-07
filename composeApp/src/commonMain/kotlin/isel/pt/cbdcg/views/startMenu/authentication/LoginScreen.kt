package isel.pt.cbdcg.views.startMenu.authentication

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.isEmailLengthValid
import isel.pt.cbdcg.domain.isEmailValid
import isel.pt.cbdcg.domain.isPasswordLengthValid

@Composable
fun LoginScreen(
    mainMenuNav: () -> Unit,
    login: (String, String) -> Unit
) {

    var email by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>("") } // Disable the login button

    var password by rememberSaveable { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>("") } // Disable the login button

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ){

        Button(onClick = mainMenuNav) {
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
        )

        Button(
            enabled = emailError == null && passwordError == null,
            onClick = { login(email, password) },
        ) {
            Text("Log In")
        }
    }
}