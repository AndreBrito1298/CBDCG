# CBDCG - Project Documentation

## 1. Introduction

**CBDCG** is a Kotlin Multiplatform (KMP) application designed as a turn-based board game management system. The project leverages **Compose Multiplatform** framework to deliver a seamless experience across web, desktop, and mobile platforms. Communication between clients and server is established through HTTP protocol.

---

## 2. Game Overview

### 2.1 Game Mechanics

CBDCG is a turn-based board game where each player influences the game board creation, controls their character, collects items, and defeats enemies. To win, a player must be at the **Start Tile** while holding all **Key Items**. Once a winner is declared, the game ends.

#### Key Features:
- **Character Control**: Players manage their in-game character.
- **Item Collection**: Strategic gathering of items from the game board, including **Key Items** necessary for victory.
- **Dungeon Construction**: Players build the board by placing tiles.
- **Turn-Based System**: Each player takes turns in a defined order.
- **Victory Condition**: Return to the **Start Tile** with all **Key Items** in hand.

### 2.2 Game Flow

#### Initial Setup:
- Players can view game rules and access a list of available tables/lobbies.
- At least two players must be waiting in a lobby before the game creator can start the game.

#### Character Cards:
Each player receives a character card with:
- **Name & Attributes**: Character class and ability descriptions.
- **Stats**: HP, ATK, DEF, SPE.
- **Items**: Characters can equip items to boost their stats.

#### Turn Phases (3 Phases):

1. **Construction Phase**
   - At the start of the turn, the player draws a tile from the deck.
   - The player can place tiles from their hand onto the board.

2. **Substitution Phase**
   - Player can place their character on the board (if not already placed) or equip items.

3. **Movement Phase**
   - Player moves their character across the board based on their SPE stat.
   - During movement, characters can interact with tile effects (e.g., Chests).

#### Turn Order:
The turn order is currently sequential based on the order players joined the game. When a full round of turns is completed, the dungeon turn increases.

#### Winning the Game:
A player wins when:
1. They are at the **Start Tile**.
2. They have at least one **Key Item** in their hand.
Check for victory happens after movement, drawing items, or at the end of a turn.

---

## 3. Application Navigation

### 3.1 Screen Navigation Diagram

The application features the following screens and navigation paths:
