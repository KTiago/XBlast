/**
 * Une sous-case
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast;

public final class SubCell {
    
    private final static int NBROFSUBCELLINCELL = 16;
    private final static int COLUMNS = NBROFSUBCELLINCELL * Cell.COLUMNS;
    private final static int ROWS = NBROFSUBCELLINCELL * Cell.ROWS;

    private final int x;
    private final int y;

    /**
     * Retourne la sous-case centrale d'une case donnée.
     * 
     * @param cell
     *            la case dont on veut connaître le centre
     * 
     * @return la sous-case centrale de la case
     */
    public static SubCell centralSubCellOf(Cell cell) {
        int x = cell.x() * NBROFSUBCELLINCELL + NBROFSUBCELLINCELL / 2;
        int y = cell.y() * NBROFSUBCELLINCELL + NBROFSUBCELLINCELL / 2;
        return new SubCell(x, y);
    }

    /**
     * Construit une sous-case avec les coordonnées x et y données normalisées.
     *
     * @param x
     *            la coordonnée x
     * @param y
     *            la coordonnée y
     */
    public SubCell(int x, int y) {
        this.x = Math.floorMod(x, COLUMNS);
        this.y = Math.floorMod(y, ROWS);
    }

    /**
     * Retourne la coordonnée x de la case
     * 
     * @return la coordonnée x de la case
     */
    public int x() {
        return x;
    }

    /**
     * Retourne la coordonnée y de la case
     * 
     * @return la coordonnée y de la case
     */
    public int y() {
        return y;
    }

    /**
     * Retourne la distance de Manhattan jusqu'à la sous-case centrale depuis cette sous-case
     * 
     * @return distance de Manhattan de cette sous-case jusqu'à la sous-case centrale
     */
    public int distanceToCentral() {
        return Math.abs(centralSubCellOf(containingCell()).x() - x)
                + Math.abs(centralSubCellOf(containingCell()).y() - y);
    }

    /**
     * Retourne true si cette sous-case est la sous-case centrale
     * 
     * @return true si et seulement si la sous-case est la sous-case centrale
     */
    public boolean isCentral() {
        return distanceToCentral() == 0;
    }

    /**
     * Retourne la sous-case voisine dans une direction donnée
     * 
     * @param d
     *            direction d donnée
     * @return la sous-case voisine dans la direction d
     * @throws Error
     *             si d n'est pas une des 4 directions cardinales
     */
    public SubCell neighbor(Direction d) {
        switch (d) {
        case N:
            return new SubCell(x, y - 1);
        case E:
            return new SubCell(x + 1, y);
        case S:
            return new SubCell(x, y + 1);
        case W:
            return new SubCell(x - 1, y);
        default:
            throw new Error();
        }
    }

    /**
     * Retourne la case contenant cette sous-case
     * 
     * @return la case contenant cette sous-case
     */
    public Cell containingCell() {
        return new Cell(x / NBROFSUBCELLINCELL, y / NBROFSUBCELLINCELL);
    }

    @Override
    /**
     * Redéfinition de la méthode equals de Object qui compare les classes et
     * les coordonnées
     * 
     * @return true si et seulement si les objets sont identiques
     */
    public boolean equals(Object that) {
        if(that == null) return false;
        if(that == this) return true;
        
        // On teste si l'objet est de classe SubCell et, dans
        // le cas échéant, si les coordonnées sont les mêmes
        return that.getClass() == getClass() && 
                (((SubCell) that).x() == x && ((SubCell) that).y() == y);
    }

    @Override
    /**
     * Redéfinition de la méthode toString qui renvoie un String avec les
     * coordonnées de la sous-case entre parenthèses
     * 
     * @return un String avec les coordonnées de la sous-case entre parenthèses
     */
    public String toString() {
        return "(" + x + "," + y + ")";
    }
    
    @Override
    /**
     * Redéfinition de la méthode hashCode qui retourne une valeur de hachage
     * pour la sous-case
     * 
     * @return une valeur de hachage pour la sous-case
     */
    public int hashCode(){
        return y * COLUMNS + x;
    }
}
