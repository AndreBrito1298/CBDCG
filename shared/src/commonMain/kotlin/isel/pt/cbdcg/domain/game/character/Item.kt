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

fun getItemByName(name: String): Item? = ItemCatalog.commonItems.find { it.name == name }

object ItemCatalog {
    val commonItems = listOf(
        Item(name = "iron_claw", stats = Stats(0, 1, 0, 0), grade = Grade.BASIC),
        Item(name = "long_sword", stats = Stats(0, 1, 0, 0), grade = Grade.BASIC),
        Item(name = "machete", stats = Stats(0, 1, 0, 0), grade = Grade.BASIC),
        Item(name = "sturdy_gloves", stats = Stats(0, 0, 1, 0), grade = Grade.BASIC),
        Item(name = "shield", stats = Stats(0, 0, 1, 0), grade = Grade.BASIC),
        Item(name = "rusty_equip", stats = Stats(0, 0, 1, 0), grade = Grade.BASIC),
        Item(name = "strength_vial", stats = Stats(1, 0, 0, 0), grade = Grade.BASIC),
        Item(name = "health_pot", stats = Stats(1, 0, 0, 0), grade = Grade.BASIC),
        Item(name = "praying_stick", stats = Stats(1, 0, 0, 0), grade = Grade.BASIC),
        Item(name = "daggers", stats = Stats(0, 0, 0, 1), grade = Grade.BASIC),
        Item(name = "spear", stats = Stats(0, 0, 0, 1), grade = Grade.BASIC),
        Item(name = "winged_bow", stats = Stats(0, 0, 0, 1), grade = Grade.BASIC),

        Item(name = "fire_claw", stats = Stats(1, 1, 0, 0), grade = Grade.RARE),
        Item(name = "greatsword", stats = Stats(0, 2, 0, 0), grade = Grade.RARE),
        Item(name = "katana", stats = Stats(0, 1, 0, 1), grade = Grade.RARE),
        Item(name = "gale_gloves", stats = Stats(0, 0, 1, 1), grade = Grade.RARE),
        Item(name = "royal_shld", stats = Stats(0, 0, 2, 0), grade = Grade.RARE),
        Item(name = "old_equip", stats = Stats(0, 1, 1, 0), grade = Grade.RARE),
        Item(name = "golden_potion", stats = Stats(1, 1, 0, 0), grade = Grade.RARE),
        Item(name = "strange_pot", stats = Stats(2, 0, 0, 0), grade = Grade.RARE),
        Item(name = "holy_cross", stats = Stats(1, 0, 1, 0), grade = Grade.RARE),
        Item(name = "ench_daggers", stats = Stats(0, 0, 0, 2), grade = Grade.RARE),
        Item(name = "fort_spear", stats = Stats(0, 0, 1, 1), grade = Grade.RARE),
        Item(name = "glow_bow", stats = Stats(1, 0, 0, 1), grade = Grade.RARE),

        Item(name = "bonecrusher", stats = Stats(1, 2, 0, 0), grade = Grade.EPIC),
        Item(name = "sky_pierce", stats = Stats(0, 2, 1, 0), grade = Grade.EPIC),
        Item(name = "willowblade", stats = Stats(0, 2, 0, 1), grade = Grade.EPIC),
        Item(name = "magic_gloves", stats = Stats(0, 0, 2, 1), grade = Grade.EPIC),
        Item(name = "ancient_shld", stats = Stats(1, 0, 2, 0), grade = Grade.EPIC),
        Item(name = "prem_equip", stats = Stats(0, 1, 2, 0), grade = Grade.EPIC),
        Item(name = "god_liquor", stats = Stats(2, 1, 0, 0), grade = Grade.EPIC),
        Item(name = "elixir", stats = Stats(2, 0, 0, 1), grade = Grade.EPIC),
        Item(name = "sanct_staff", stats = Stats(2, 0, 1, 0), grade = Grade.EPIC),
        Item(name = "relic_daggers", stats = Stats(0, 1, 0, 2), grade = Grade.EPIC),
        Item(name = "gold_trident", stats = Stats(0, 0, 1, 2), grade = Grade.EPIC),
        Item(name = "golden_bow", stats = Stats(1, 0, 0, 2), grade = Grade.EPIC),
    )
    val specialItems = listOf(
        Item(name = "fae", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "jade_sword", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "red_gourd", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "badge", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "golden_glove", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "golden_star", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "peacemaker", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "poison_shard", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "unknown_stone", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "nirvana_cross", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "idol", stats = Stats(), grade = Grade.EVOLVE),
        Item(name = "chalice", stats = Stats(), grade = Grade.EVOLVE),

        Item(name = "red_key", stats = Stats(0,0,0,0), grade = Grade.KEY),
        Item(name = "blue_key", stats = Stats(0,0,0,0), grade = Grade.KEY),
        Item(name = "green_key", stats = Stats(0,0,0,0), grade = Grade.KEY),
        Item(name = "yellow_key", stats = Stats(0,0,0,0), grade = Grade.KEY),
        Item(name = "white_key", stats = Stats(0,0,0,0), grade = Grade.KEY),
    )
}