package isel.pt.cbdcg.views.game.utils.misc.stats

import androidx.compose.ui.graphics.Color

enum class StarColor {
    GRAY, YELLOW, BLUE,
    GREEN, DARK_BLUE, RED, PINK

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
fun statVariation(value: Int, delta: Int): List<StarColor> {

    val before = statStars(value)
    val after = statStars(value + delta)

    return before.zip(after) { oldColor, newColor ->
        when{
            oldColor == StarColor.GRAY && newColor == StarColor.YELLOW -> StarColor.GREEN
            oldColor == StarColor.GRAY && newColor == StarColor.BLUE -> StarColor.DARK_BLUE

            oldColor == StarColor.YELLOW && newColor == StarColor.BLUE -> StarColor.GREEN
            oldColor == StarColor.YELLOW && newColor == StarColor.GRAY -> StarColor.RED

            oldColor == StarColor.BLUE && newColor == StarColor.GRAY -> StarColor.PINK
            oldColor == StarColor.BLUE && newColor == StarColor.YELLOW -> StarColor.RED
            else -> oldColor
        }
    }
}

fun StarColor.color(): Color =
    when (this) {
        StarColor.GRAY -> Color.Gray
        StarColor.YELLOW -> Color(0xFFFFB000)
        StarColor.BLUE -> Color(0xFF00AAFF)
        StarColor.RED -> Color(0xFFF00000)
        StarColor.PINK -> Color(0xFFF000FF)
        StarColor.GREEN -> Color(0xFF50B050)
        StarColor.DARK_BLUE -> Color(0xFF0000FF)
    }