package isel.pt.cbdcg.views.game.utils.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.directionTo
import isel.pt.cbdcg.domain.game.board.opposite

data class TilePathSegment(
    val from: Direction?,
    val to: Direction?,
    val isEnd: Boolean
)

fun pathSegmentFor(
    path: List<BoardTile>,
    tile: BoardTile
): TilePathSegment? {
    val index = path.indexOf(tile)
    if (index == -1) return null

    val previous = path.getOrNull(index - 1)
    val next = path.getOrNull(index + 1)

    return TilePathSegment(
        from = previous?.let { tile.directionTo(it) },
        to = next?.let { tile.directionTo(it) },
        isEnd = index == path.lastIndex
    )
}

@Composable
fun MovementPathOverlay(
    segment: TilePathSegment,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {

        val center = Offset(size.width / 2f, size.height / 2f)
        val stroke = 8.dp.toPx()
        val color = Color.Yellow

        // Norte: de cima até meio | Sul: de meio até baixo | Este: de meio até a direita | Oeste: da esquerda até o meio
        fun edge(direction: Direction): Offset =
            when (direction) {
                Direction.NORTH -> Offset(size.width / 2f, 0f)
                Direction.SOUTH -> Offset(size.width / 2f, size.height)
                Direction.EAST -> Offset(size.width, size.height / 2f)
                Direction.WEST -> Offset(0f, size.height / 2f)
            }

        // Desenha os fragmentos de acordo com a Tile e a próxima do caminho
        segment.from?.let { from ->
            drawLine(
                color = color,
                start = edge(from),
                end = center,
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
        segment.to?.let { to ->
            drawLine(
                color = color,
                start = center,
                end = edge(to),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }

        // Desenha a seta no fim do caminho
        if (segment.isEnd && segment.from != null) {
            val direction = segment.from.opposite()
            val tip = center
            val arrowSize = 14.dp.toPx()

            val left: Offset
            val right: Offset

            when (direction) {
                Direction.NORTH -> {
                    left = Offset(tip.x - arrowSize, tip.y + arrowSize)
                    right = Offset(tip.x + arrowSize, tip.y + arrowSize)
                }
                Direction.SOUTH -> {
                    left = Offset(tip.x - arrowSize, tip.y - arrowSize)
                    right = Offset(tip.x + arrowSize, tip.y - arrowSize)
                }
                Direction.EAST -> {
                    left = Offset(tip.x - arrowSize, tip.y - arrowSize)
                    right = Offset(tip.x - arrowSize, tip.y + arrowSize)
                }
                Direction.WEST -> {
                    left = Offset(tip.x + arrowSize, tip.y - arrowSize)
                    right = Offset(tip.x + arrowSize, tip.y + arrowSize)
                }
            }

            drawLine(color, tip, left, strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, tip, right, strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}