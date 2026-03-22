# CBDCG - Project Documentation

## 1. Introduction

**CBDCG** is a Kotlin Multiplatform (KMP) application designed as a turn-based board game management system. The project leverages **Compose Multiplatform** framework to deliver a seamless experience across web, desktop, and mobile platforms. Communication between clients and server is established through HTTP protocol.

---

## 2. Game Overview

### 2.1 Game Mechanics

CBDCG is a turn-based board game where each player influences the game board creation, controls their character, collects items, and defeats enemies. Once all players complete their actions, they are redirected to the initial screen.

#### Key Features:
- **Character Control**: Players manage their in-game character
- **Item Collection**: Strategic gathering of items from the game board
- **Enemy Encounters**: Battle mechanics when characters meet on the same tile
- **Turn-Based System**: Each player takes turns in a defined order

### 2.2 Game Flow

#### Initial Setup:
- Players can view game rules and access a list of available tables/lobbies
- At least two players must be waiting in a lobby before the game creator can start the game

#### Character Cards:
Each player receives a character card with:
- **Name & Attributes**: Character class and ability descriptions
- **Stats**: 4 colored stat bars representing different attributes
- **Abilities**: Special passive or active abilities with descriptions

#### Turn Phases (3 Phases):

1. **Draw & Add Phase**
   - Player receives one board piece
   - Player decides whether to add additional pieces from their hand
   - Add chosen pieces to any position on the board

2. **Character Exchange Phase**
   - Player can swap their current character with another character from their hand

3. **Movement Phase**
   - Player moves their character freely across the board

#### Turn Order:
The turn order is dynamic and depends on:
- Player character types
- Equipped item types
- When all players complete their turn, enemies also traverse the board with potential events to aid or hinder players

#### Battle Mechanics:
When two character pieces occupy the same board tile, a battle ensues:
- Battles are a crucial mechanic for obtaining items from other players
- Items obtained through battle may be necessary to win the game

---

## 3. Application Navigation

### 3.1 Screen Navigation Diagram

The application features the following screens and navigation paths:
