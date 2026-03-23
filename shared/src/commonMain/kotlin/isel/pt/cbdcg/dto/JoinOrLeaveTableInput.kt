package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class JoinOrLeaveTableInput(
    val name: String,
    val owner: String,
)