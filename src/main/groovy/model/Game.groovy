package model

import groovy.transform.ToString
import model.player.Player
import model.player.PlayerId
import model.player.Players

@ToString
class Game {
    PlayerId currentPlayerId = null
    Players players = new Players()
    GamePhase gamePhase = GamePhase.PLACEMENT

    State shootAt(Map<String, String> fireCoordinate, PlayerId shooterId) {
        final Player opponent = players.oppositePlayer(shooterId)
        final Player shooter = players.playerBy(shooterId)

        final FieldState state = opponent.shotAt(fireCoordinate)
        shooter.setShotResult(fireCoordinate, state)

        if(!opponent.hasShipsLeft()){
            gamePhase= GamePhase.FINISHED
        }

        currentPlayerId = opponent.id

        getState(shooterId)
    }

    State placeBoat(Map<String, Map<String, String>> boatCoordinates, PlayerId playerId) {
        final Player player = players.playerBy(playerId)
        if (player) {
            final map = player.placeBoat(boatCoordinates)

            if (players.allShipsArePlaced()) {
                //Now let the game start
                gamePhase=GamePhase.SHOOTOUT
                currentPlayerId = players.first
            }

            getState(playerId)
        } else {
            null
        }
    }



    boolean myTurn(playerId) {
        currentPlayerId == null ? false : currentPlayerId == playerId
    }

    Optional<PlayerId> addPlayer() {
        players.addPlayer()
    }

    State getState(PlayerId playerId){
        Player player = players.playerBy(playerId)
        new State(
                playerId:       playerId,
                myTurn:         myTurn(playerId),
                gamePhase:      gamePhase,
                availableShips: player.availableShipsList(),
                field:          player.positionListFor(player.field),
                isVictory:      player.hasShipsLeft(),
                oppositeField:  player.positionListFor(player.oppositeField),
                undamagedShips: player.shipCounter
        )
    }
}
