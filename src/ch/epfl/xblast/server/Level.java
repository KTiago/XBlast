/**
 * Classe qui modélise un niveau
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.server;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            Arrays.asList(__, __, __, __, __, xx, __),
            Arrays.asList(__, XX, xx, XX, xx, XX, xx),
            Arrays.asList(__, xx, __, __, __, xx, __),
            Arrays.asList(xx, XX, __, XX, XX, XX, XX),
            Arrays.asList(__, xx, __, xx, __, __, __),
            Arrays.asList(xx, XX, xx, XX, xx, XX, __)));
        
        final int nbrLifeByDefault = 3;
        final int nbrBombByDefault = 2;
        final int bombRangeByDefault = 3;
        final List<Cell> startingCells = new ArrayList<>(Arrays.asList(new Cell(1, 1),
                new Cell(13, 1), new Cell(13, 11), new Cell(1, 11)));        

        List<Player> players = new ArrayList<Player>();
        for(int i = 0; i < PlayerID.values().length; ++i){
            players.add(new Player(PlayerID.values()[i], nbrLifeByDefault,
                    startingCells.get(i), nbrBombByDefault, bombRangeByDefault));
        }
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
        this.boardPainter = Objects.requireNonNull(boardPainter);
        this.initialGameState = Objects.requireNonNull(initialGameState);
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
