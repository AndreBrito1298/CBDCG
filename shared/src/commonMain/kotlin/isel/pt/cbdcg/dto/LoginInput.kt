package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginInput(
    val email: String,
    val password: String,
)