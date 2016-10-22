/**
 * Enum pour les 4 types de blocks
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast.server;

import java.util.NoSuchElementException;

public enum Block {
    FREE, INDESTRUCTIBLE_WALL, DESTRUCTIBLE_WALL, CRUMBLING_WALL,
    BONUS_BOMB(Bonus.INC_BOMB),
    BONUS_RANGE(Bonus.INC_RANGE);
    
    //bonus correspondant, ou null
    private Bonus maybeAssociatedBonus;
    
    /**
     * Constructeur principal, utilisé par les blocs bonus
     * 
     * @param maybeAssociatedBonus
     *            le bloc bonus associé
     */
    private Block(Bonus maybeAssociatedBonus) {
        this.maybeAssociatedBonus = maybeAssociatedBonus;
    }
    
    /**
     * Constructeur par défaut, utilisé par les autres blocs initialise le bonus
     * associé à null
     */
    private Block() {
        this.maybeAssociatedBonus = null;
    }
    
    /**
     * Retourne vrai si et seulement si la case est libre
     * 
     * @return true si et seulement si la case actuelle est FREE
     */
    public boolean isFree(){
        return this == FREE;
    }
    
    /**
     * Retourne vrai si et seulement si la case peut héberger le joueur
     * 
     * @return true si et seulement si la case peut héberger le joueur (case
     *         libre)
     */
    public boolean canHostPlayer(){
        return isFree() || isBonus();
    }
    
    /**
     * Retourne vrai si et seulement si la case projette une ombre sur le
     * plateau de jeu
     * 
     * @return true si et seulement si la casse projette une ombre sur le
     *         plateau de jeu (cases de type *_WALL)
     */
    public boolean castsShadow(){
        return this == INDESTRUCTIBLE_WALL || this == DESTRUCTIBLE_WALL || this == CRUMBLING_WALL;
    }
    
    /**
     * Retourne vrai si et seulement si la case est un bonus
     * 
     * @return true si et seulement si la case est un bonus (cases de type
     *         BONUS_*)
     */
    public boolean isBonus(){
        return this == BONUS_BOMB || this == BONUS_RANGE;
    }
    
    /**
     * Retourne le bonus associé au block si il existe, lance une exception sinon
     * 
     * @return le bonus associé
     * @throws NoSuchElementException si aucun bonnus n'est associé à ce bloc
     */
    public Bonus associatedBonus(){
        if(maybeAssociatedBonus == null)
            throw new NoSuchElementException();
        else
            return maybeAssociatedBonus;
    }
}
