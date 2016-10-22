/**
 * Déserialiseur d'état de jeu
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.client;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.epfl.xblast.Cell;
import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.RunLengthEncoder;
import ch.epfl.xblast.SubCell;
import ch.epfl.xblast.client.GameState.Player;

public final class GameStateDeserializer {
    private final static int TOTAL_NUMBER_LED = 60;
    private final static byte BYTE_LED_ON = 21;
    private final static byte BYTE_LED_OFF = 20;
    private final static byte BYTE_TEXT_MIDDLE = 10;
    private final static byte BYTE_TEXT_RIGHT = 11;
    private final static byte BYTE_TEXT_VOID = 12;
    private final static byte SIZE_SERIALIZED_PLAYER = 16;
    private final static int NBR_IMAGES_PER_PLAYER = 2;
    private final static int NBR_IMAGES_MIDDLE_GAP = 8;
    
    private final static ImageCollection BLOCK_IMAGES = new ImageCollection("block");
    private final static ImageCollection EXPLOSION_IMAGES = new ImageCollection("explosion");
    private final static ImageCollection PLAYER_IMAGES = new ImageCollection("player");
    private final static ImageCollection SCORE_IMAGES = new ImageCollection("score");
    
    private GameStateDeserializer(){}
    
    
    /**
     * Retourne une représentation de l'état du jeu étant donné une liste
     * d'octets représentant un état sérialisé
     * 
     * @param serializedGameState
     *            un état sérialisé
     * @return une représentation de l'état du jeu
     */
    public static GameState deserializeGameState(List<Byte> serializedGameState){
        //Initialise quelques valeurs de position et de taille utiles
        //à la création des sublist
        int sizeSerializedBoard = Byte
                .toUnsignedInt(serializedGameState.get(0));
        int sizeSerializedBombAndBlast = Byte.toUnsignedInt(
                serializedGameState.get(sizeSerializedBoard + 1));
        int positionSerializedPlayer = sizeSerializedBoard
                + sizeSerializedBombAndBlast + 2;
        
        //Crée l'état de jeu à l'aide des méthodes privées de la classe
        List<Image> deserializedBoard = deserializeBoard(
                serializedGameState.subList(1, 1 + sizeSerializedBoard));
        List<Image> deserializedBombAndBlast = deserializeBombAndBlast(
                serializedGameState.subList(sizeSerializedBoard + 2,
                        positionSerializedPlayer));
        List<Player> deserializedPlayer = deserializePlayers(
                serializedGameState.subList(positionSerializedPlayer,
                        positionSerializedPlayer + SIZE_SERIALIZED_PLAYER));
        List<Image> deserializedScore = deserializeScore(deserializedPlayer);
        List<Image> deserializedTime = deserializeTime(serializedGameState
                .get(positionSerializedPlayer + SIZE_SERIALIZED_PLAYER));
        return new GameState(deserializedPlayer, deserializedBoard,
                deserializedBombAndBlast, deserializedScore, deserializedTime);
    }
    
    
    
    /**
     * Retourne la liste des joueurs étant donné une liste d'octets représentant
     * les joueurs sérialisés
     * 
     * @param serializedPlayers
     *            les joueurs sérialisés
     * @return la liste des joueurs
     */
    private static List<Player> deserializePlayers(List<Byte> serializedPlayers){
        List<Player> players = new ArrayList<>();
        Iterator<Byte> it = serializedPlayers.iterator();
        for(PlayerID id : PlayerID.values()){
            players.add(new Player(
                    id,
                    Byte.toUnsignedInt(it.next()),
                    new SubCell(Byte.toUnsignedInt(it.next()), Byte.toUnsignedInt(it.next())),
                    PLAYER_IMAGES.imageOrNull(Byte.toUnsignedInt(it.next()))
                    ));
        }
        return Collections.unmodifiableList(players);
    }
    
    
    /**
     * Retourne une liste d'image représentant le plateau de jeu dans l'ordre
     * de lecture étant donné une version sérialisée de celui-ci
     * 
     * @param serializedBoard
     *          le board sérialisé
     * @return une liste d'image correspondant au plateau de jeu
     */
    private static List<Image> deserializeBoard(List<Byte> serializedBoard){
        Iterator<Cell> cellIt = Cell.SPIRAL_ORDER.iterator();
        Image[] boardRowMajorOrder = new Image[Cell.COUNT];
        for(byte b : RunLengthEncoder.decode(serializedBoard)){
            boardRowMajorOrder[cellIt.next().rowMajorIndex()] = BLOCK_IMAGES.image(b);
        }    
        return Collections.unmodifiableList(Arrays.asList(boardRowMajorOrder));
    }
    
    /**
     * Retourne une liste d'image représentant les bombes et particules
     * d'explosion dans l'ordre de lecture étant donné une version sérialisée de
     * ceux-ci
     * 
     * @param serializedBombsAndBlasts
     *            les bombes et particules d'explosion sérialisés
     * @return une liste d'image représentant les bombes et particules
     *         d'explosion
     */
    private static List<Image> deserializeBombAndBlast(List<Byte> serializedBombsAndBlasts){
        List<Image> bombsAndBlasts = new ArrayList<>();
        for(byte b : RunLengthEncoder.decode(serializedBombsAndBlasts)){
            bombsAndBlasts.add(EXPLOSION_IMAGES.imageOrNull(b));
        }
        return Collections.unmodifiableList(bombsAndBlasts);
    }
    
    /**
     * Retourne une liste d'image représentant le score étant donné la liste des
     * joueurs
     * 
     * @param players
     *          la liste des joueurs
     * @return une liste d'image représentant le score
     */
    private static List<Image> deserializeScore(List<Player> players){
        List<Image> playerScore = new ArrayList<>();
        for(Player player : players){
            //Ajoute la tête du joueur selon s'il est mort ou pas
            playerScore.add(SCORE_IMAGES.image(player.id().ordinal() * NBR_IMAGES_PER_PLAYER + (player.lives() == 0
                    ? 1
                    : 0)));
            playerScore.add(SCORE_IMAGES.image(BYTE_TEXT_MIDDLE));
            playerScore.add(SCORE_IMAGES.image(BYTE_TEXT_RIGHT));
            //Entre le joueur 2 et 3, il y a 8 blocs "vides"
            if(player.id() == PlayerID.PLAYER_2){
                playerScore.addAll(Collections.nCopies
                        (NBR_IMAGES_MIDDLE_GAP, SCORE_IMAGES.image(BYTE_TEXT_VOID)));
            }
        }
        return Collections.unmodifiableList(playerScore);
    }
    
    /**
     * Retourne une liste d'image représentant le temps restant d'une partie
     * étant donné le temps restant sérialisé
     * 
     * @param serializedTime
     *          le temps restant sérialisé
     * @return une liste d'image représentant le temps restant d'une partie
     */
    private static List<Image> deserializeTime(byte serializedTime){
        int numberLedOn = Byte.toUnsignedInt(serializedTime);
        List<Image> remainingTime = new ArrayList<>();
        remainingTime.addAll(Collections.nCopies(numberLedOn, SCORE_IMAGES.image(BYTE_LED_ON)));
        remainingTime.addAll(Collections.nCopies(TOTAL_NUMBER_LED - numberLedOn, SCORE_IMAGES.image(BYTE_LED_OFF)));
        return Collections.unmodifiableList(remainingTime);
    }
}
