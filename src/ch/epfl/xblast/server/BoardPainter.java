/**
 * Peintre du plateau
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 * 
 */

package ch.epfl.xblast.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.epfl.xblast.Cell;
import ch.epfl.xblast.Direction;

public final class BoardPainter {

    private final Map<Block, BlockImage> palette;
    private final BlockImage freeShadow;

    /**
     * Construit un peintre de jeu avec les attributs donnés
     * 
     * @param palette
     *            associe les images à leur bloc correspondant
     * @param freeShadow
     *            image à utiliser pour les blocs libre ombrés
     * @throws NullPointerException
     *             si l'un des arguments est null
     */
    public BoardPainter(Map<Block, BlockImage> palette, BlockImage freeShadow) {
        this.freeShadow = Objects.requireNonNull(freeShadow);
        this.palette = Collections.unmodifiableMap(
                new HashMap<>(Objects.requireNonNull(palette)));
    }

    /**
     * Retourne l'octet identifiant l'image à utiliser 
     * pour la case en question
     * 
     * @param board
     *          le plateau de jeu
     * @param c
     *          la case
     * @return l'octet identifiant l'image
     */
    public byte byteForCell(Board board, Cell c) {
        //Si le block est libre et a un mur à l'ouest renvoie un block ombré.
        //renvoie le block correspondant dans la palette sinon
        Boolean isShadowed = board.blockAt(c).isFree()
                && board.blockAt(c.neighbor(Direction.W)).castsShadow();
        return (byte) (isShadowed ? freeShadow : palette.get(board.blockAt(c))).ordinal();
    }

}
