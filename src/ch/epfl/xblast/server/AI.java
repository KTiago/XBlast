package ch.epfl.xblast.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import ch.epfl.xblast.Direction;
import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.SubCell;

public class AI {
    private final static int PROBABILITY = 100;
    private final PlayerID id;
    private Direction lastDirection = Direction.S;
    private boolean blockedByBomb = false;
    private boolean blockedByBlast = false;
    private Random rng;

    public AI(PlayerID id, Random rng) {
        this.id = id;
        this.rng = rng;
    }

    public AI copy(){
        AI copy = new AI(this.id, this.rng);
        copy.lastDirection = this.lastDirection;
        copy.blockedByBlast = this.blockedByBlast;
        copy.blockedByBomb = this.blockedByBomb;
        return copy;
    }
    
    public boolean dropBomb(GameState gameState){
        if(rng.nextInt(PROBABILITY) == 1){
            Player ai = findPlayer(gameState);
            GameState g = gameState;
            Set<PlayerID> bombDropEvents = new HashSet<>();
            bombDropEvents.add(id);
            Map<PlayerID, Optional<Direction>> speedChangeEvents = new HashMap<>();
            g = g.next(speedChangeEvents, bombDropEvents);
            AI copy = this.copy();
            
            //on fait évoluer une copie du gameState pour savoir si l'AI perd
            //une vie en posant une bombe.
            for(int i = 0; i < Ticks.PLAYER_DYING_TICKS + Ticks.BOMB_FUSE_TICKS + Ticks.EXPLOSION_TICKS; ++i){
                speedChangeEvents.put(id, Optional.of(copy.move(g)));
                g = g.next(speedChangeEvents,
                        EnumSet.noneOf(PlayerID.class));
                speedChangeEvents.clear();
            }
            Player aiAfter = findPlayer(g);
            if(aiAfter.lives() == ai.lives()){
                return true;}
        }
        return false;
    }
    
    public Direction move(GameState gameState) {
        Player ai = findPlayer(gameState);

        //récupère les directions possibles
        //on ne garde que les cases libres
        //sur lesquelles il n'y a pas d'explosion
        //et seulement si l'AI se trouve sur la case centrale
        List<Direction> freeDirections = 
                Arrays.asList(Direction.values()).stream()
                .filter(y -> ai.position().isCentral())
                .filter(y -> gameState.board()
                                .blockAt(ai.position()
                                .containingCell()
                                .neighbor(y))
                                .canHostPlayer())
                .filter(y -> !gameState.bombedCells()
                                       .containsKey(ai.position().containingCell().neighbor(y)))
                .filter(y -> !gameState.blastedCells()
                        .contains(ai.position().containingCell().neighbor(y)))
                .collect(Collectors.toList());

        //si il n'y a aucun choix (arrive souvent, quand le joueur n'est pas sur une case
        //centrale par exemple
        if (freeDirections.isEmpty()) {
            SubCell nextPos = ai.directedPositions().tail().head()
                    .position();
            if (nextPos.distanceToCentral() <= ai.position()
                    .distanceToCentral()){
                if (gameState.bombedCells().containsKey(
                        nextPos.containingCell()) && !blockedByBomb) {
                    
                    lastDirection = ai.directedPositions().head().direction().opposite();
                    blockedByBomb = true;
                }
                if (gameState.blastedCells().contains(
                        nextPos.containingCell()) && !blockedByBlast) {
                    lastDirection = lastDirection.opposite();
                    blockedByBlast = true;
                }
            }
            return lastDirection;
        }
        blockedByBomb = false;
        blockedByBlast = false;
        freeDirections = removeThreatening(gameState, freeDirections);

        if (freeDirections.size() > 1) {
            Iterator<Direction> it = freeDirections.iterator();
            while (it.hasNext()) {
                if (it.next() == lastDirection.opposite()) {
                    it.remove();
                }
            }
        }
        Random randomizer = new Random();
        lastDirection = freeDirections
                .get(randomizer.nextInt(freeDirections.size()));
        return lastDirection;
    }
    
    private List<Direction> removeThreatening(GameState gameState, List<Direction> directions){
        Player p = findPlayer(gameState);
        GameState g = gameState;
        List<Direction> freeDirections = new ArrayList<>(directions);
        for(int i = 1; i <= Ticks.BOMB_FUSE_TICKS + Ticks.EXPLOSION_TICKS; i += 1){
            g = g.next(new EnumMap<>(PlayerID.class),
                    EnumSet.noneOf(PlayerID.class));
            List<Direction> directionsCopy = new ArrayList<>(freeDirections);
            Iterator<Direction> it = directionsCopy.iterator();
            while(it.hasNext()){
                if(g.blastedCells().contains(p.position().containingCell().neighbor(it.next())))
                    it.remove();
            }
            if(!directionsCopy.isEmpty()){
                freeDirections = directionsCopy;
            } else {
                return freeDirections;
            }
        }
        return freeDirections;
    }

    private Player findPlayer(GameState gameState){
        return gameState.players().stream()
                .filter(y -> y.id().equals(id))
                .collect(Collectors.toList()).get(0);
    }
}
