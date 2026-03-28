package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChangeRoleInput(
    val name: String,
    val role: String,
)