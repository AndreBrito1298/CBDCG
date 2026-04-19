# Card-Based Dungeon Crawling Game

This is a Kotlin Multiplatform application for a turn-based digital card game experience. The project combines a Compose Multiplatform client with a Ktor server to support cross-platform play, shared domain logic, and real-time interaction between players.

The game is centered around a dungeon-crawling board game structure in which players progressively shape the board, control characters with distinct attributes and abilities, move through said board, interact with enemies, and engage in battles to obtain items and advance toward victory. The design aims to combine strategic board building, character management, and competitive encounters within a multiplayer environment.

## Project Overview

CBDCG is designed as a client-server application with shared models and communication contracts across platforms.

- `composeApp/` contains the Compose Multiplatform client application
- `server/` contains the Ktor backend and multiplayer communication layer
- `shared/` contains shared domain models, DTOs, and common logic
- `Docs/` contains supporting documentation, diagrams, and design notes

## Game Concept

The application is based on a turn-based card game flow where players participate in a shared session and progress through multiple phases of play. Core ideas of the game include:

- building and expanding the board over the course of the match;
- managing playable characters with unique stats and abilities;
- moving across the map and reacting to the evolving board state;
- encountering enemies and other players;
- resolving battles and collecting items that influence progression and victory conditions.

The broader goal of the project is to provide the full digital support layer for this experience, including session management, game state synchronization, and multiplayer interaction.

## Technology Stack

- Kotlin Multiplatform
- Compose Multiplatform
- Ktor
- WebSockets
- client-server architecture

## Running the Project
