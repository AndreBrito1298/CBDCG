package isel.pt.cbdcg.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.dto.UserOutput

@Composable
fun displayUserOutput(error: String? = null, result: UserOutput? = null) {

    if (error != null) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = error,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    if (result != null) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("User Details", style = MaterialTheme.typography.titleMedium)
                Text("Id: ${result.id}")
                Text("Name: ${result.name}")
                Text("Email: ${result.email}")
                // Text("Password: ${result.password}")
            }
        }
    }

}