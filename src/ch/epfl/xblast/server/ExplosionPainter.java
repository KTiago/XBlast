/**
 * Peintre des explosions
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 * 
 */
package ch.epfl.xblast.server;

public final class ExplosionPainter {
    protected static final byte BYTE_FOR_EMPTY = 16;

    private static final byte BYTE_FOR_BLACK_BOMB = 20;
    private static final byte BYTE_FOR_WHITE_BOMB = 21;

    private ExplosionPainter() {}

    /**
     * Retourne l'octet identifiant l'image à utiliser en fonction de la bombe
     * donnée
     * 
     * @param bomb
     *            la bombe
     * @return l'octet identifiant l'image à utiliser
     */
    public static byte byteForBomb(Bomb bomb) {
        return (byte) (Integer.bitCount(bomb.fuseLength()) == 1
                ? BYTE_FOR_WHITE_BOMB : BYTE_FOR_BLACK_BOMB);
    }

    /**
     * Retourne l'octet identifiant l'image à utiliser en fonction de la
     * présence ou non d'explosions environnantes
     * 
     * @param N
     *            la case voisine au nord
     * @param E
     *            la case voisine à l'est
     * @param S
     *            la case voisine au sud
     * @param W
     *            la case voisine à l'ouest
     * @return l'octet identifiant l'image à utiliser
     */
    public static byte byteForBlast(boolean N, boolean E, boolean S, boolean W) {
        return (byte) ((toInt(N) << 3) | (toInt(E) << 2) | (toInt(S) << 1) | toInt(W));
    }

    //transforme un boolean en entier : vrai vaut 1, faux vaut 0
    private static int toInt(boolean b) {
        return b ? 1 : 0;
    }
}
