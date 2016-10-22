/**
 * Une case
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.lang.Math;

public final class Cell {
    public final static int COLUMNS = 15;
    public final static int ROWS = 13;
    public final static int COUNT = COLUMNS * ROWS;

    public final static List<Cell> ROW_MAJOR_ORDER = Collections
            .unmodifiableList(rowMajorOrder());
    public final static List<Cell> SPIRAL_ORDER = Collections
            .unmodifiableList(spiralOrder());

    private final int x;
    private final int y;

    /**
     * Calcule et retourne un tableau dynamique des cases dans l'ordre de
     * lecture.
     * 
     * @return tableau dynamique des cases dans l'ordre de lecture
     */
    private static ArrayList<Cell> rowMajorOrder() {
        ArrayList<Cell> rowMajorOrder = new ArrayList<Cell>();
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                rowMajorOrder.add(new Cell(x, y));
            }
        }
        return rowMajorOrder;
    }

    /**
     * Calcule et retourne un tableau dynamique des cases dans l'ordre en
     * spirale.
     * 
     * @return tableau dynamique des cases dans l'ordre en spirale
     */
    private static ArrayList<Cell> spiralOrder() {
        // Précisément selon l'algorithme présenté dans l'étape 1

        LinkedList<Integer> ix = new LinkedList<>();
        for (int i = 0; i < COLUMNS; i++) {
            ix.add(i);
        }
        LinkedList<Integer> iy = new LinkedList<>();
        for (int j = 0; j < ROWS; j++) {
            iy.add(j);
        }
        boolean horizontal = true;

        ArrayList<Cell> spiralOrder = new ArrayList<>();

        LinkedList<Integer> i1;
        LinkedList<Integer> i2;
        int c2;

        while (!(ix.isEmpty() || iy.isEmpty())) {
            if (horizontal) {
                i1 = ix;
                i2 = iy;
            } else {
                i1 = iy;
                i2 = ix;
            }
            c2 = i2.poll();
            for (int c1 : i1) {
                spiralOrder.add(horizontal ? new Cell(c1, c2) : new Cell(c2, c1));
            }
            Collections.reverse(i1);
            horizontal = !horizontal;
        }
        return spiralOrder;
    }

    /**
     * Construit une case avec les coordonnées x et y données normalisées.
     *
     * @param x
     *            la coordonnée x
     * @param y
     *            la coordonnée y
     */
    public Cell(int x, int y) {
        this.x = Math.floorMod(x, COLUMNS);
        this.y = Math.floorMod(y, ROWS);
    }

    /**
     * Retourne la coordonnée x de la case.
     * 
     * @return la coordonnée x de la case
     */
    public int x() {
        return x;
    }

    /**
     * Retourne la coordonnée y de la case.
     * 
     * @return la coordonnée y de la case
     */
    public int y() {
        return y;
    }

    /**
     * Retourne l'index de la case dans l'ordre de lecture.
     * 
     * @return l'index de la case dans l'ordre de lecture
     */
    public int rowMajorIndex() {
        return y * COLUMNS + x;
    }

    /**
     * Retourne une nouvelle case (objet Cell) dans la direction donnée en
     * paramètre.
     * 
     * @param dir
     *            la direction dir
     * @return une case dans la direction dir
     * @throws Error
     *             si dir n'est pas pas une des 4 directions cardinales
     */
    public Cell neighbor(Direction dir) {
        switch (dir) {
        case N:
            return new Cell(x, y - 1);
        case E:
            return new Cell(x + 1, y);
        case S:
            return new Cell(x, y + 1);
        case W:
            return new Cell(x - 1, y);
        default:
            throw new Error();
        }
    }

    @Override
    /**
     * Redéfinition de la méthode equals qui compare les classes et les index
     * des cases dans l'ordre de lecture.
     * 
     * @return true si et seulement si les objets sont identiques
     */
    public boolean equals(Object that) {
        if (that == null) return false;
        if (that == this) return true;

        // On teste si l'objet est de classe Cell et, dans
        // le cas échéant, si les coordonnées sont les mêmes
        return that.getClass() == getClass()
                && this.rowMajorIndex() == ((Cell) that).rowMajorIndex();
    }

    @Override
    /**
     * Redéfinition de la méthode toString qui retourne les coordonnées x et y
     * entre parenthèses.
     * 
     * @return un chaîne de caractères qui décrit la position de la case
     */
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    /**
     * Redéfinition de la méthode hashCode qui retourne une valeur de hachage
     * pour la case
     * 
     * @return une valeur de hachage pour la case
     */
    public int hashCode() {
        return rowMajorIndex();
    }
}
