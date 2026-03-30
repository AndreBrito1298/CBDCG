package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class LogoutInput(
    val token: String,
)