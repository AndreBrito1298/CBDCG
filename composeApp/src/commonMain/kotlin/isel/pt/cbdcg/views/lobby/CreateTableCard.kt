package isel.pt.cbdcg.views.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.isNameFilled
import isel.pt.cbdcg.domain.isNameLengthValid

@Composable
fun CreateTableCard(
    createTable: (String) -> Unit,
) {

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>("") } // Disable the create button

    Card(
        modifier = Modifier.fillMaxWidth(),
    ){

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Create table",
                    style = MaterialTheme.typography.titleMedium,
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
            }

            Button(
                onClick = { createTable(name) },
                enabled = nameError == null,
                modifier = Modifier.padding(start = 16.dp),
            ) {
                Text("Create Table")
            }
        }
    }
}