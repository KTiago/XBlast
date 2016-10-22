/**
 * Classe qui représente un joueur
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.server;

import java.util.Objects;

import ch.epfl.xblast.*;
import ch.epfl.cs108.*;

public final class Player {
    private final PlayerID id;
    private final Sq<LifeState> lifeStates;
    private final Sq<DirectedPosition> directedPos;
    private final int maxBombs;
    private final int bombRange;
    
    /**
     * Retourne une séquence de couples (nombre de vies, état) étant donné un nombre de points de vie
     * 
     * @param lives
     *          le nombre de vies du joueur
     * @return une séquence de couples (nombre de vies, état)
     */
    private static Sq<LifeState> computeLifeStates(int lives){
        //Etat de mort permanente si le nombre de points de vie reçu est négatif
        if(lives <= 0){
            return Sq.constant(new LifeState(0, LifeState.State.DEAD));
        }
        //Etat invulnérable pendant "PLAYER_INVULNERABLE_TICKS" puis vulnérable indéfiniment
        else{
            Sq<LifeState> states = Sq.repeat(Ticks.PLAYER_INVULNERABLE_TICKS, new LifeState(lives, LifeState.State.INVULNERABLE));
            return states.concat(Sq.constant(new LifeState(lives, LifeState.State.VULNERABLE)));
        }
    }

    /**
     * Construit un joueur avec les attributs données
     * 
     * @param id 
     *          l'identité du joueur     
     * @param lifeStates
     *          la séquence des couples (nombre de vies, état) du joueur
     * @param directedPos
     *          la séquence des positions dirigées du joueur
     * @param maxBombs
     *          le nombre maximum de bombes que le joueur peut déposer
     * @param bombRange
     *          la portée (en nombre de cases) des explosions produites par les bombes du joueur
     * @throw NullPointerException 
     *          si l'un des 3 premiers arguments est null
     * @throw IllegalArgumentException 
     *          si l'un des 2 derniers arguments est strictement négatif
     */
    public Player(PlayerID id, Sq<LifeState> lifeStates, Sq<DirectedPosition> directedPos, int maxBombs, int bombRange){
        this.id = Objects.requireNonNull(id);
        this.lifeStates = Objects.requireNonNull(lifeStates);
        this.directedPos = Objects.requireNonNull(directedPos);
        this.maxBombs = ArgumentChecker.requireNonNegative(maxBombs);
        this.bombRange = ArgumentChecker.requireNonNegative(bombRange);
    }
    
    /**
     * Construit un joueur avec les attributs donnés
     * 
     * @param id
     *          l'identité du joueur    
     * @param lives
     *          le nombre de vies du joueur
     * @param position
     *          la position du joueur
     * @param maxBombs
     *          le nombre maximum de bombes que le joueur peut déposer
     * @param bombRange
     *          la portée (en nombre de cases) des explosions produites par les bombes du joueur
     * @throw NullPointerException
     *          si l'identité ou la position sont nulles
     * @throw IllegalArgumentException
     *          si le nombre de vies, le nombre maximum de bombes ou leur portée est strictement négatif       
     */
    public Player(PlayerID id, int lives, Cell position, int maxBombs, int bombRange){
        this(id,
             computeLifeStates(ArgumentChecker.requireNonNegative(lives)),
             DirectedPosition.stopped(new Player.DirectedPosition(SubCell.centralSubCellOf(Objects.requireNonNull(position)),Direction.S)),
             maxBombs,
             bombRange);
    }
    
    /**
     * Retourne l'identité du joueur
     * 
     * @return l'identité du joueur
     */
    public PlayerID id(){return id;}
    
    /**
     * Retourne la séquence des couples (nombre de vies, état) du joueur
     * 
     * @return la séquence des couples (nombre de vies, état) du joueur
     */
    public Sq<LifeState> lifeStates(){return lifeStates;}
    
    /**
     * Retourne le couple (nombre de vies, état) actuel du joueur
     * 
     * @return le couple (nombre de vies, état) actuel du joueur
     */
    public LifeState lifeState(){return lifeStates().head();}
    
    /**
     * Retourne la séquence d'états pour la prochaine vie du joueur
     * 
     * @return Retourne la séquence d'états pour la prochaine vie du joueur
     */
    public Sq<LifeState> statesForNextLife(){
        Sq<LifeState> states = Sq.repeat(Ticks.PLAYER_DYING_TICKS, new LifeState(lives(), LifeState.State.DYING));
        return states.concat(computeLifeStates(lives()-1));
    }

    /**
     * Retourne le nombre de vies actuel du joueur
     * 
     * @return le nombre de vies actuel du joueur
     */
    public int lives(){return lifeState().lives();}
    
    /**
     * Retourne vrai si et seulement si le joueur est vivant (nbr de vies > 0)
     * 
     * @return true si et seulement si le joueur est vivant (nbr de vies > 0)
     */
    public boolean isAlive(){return lives() > 0;}
    
    /**
     * Retourne la séquence des positions dirigées du joueur
     * 
     * @return la séquence des positions dirigées du joueur
     */
    public Sq<DirectedPosition> directedPositions(){return directedPos;}
    
    /**
     * Retourne la position actuelle du joueur
     * 
     * @return la position actuelle du joueur
     */
    public SubCell position(){return directedPositions().head().position();}
    
    /**
     * Retourne la direction vers laquelle le joueur regarde actuellement
     * 
     * @return la direction vers laquelle le joueur regarde actuellement
     */
    public Direction direction(){return directedPositions().head().direction();}
    
    /**
     * Retourne le nombre maximum de bombes que le joueur peut déposer
     * 
     * @return le nombre maximum de bombes que le joueur peut déposer
     */
    public int maxBombs(){return maxBombs;}
    
    /**
     * Retourne un joueur identique à celui auquel on l'applique mais avec un nombre maximal de bombes donné
     * 
     * @param newMaxBombs
     *              le nouveau nombre maximal de bombes
     * @return un joueur identique à celui auquel on l'applique mais avec un nombre maximal de bombes donné
     */
    public Player withMaxBombs(int newMaxBombs){return new Player(id, lifeStates, directedPos, newMaxBombs, bombRange);}
    
    /**
     * Retourne un joueur identique à celui auquel on l'applique mais avec une portée maximale des bombes donnée
     * 
     * @param newBombRange
     *              la nouvelle portée maximale des bombes
     * @return un joueur identique à celui auquel on l'applique mais avec une portée maximale des bombes donnée
     */
    public Player withBombRange(int newBombRange){return new Player(id, lifeStates, directedPos, maxBombs, newBombRange);}

    /**
     * Retourne la portée (en nombre de cases) des explosions produites par les bombes du joueur
     * 
     * @return la portée (en nombre de cases) des explosions produites par les bombes du joueur
     */
    public int bombRange(){return bombRange;}
    
    /**
     * Retourne une bombe positionnée sur la case sur laquelle le joueur se trouve actuellement
     * 
     * @return une bombe positionnée sur la case sur laquelle le joueur se trouve actuellement
     */
    public Bomb newBomb(){return new Bomb(id, position().containingCell(), Ticks.BOMB_FUSE_TICKS, bombRange);}
    
    /**
     * Classe qui représente un couple (nombre de vies, état) du joueur
     */
    public final static class LifeState{
        /**
         * Enum qui représente différents états
         */
        public enum State{
            INVULNERABLE, VULNERABLE, DYING, DEAD;
        }

        private final int lives;
        private final State state;
        
        /**
         * Construit le couple (nombre de vies, état) avec les valeurs données
         * 
         * @param lives
         *          le nombre de vies
         * @param state
         *          l'état
         * @throws IllegalArgumentException
         *          si le nombre de vies est strictement négatif
         * @throws NullPointerException 
         *          si l'état est nul
         */
        public LifeState(int lives, State state){
            this.lives = ArgumentChecker.requireNonNegative(lives);
            this.state = Objects.requireNonNull(state);
        }
        
        /**
         * Retourne le nombre de vies du couple
         * 
         * @return le nombre de vies du couple
         */
        public int lives(){return lives;}
        
        /**
         * Retourne l'état
         * 
         * @return l'état
         */
        public State state(){return state;}
        
        /**
         * retourne vrai si et seulement si l'état permet au joueur de se déplacer (invulnérable ou vulnérable)
         * 
         * @return vrai si et seulement si l'état permet au joueur de se déplacer (invulnérable ou vulnérable)
         */
        public boolean canMove(){
            return state == State.INVULNERABLE || state == State.VULNERABLE;
        }
    }
    
    /**
     * Classe qui représente la «position dirigée» d'un joueur, c-à-d une paire (sous-case, direction)
     */
    public final static class DirectedPosition{
        private final Direction direction;
        private final SubCell position;
        
        /**
         * Retourne une séquence infinie composée de la position dirigée donnée et représentant un joueur arrêté dans cette position
         * 
         * @param p
         *          la position dirigée
         * @return une séquence infinie composée de la position dirigée donnée et représentant un joueur arrêté dans cette position
         */
        public static Sq<DirectedPosition> stopped(DirectedPosition p){
            return Sq.constant(p);
        }
        
        /**
         * Retourne une séquence infinie de positions dirigées représentant un joueur se déplaçant dans la direction dans laquelle il regarde
         *
         * @param p
         *          la position dirigée
         * @return une séquence infinie de positions dirigées représentant un joueur se déplaçant dans la direction dans laquelle il regarde
         */
        public static Sq<DirectedPosition> moving(DirectedPosition p){
            return Sq.iterate(p, u -> new DirectedPosition(u.position.neighbor(u.direction), u.direction));
        }
        
        /**
         * Construit une position dirigée avec la position et la direction
         * donnés
         * 
         * @param position
         *            la position
         * @param direction
         *            la direction
         * @throw NullPointerException si l'un des arguments est nul
         */
        public DirectedPosition(SubCell position, Direction direction){
            this.position = Objects.requireNonNull(position);
            this.direction = Objects.requireNonNull(direction);
        }

        /**
         * Retourne la position
         * 
         * @return la position
         */
        public SubCell position(){return position;}
        
        /**
         * Retourne la direction de la position dirigée
         * 
         * @return la direction de la position dirigée
         */
        public Direction direction(){return direction;}
        
        /**
         * Retourne une position dirigée dont la position est celle donnée, et la direction est identique à celle du récepteur
         * 
         * @param newPosition
         *          la nouvelle position
         * @return une position dirigée dont la position est celle donnée, et la direction est identique à celle du récepteur
         */
        public DirectedPosition withPosition(SubCell newPosition){return new DirectedPosition(newPosition, direction);}
        
        /**
         * Retourne une position dirigée dont la direction est celle donnée, et la position est identique à celle du récepteur.
         * 
         * @param newDirection
         *          la nouvelle direction
         * @return une position dirigée dont la direction est celle donnée, et la position est identique à celle du récepteur.
         */
        public DirectedPosition withDirection(Direction newDirection){return new DirectedPosition(position, newDirection);}
    }
}
