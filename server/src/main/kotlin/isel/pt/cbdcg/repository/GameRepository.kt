package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.game.Deck
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.character.Item

interface GameRepository: Repository<Game> {

    fun createGame(players: List<Player>, spectators: List<Spectator>, turnOrder: List<UInt>, startingDeck: Deck<Tile>, itemDeck: Deck<Item>): Game
    fun getAllGames(): List<Game>
}