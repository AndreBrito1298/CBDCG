package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTableInput(
    val name: String,
    val owner: String,
)