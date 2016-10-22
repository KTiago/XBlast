/**
 * Enum pour les 4 directions cardinales
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast;

public enum Direction {
    N, E, S, W;

    /**
     * Retourne la direction opposée de celle à laquelle on l'applique
     * 
     * @return la direction opposée de celle à laquelle on l'applique
     * @throws Error
     *             si ce n'est pas une des 4 directions cardinales
     */
    public Direction opposite() {
        switch (this) {
        case N:
            return S;
        case E:
            return W;
        case S:
            return N;
        case W:
            return E;
        default:
            throw new Error();
        }
    }

    /**
     * Retourne vrai si et seulement si la direction est horizontale (Est,
     * Ouest)
     * 
     * @return true si et seulement si la direction est horizontale (Est, Ouest)
     */
    public boolean isHorizontal() {
        return this == W || this == E;
    }

    /**
     * Retourne vrai si et seulement si that et la direction sont parallèles
     * 
     * @param that
     *            Direction donnée
     * 
     * @return true si et seulement si that et la direction sont parallèles
     */
    public boolean isParallelTo(Direction that) {
        return this == that || this.opposite() == that;
    }
}
