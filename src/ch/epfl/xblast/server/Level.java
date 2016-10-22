package ch.epfl.xblast.server;

/**
 * Classe qui modélise un niveau
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.xblast.Cell;
import ch.epfl.xblast.PlayerID;


public final class Level {
    
    public final static Level DEFAULT_LEVEL = new Level(defaultBoardPainter(), defaultGameState());
    
    private final BoardPainter boardPainter;
    private final GameState initialGameState;
    
    /**
     * Retourne un BoardPainter par défaut
     * 
     * @return un BoardPainter par défaut
     */
    private static BoardPainter defaultBoardPainter(){
        Map<Block, BlockImage> palette = new HashMap<>();
        palette.put(Block.FREE, BlockImage.IRON_FLOOR);
        palette.put(Block.INDESTRUCTIBLE_WALL, BlockImage.DARK_BLOCK);
        palette.put(Block.DESTRUCTIBLE_WALL, BlockImage.EXTRA);
        palette.put(Block.CRUMBLING_WALL, BlockImage.EXTRA_O);
        palette.put(Block.BONUS_BOMB, BlockImage.BONUS_BOMB);
        palette.put(Block.BONUS_RANGE, BlockImage.BONUS_RANGE);
        return new BoardPainter(palette, BlockImage.IRON_FLOOR_S);
    }
    
    /**
     * Retourne un GameState par défaut
     * 
     * @return un GameState par défaut
     */
    private static GameState defaultGameState(){
        Block __ = Block.FREE;
        Block XX = Block.INDESTRUCTIBLE_WALL;
        Block xx = Block.DESTRUCTIBLE_WALL;
        Board board = Board.ofQuadrantNWBlocksWalled(
          Arrays.asList(
            Arrays.asList(__, __, __, __, __, __, __),
            Arrays.asList(__, XX, xx, XX, xx, XX, xx),
            Arrays.asList(__, xx, __, __, __, xx, __),
            Arrays.asList(xx, XX, __, XX, XX, XX, XX),
            Arrays.asList(__, xx, __, xx, __, __, __),
            Arrays.asList(xx, XX, xx, XX, xx, XX, __)));
        
        Player p1 = new Player(PlayerID.PLAYER_1, 3, new Cell(1, 1), 2, 3);
        Player p2 = new Player(PlayerID.PLAYER_2, 3, new Cell(13, 1), 2, 3);
        Player p3 = new Player(PlayerID.PLAYER_3, 3, new Cell(13, 11), 2, 3);
        Player p4 = new Player(PlayerID.PLAYER_4, 3, new Cell(1, 11), 2, 3);
        List<Player> players = new ArrayList<Player>();
        players.add(p1);
        players.add(p2);
        players.add(p3);
        players.add(p4);
        return new GameState(board, players);
    }
    
    /**
     * Construit un level avec le peintre de plateau et l'état initial
     * passés en argument
     * 
     * @param boardPainter
     *          le peintre de plateau
     * @param initialGameState
     *          l'état initial
     */
    public Level (BoardPainter boardPainter, GameState initialGameState) {
        this.boardPainter = boardPainter;
        this.initialGameState = initialGameState;
    }
    
    /**
     * Retourne le peintre de plateau
     * 
     * @return le peintre de plateau
     */
    public BoardPainter boardPainter() {return boardPainter;}
    
    /**
     * Retourne l'état initial
     * 
     * @return l'état initial
     */
    public GameState gameState() {return initialGameState;}
}
