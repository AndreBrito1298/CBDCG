package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class TableOperationInput(
    val table: String,
    val user: String,
    val token: String
)