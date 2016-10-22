/**
 * Un plateau de jeu
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast.server;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import ch.epfl.cs108.*;
import ch.epfl.xblast.Cell;
import ch.epfl.xblast.Lists;

public final class Board {
    private final List<Sq<Block>> blocks;

    /**
     * Construit un plateau constant avec la matrice de blocs donnée
     * 
     * @param rows
     *            une matrice de blocs
     * @return le plateau de jeu ainsi construit
     * @throws IllegalArgumentException
     *             si la liste reçue n'est pas constituée de 13 listes de 15
     *             éléments chacune
     */
    public static Board ofRows(List<List<Block>> rows) {
        checkBlockMatrix(rows, Cell.ROWS, Cell.COLUMNS);
        
        List<Sq<Block>> blocks = new ArrayList<>();
        for (List<Block> l : rows) {
            for (Block b : l) {
                blocks.add(Sq.constant(b));
            }
        }
        return new Board(blocks);
    }

    /**
     * Construit un plateau muré avec les blocs intérieurs donnés
     * 
     * @param innerBlocks
     *            les blocs intérieurs.
     * @return le plateau de jeu ainsi construit
     * @throws IllegalArgumentException
     *             si la liste reçue n'est pas constituée de 11 listes (ROWS-2)
     *             de 13 (COLUMNS-2) éléments chacune
     */
    public static Board ofInnerBlocksWalled(List<List<Block>> innerBlocks) {
        checkBlockMatrix(innerBlocks, Cell.ROWS - 2, Cell.COLUMNS - 2);

        List<Sq<Block>> blocks = new ArrayList<>();
        // ligne de mur supérieure
        blocks.addAll(Collections.nCopies(Cell.COLUMNS,
                Sq.constant(Block.INDESTRUCTIBLE_WALL)));
        for (List<Block> l : innerBlocks) {
            // mur de gauche
            blocks.add(Sq.constant(Block.INDESTRUCTIBLE_WALL));
            for (Block b : l) {
                blocks.add(Sq.constant(b));
            }
            // mur de droite
            blocks.add(Sq.constant(Block.INDESTRUCTIBLE_WALL));
        }
        // ligne de mur inférieure
        blocks.addAll(Collections.nCopies(Cell.COLUMNS,
                Sq.constant(Block.INDESTRUCTIBLE_WALL)));
        return new Board(blocks);
    }

    /**
     * Construit un plateau muré symétrique avec les blocs du quadrant
     * nord-ouest donnés
     * 
     * @param quadrantNWBlocks
     *            les blocs intérieurs du quadrant nord-ouest
     * @return le plateau de jeu ainsi construit
     * @throws IllegalArgumentException
     *             si la liste reçue n'est pas constituée de 6 listes de 7
     *             éléments chacune
     */
    public static Board ofQuadrantNWBlocksWalled(List<List<Block>> quadrantNWBlocks) {
        checkBlockMatrix(quadrantNWBlocks, 6, 7);

        List<List<Block>> innerBlocks = new ArrayList<>();
        for (List<Block> l : quadrantNWBlocks) {
            innerBlocks.add(Lists.mirrored(l));
        }
        return ofInnerBlocksWalled(Lists.mirrored(innerBlocks));
    }

    /**
     * Construit un plateau de jeu avec un tableau bidimmensionnel de séquences
     * de blocs fourni en paramètre.
     * 
     * @param blocks
     *            tableau bidimmensionnel de séquences de blocs
     * @throws IllegalArgumentException
     *             si la liste passée en argument ne contient pas 195 éléments
     */
    public Board(List<Sq<Block>> blocks) {
        if (blocks.size() != Cell.COUNT) 
            throw new IllegalArgumentException();
        
        this.blocks = Collections.unmodifiableList(new ArrayList<>(blocks));
    }

    /**
     * Retourne la séquence des blocs pour la case donnée
     * 
     * @param c
     *            la case dont on cherche la séquence
     * @return la séquence des blocs pour la case donnée
     */
    public Sq<Block> blocksAt(Cell c) {
        return blocks.get(c.rowMajorIndex());
    }

    /**
     * Retourne le bloc pour la case donnée
     * 
     * @param c
     *            la case dont on cherche la séquence
     * @return le bloc pour la case donnée
     */
    public Block blockAt(Cell c) {
        return blocksAt(c).head();
    }

    /**
     * Lève l'exception IllegalArgumentException si la matrice donnée ne
     * contient pas rows éléments, contenant chacun columns blocs
     * 
     * @param matrix
     *            la matrice données
     * @param rows
     *            le nombre de lignes
     * @param columns
     *            le nombre de colonnes
     * @throws IllegalArgumentException
     *             si la matrice donnée ne contient pas rows éléments et chacun
     *             d'eux columns blocs
     */
    private static void checkBlockMatrix(List<List<Block>> matrix, int rows,
            int columns) {
        boolean isCorrect = matrix.size() == rows;
        for (List<Block> l : matrix) {
            if (l.size() != columns)
                isCorrect = false;
        }
        if (!isCorrect)
            throw new IllegalArgumentException();
    }
}
