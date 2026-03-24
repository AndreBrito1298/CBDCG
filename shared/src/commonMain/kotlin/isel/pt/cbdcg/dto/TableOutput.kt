package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Name
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

fun TableOutput.toTable(): Table = Table(
    id = id,
    name = Name(name),
    owner = owner,
    players = players
)