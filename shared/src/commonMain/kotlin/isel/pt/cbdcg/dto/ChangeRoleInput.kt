package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChangeRoleInput(
    val user: String,
    val table: String,
    val token: String,
)