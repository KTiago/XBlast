/**
 * Une bombe
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.epfl.cs108.Sq;
import ch.epfl.xblast.ArgumentChecker;
import ch.epfl.xblast.Cell;
import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.Direction;


public final class Bomb {
    
    private final PlayerID ownerId;
    private final Cell position;
    private final Sq<Integer> fuseLengths;
    private final int range;
    
    /**
     * Construit une bombe avec le propriétaire, la position, la séquence de
     * longueurs de mèche et la portée donnés
     * 
     * @param ownerId
     *            propriétaire de la bombe
     * @param position
     *            position de la bombe
     * @param fuseLengths
     *            séquence des longueurs de la mèche
     * @param range
     *            portée de la bombe
     * @throws NullPointerException
     *             si un des trois premiers paramètres est nul
     * @throws IllegalArgumentException
     *             si range est strictement négatif ou si la séquence des
     *             longueurs de mèche est vide
     */
    public Bomb(PlayerID ownerId, Cell position, Sq<Integer> fuseLengths, int range){
        this.ownerId = Objects.requireNonNull(ownerId);
        this.position = Objects.requireNonNull(position);
        this.fuseLengths = Objects.requireNonNull(fuseLengths);
        if (fuseLengths.isEmpty()) {throw new IllegalArgumentException();}
        this.range = ArgumentChecker.requireNonNegative(range);   
    }
    
    /**
     * Construit une bombe avec le propriétaire, la position et 
     * la portée donnés, et la séquence de longueur de mèche
     * 
     * @param ownerId
     *          propriétaire de la bombe
     * @param position
     *          position de la bombe
     * @param fuseLength
     *          longueur de la mèche
     * @param range
     *          portée de la bombe
     * @throws NullPointerException
     *              si ownerId ou position est nul
     * @throws IllegalArgumentException
     *              si range ou fuseLength sont strictement négatifs    
     *               
     */
    public Bomb(PlayerID ownerId, Cell position, int fuseLength, int range){
        this(ownerId, position,
                Sq.iterate(ArgumentChecker.requireNonNegative(fuseLength),x -> x - 1).limit(fuseLength),
                range);
    }
    /**
     * Retourne l'identité du propriétaire de la bombe
     * 
     * @return l'identité du propriétaire de la bombe 
     */
    public PlayerID ownerId() {
        return ownerId;
    }
    /**
     * Retourne la position de la bombe
     * 
     * @return la position de la bombe
     */
    public Cell position(){
        return position;
    }
    /**
     * Retourne la séquence des longueurs de mèche de la bombe
     * 
     * @return la séquence des longueurs de mèche
     */
    public Sq<Integer> fuseLengths(){
        return fuseLengths;
    }
    /**
     * Retour la longueur de mèche actuelle
     * 
     * @return la longueur de mèche actuelle
     */
    public int fuseLength(){
        return fuseLengths().head();
    }
    /**
     * Retourne la portée de la bombe
     * 
     * @return la portée de la bombe
     */
    public int range(){
        return range;
    }
    /**
     * Retourne l'explosion correspondant à la bombe, sous la forme 
     * d'un tableau de 4 éléments, chacun représentant un bras
     * 
     * @return l'explosion correspondant à la bombe
     */
    public List<Sq<Sq<Cell>>> explosion(){
        List<Sq<Sq<Cell>>> explosion = new ArrayList<>();
        //Crée un bras d'explosion pour chacune des 4 directions cardinales
        for(Direction dir : Direction.values()){
            explosion.add(explosionArmTowards(dir));
        }
        return explosion;
    }
    
    /**
     * Retourne le bras de l'explosion se dirigeant dans la direction dir
     * 
     * @param dir
     *          Direction du bras de l'explosion
     * @return la bras de l'explosion se dirigeant dans la direction dir
     */
    private Sq<Sq<Cell>> explosionArmTowards(Direction dir){
        return Sq.repeat(Ticks.EXPLOSION_TICKS,Sq.iterate(position, c -> c.neighbor(dir)).limit(range));
    }
}
