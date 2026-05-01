package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.error.GameError

enum class Direction { EAST, NORTH, SOUTH, WEST }

fun Direction.opposite() = when(this){
    Direction.NORTH -> Direction.SOUTH
    Direction.EAST -> Direction.WEST
    Direction.SOUTH -> Direction.NORTH
    Direction.WEST -> Direction.EAST
}

fun Direction.rotateRight() = when(this){
    Direction.NORTH -> Direction.EAST
    Direction.EAST -> Direction.SOUTH
    Direction.SOUTH -> Direction.WEST
    Direction.WEST -> Direction.NORTH
}

fun Direction.rotateLeft() = when(this){
    Direction.NORTH -> Direction.WEST
    Direction.EAST -> Direction.NORTH
    Direction.SOUTH -> Direction.EAST
    Direction.WEST -> Direction.SOUTH
}

fun Char.toDirection(): Direction = when(this){
    'N' -> Direction.NORTH
    'E' -> Direction.EAST
    'S' -> Direction.SOUTH
    'W' -> Direction.WEST
    else -> throw GameError.InvalidDirection(this)
}