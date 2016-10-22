/**
 * Serialiseur d'état de jeu
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.server;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.xblast.Cell;
import ch.epfl.xblast.Direction;
import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.RunLengthEncoder;

public final class GameStateSerializer {

    private static final int SECONDS_BY_LED = 2;
    
    private GameStateSerializer() {
    }

    /**
     * Retourne la version sérialisée de l'état, sous forme d'une liste d'octets
     * étant donné un peintre de plateau et un état
     * 
     * @param boardPainter
     *            le peintre de plateau
     * @param gameState
     *            l'état
     * @return la version sérialisée de l'état
     */
    public static List<Byte> serialize(BoardPainter boardPainter,
            GameState gameState) {
        List<Byte> serializedGameState = new ArrayList<>();
        serializedGameState
                .addAll(serializeBoard(boardPainter, gameState.board()));
        serializedGameState
                .addAll(serializedBombExplosions(gameState.bombedCells(),
                        gameState.blastedCells(), gameState.board()));
        serializedGameState.addAll(
                serializedPlayer(gameState.players(), gameState.ticks()));
        serializedGameState
                .add(serializedRemainingTime(gameState.remainingTime()));
        
        int b = 0;
        b += gameState.explodingSound() ? 1 : 0;
        for(PlayerID id : PlayerID.values()){
            b = b << 1;
            b += gameState.isUpgraded(id) ? 1 : 0;
        }
        serializedGameState.add((byte) b);
        return serializedGameState;
    }

    /**
     * Retourne la version sérialisée du plateau de jeu
     * 
     * @param boardPainter
     *            le peintre de plateau
     * @param board
     *            la plateau de jeu
     * @return la version sérialisée du plateau de jeu
     */
    private static List<Byte> serializeBoard(BoardPainter boardPainter,
            Board board) {
        List<Byte> serializedBoard = new LinkedList<>();
        // Parcours les cases dans l'ordre spirale et ajoute la version
        // sérialisée de chaque case à la liste serializedBoard
        for (Cell c : Cell.SPIRAL_ORDER) {
            serializedBoard.add(boardPainter.byteForCell(board, c));
        }
        // Compresse la sérialisation du plateau de jeu
        List<Byte> encodedSerializedBoard = new LinkedList<>(
                RunLengthEncoder.encode(serializedBoard));
        encodedSerializedBoard.add(0, (byte) encodedSerializedBoard.size());
        return encodedSerializedBoard;
    }

    /**
     * Retourne la version sérialisée des bombes et explosions
     * 
     * @param bombedCells
     *            les cases contenant une bombes
     * @param blastedCells
     *            les cases contenant des particules d'explosion
     * @param board
     *            le plateau de jeu
     * @return la version sérialisée des bombes et explosions
     */
    private static List<Byte> serializedBombExplosions(
            Map<Cell, Bomb> bombedCells, Set<Cell> blastedCells, Board board) {
        List<Byte> serializedBombExplosions = new LinkedList<>();

        for (Cell c : Cell.ROW_MAJOR_ORDER) {
            if (bombedCells.containsKey(c)) {
                serializedBombExplosions
                        .add(ExplosionPainter.byteForBomb(bombedCells.get(c)));
            } else if (blastedCells.contains(c) && board.blockAt(c).isFree()) {
                boolean N = blastedCells.contains(c.neighbor(Direction.N));
                boolean E = blastedCells.contains(c.neighbor(Direction.E));
                boolean S = blastedCells.contains(c.neighbor(Direction.S));
                boolean W = blastedCells.contains(c.neighbor(Direction.W));
                serializedBombExplosions.add(ExplosionPainter.byteForBlast(N, E, S, W));

            } else {
                serializedBombExplosions.add(ExplosionPainter.BYTE_FOR_EMPTY);
            }
        }
        List<Byte> encodedSerializedBombExplosions =
                new LinkedList<>(RunLengthEncoder.encode(serializedBombExplosions));
        encodedSerializedBombExplosions.add(0, (byte) encodedSerializedBombExplosions.size());
        return encodedSerializedBombExplosions;
    }

    /**
     * Retourne la version sérialisée des joueurs
     * 
     * @param players
     *            liste contenant les joueurs
     * @param tick
     *            tick de la partie
     * @return la version sérialisée des joueurs
     */
    private static List<Byte> serializedPlayer(List<Player> players, int tick) {
        List<Byte> serializedPlayer = new ArrayList<>();
        for (Player player : players) {
            serializedPlayer.addAll(Arrays.asList(
                    (byte) player.lives(),
                    (byte) player.position().x(),
                    (byte) player.position().y(),
                    PlayerPainter.byteForPlayer(tick, player)));
        }
        return serializedPlayer;
    }

    /**
     * Retourne la version sérialisée du temps restant
     * 
     * @param remainingTime
     *            le temps restant (en seconde)
     * @return la version sérialisée du temps restant
     */
    private static Byte serializedRemainingTime(double remainingTime) {
        return (byte) Math.ceil(remainingTime / SECONDS_BY_LED);
    }
}
