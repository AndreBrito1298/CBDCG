package isel.pt.cbdcg.views.game.utils.cardInfo

import androidx.compose.ui.graphics.Color

enum class StarColor {
    GRAY,
    YELLOW,
    BLUE
}

fun statStars(value: Int): List<StarColor> {
    val clamped = value.coerceIn(0, 12)

    return if (clamped <= 6) {
        List(6) { index ->
            if (index < clamped) StarColor.YELLOW
            else StarColor.GRAY
        }
    } else {
        val blue = clamped - 6
        val yellow = 6 - blue

        List(6) { index ->
            when {
                index < blue -> StarColor.BLUE
                index < blue + yellow -> StarColor.YELLOW
                else -> StarColor.GRAY
            }
        }
    }
}
fun StarColor.color(): Color =
    when (this) {
        StarColor.GRAY -> Color.Gray
        StarColor.YELLOW -> Color(0xFFFFB000)
        StarColor.BLUE -> Color(0xFF00AAFF)
    }