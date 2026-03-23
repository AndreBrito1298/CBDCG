package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserInput(
    val name: String,
    val email: String,
    val password: String,
)
