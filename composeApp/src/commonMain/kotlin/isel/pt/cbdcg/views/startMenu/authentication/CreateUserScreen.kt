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
import isel.pt.cbdcg.domain.isNameFilled
import isel.pt.cbdcg.domain.isNameLengthValid
import isel.pt.cbdcg.domain.isPasswordLengthValid

@Composable
fun CreateUserScreen(
    mainMenuNav: () -> Unit,
    create: (String, String, String) -> Unit
) {

    var name by rememberSaveable { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>("") } // Disable the create button

    var email by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>("") } // Disable the create button

    var password by rememberSaveable { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>("") } // Disable the create button

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Button(onClick = mainMenuNav) {
            Text("Back")
        }

        Text(
            text = "Create User",
            style = MaterialTheme.typography.headlineMedium,
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = if(!it.isNameFilled()) "Name is empty."
                            else if(!it.isNameLengthValid()) "Name cannot have more than 20 characters."
                            else null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true,
            supportingText = {
                if(nameError != null) { Text(nameError!!) }
            },
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
            enabled = nameError == null && emailError == null && passwordError == null,
            onClick = { create(name, email, password) }
        ) {
            Text("Create User")
        }
    }
}