package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTableInput(
    val name: String,
    val email: String,
    val token: String,
)