package isel.pt.cbdcg

import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val SERVER_PORT = 8080
const val MAX_TILES_IN_HAND = 5
const val SNEAK_BASE_CHANCE = 0.2
const val MIN_PLAYERS_TO_START = 2
const val MAX_PLAYERS_TO_START = 4
const val INITIAL_TILE_CARDS = 3
const val INITIAL_ITEM_CARDS = 2
const val INITIAL_CHARACTER_CARDS = 2
const val NUM_4_WAY_TILES = 13u
const val NUM_3_WAY_TILES = 24u
const val NUM_2_WAY_TILES = 31u
const val NUM_COPIES_CHARACTER = 1u
const val NUM_COPIES_ITEM = 1u
const val NUM_KEY_ITEMS = 5
const val MAX_STAT_VALUE = 12
const val ITEM_CAPACITY = 1
const val MIN_SNEAK_CHANCE = 30
const val SINGLE_TARGET_EFFECT_MOD = 1
const val AOE_EFFECT_MOD = 2
const val EFFECT_DURATION = 2u
const val HOLD_BONUS_FLEE_CHANCE = 0.2
const val BASE_HOLD_DEFENCE_BOOST = 1
const val TURN_DURATION_SECONDS = 181
const val BATTLE_TURN_DURATION_SECONDS = 16
const val REMAINING_SECONDS_AFTER_BATTLE = 61
const val BASE_FLEE_CHANCE = 0.20
const val MAX_GAME_TURNS = 2u
const val BASIC_POINTS = 1
const val EVOLVE_POINTS = 2
const val RARE_POINTS = 2
const val KEY_POINTS = 3
const val EPIC_POINTS = 4
const val TIME_BETWEEN_CLEANUP = 10L
val REFRESH_INCREMENT = (BATTLE_TURN_DURATION_SECONDS * 5).toDuration(DurationUnit.MILLISECONDS)
val GAME_SESSION_TIME = 86400000.toDuration(DurationUnit.MILLISECONDS)