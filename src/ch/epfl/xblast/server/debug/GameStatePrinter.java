package ch.epfl.xblast.server.debug;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import ch.epfl.xblast.Cell;
import ch.epfl.xblast.server.Block;
import ch.epfl.xblast.server.Board;
import ch.epfl.xblast.server.Bomb;
import ch.epfl.xblast.server.GameState;
import ch.epfl.xblast.server.Player;

public final class GameStatePrinter {
    private GameStatePrinter() {}
    static private String red = "\u001b[31m";
    static private String redbg = "\u001b[41m";
    static private String black = "\u001b[30m";
    static private String blackbg = "\u001b[40m";
    static private String whitebg = "\u001b[47m";
    static private String white   = "\u001b[37m";
    static private String greenbg = "\u001b[42m";
    static private String bluebg = "\u001b[44m";
    static private String stop = "\u001b[m";

    public static void printGameState(GameState s) {
        List<Player> ps = s.alivePlayers();
        Board board = s.board();
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < Cell.ROWS; ++y) {
            xLoop: for (int x = 0; x < Cell.COLUMNS; ++x) {
                Cell c = new Cell(x, y);
                for (Player p: ps) {
                    if (p.position().containingCell().equals(c)) {
                        builder.append(stringForPlayer(p));
                        continue xLoop;
                    }
                }
                Block b = board.blockAt(c);
                boolean bomb = true;
                for (Map.Entry<Cell, Bomb> entry : s.bombedCells().entrySet()){
                    if(entry.getKey().equals(c)){
                        bomb = false;
                        builder.append(" O~ ");
                    }
                }
                
                boolean blast = true;
                for (Cell cell : s.blastedCells()){
                    if(cell.equals(c)){
                        blast = false;
                        if(b.equals(Block.INDESTRUCTIBLE_WALL)){
                           builder.append(blackbg+red+" ** "+stop);
                        }
                        else if(b.equals(Block.CRUMBLING_WALL)){
                            builder.append(blackbg+white+" ;; "+stop);
                        }
                        else{builder.append(redbg+white+" ** "+stop);}
                    }
                }
                if(bomb&&blast)
                    builder.append(stringForBlock(b));
            }
            builder.append("\n");
        }
        for(Player p : ps){
            builder.append(p.id()+" : "+p.lives()+" vies ("+p.lifeState().state()+")");
            builder.append("\n           bombes max : "+p.maxBombs()+", portee : "+p.bombRange());
            builder.append("\n           position : "+p.position().containingCell()+" +/- "+p.position().distanceToCentral()+"\n");
        }
        builder.append("Remaining Time : "+ String.format("%.1f", s.remainingTime())+"\n");
        String string = builder.toString();
        System.setOut( new PrintStream(new BufferedOutputStream(System.out)));
        System.out.print(string);
        System.out.flush();
    }

    private static String stringForPlayer(Player p) {
        StringBuilder b = new StringBuilder();
        b.append(bluebg+white+(p.id().ordinal() + 1));
        switch(p.lifeState().state()){
        case VULNERABLE : b.append('V'); break;
        case INVULNERABLE : b.append('I'); break;
        case DYING : b.append('D'); break;
        case DEAD : b.append('X'); break;
        }
        b.append(p.lives());
        switch (p.direction()) {
        case N: b.append('^'); break;
        case E: b.append('>'); break;
        case S: b.append('v'); break;
        case W: b.append('<'); break;
        }
        b.append(stop);
        return b.toString();
    }

    private static String stringForBlock(Block b) {
        switch (b) {
        case FREE: return whitebg+"    "+stop;
        case INDESTRUCTIBLE_WALL: return blackbg+"    "+stop;
        case DESTRUCTIBLE_WALL: return blackbg+white+" :: "+stop;
        case CRUMBLING_WALL: return blackbg+white+" ;; "+stop;
        case BONUS_BOMB: return greenbg+white+" Q+ "+stop;
        case BONUS_RANGE: return greenbg+white+" +R "+stop;
        default: throw new Error();
        }
    }
}
