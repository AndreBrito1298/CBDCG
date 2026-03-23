package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Table
import kotlinx.serialization.Serializable

@Serializable
data class TableOutput(
    val id: UInt,
    val name: String,
    val owner: UInt,
    val players: UInt,
)

fun Table.toTableOutput(): TableOutput = TableOutput(
    id = id,
    name = name.string,
    owner = owner,
    players = players
)