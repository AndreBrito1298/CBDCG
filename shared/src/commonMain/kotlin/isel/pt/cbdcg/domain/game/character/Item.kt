package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.ItemDTO

data class Item(
    val name: String,
    val stats: Stats,
    val grade: Grade,
)

fun Item.toItemDTO(): ItemDTO =
    ItemDTO(
        name = name,
        stats = stats.toString(),
        grade = grade.code(),
    )

fun ItemDTO.toItem(): Item = Item(
    name = name,
    stats = stats.toStats(),
    grade = grade.toGrade(),
)

object ItemCatalog {
    val items = listOf(
        Item(name = "iron_claw", stats = Stats(0u, 1u, 0u, 0u), grade = Grade.BASIC),
        Item(name = "long_sword", stats = Stats(0u, 1u, 0u, 0u), grade = Grade.BASIC),
        Item(name = "machete", stats = Stats(0u, 1u, 0u, 0u), grade = Grade.BASIC),
        Item(name = "sturdy_gloves", stats = Stats(0u, 0u, 1u, 0u), grade = Grade.BASIC),
        Item(name = "shield", stats = Stats(0u, 0u, 1u, 0u), grade = Grade.BASIC),
        Item(name = "rusty_equip", stats = Stats(0u, 0u, 1u, 0u), grade = Grade.BASIC),
        Item(name = "strength_vial", stats = Stats(1u, 0u, 0u, 0u), grade = Grade.BASIC),
        Item(name = "health_pot", stats = Stats(1u, 0u, 0u, 0u), grade = Grade.BASIC),
        Item(name = "praying_stick", stats = Stats(1u, 0u, 0u, 0u), grade = Grade.BASIC),
        Item(name = "daggers", stats = Stats(0u, 0u, 0u, 1u), grade = Grade.BASIC),
        Item(name = "spear", stats = Stats(0u, 0u, 0u, 1u), grade = Grade.BASIC),
        Item(name = "winged_bow", stats = Stats(0u, 0u, 0u, 1u), grade = Grade.BASIC),
    )
}