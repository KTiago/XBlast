/**
 * Classe utilitaire pour la vérification d'arguments entiers.
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast;

public final class ArgumentChecker {
    /*
     * Le constructeur est vide et privé afin que la classe ne soit pas instantiable
     */
    private ArgumentChecker() {
    }

    /**
     * Lance une exception si la valeur passée en paramètre est strictement
     * négative, renvoie la valeur sinon.
     * 
     * @param value
     *            valeur dont on souhaite vérifier qu'elle soit positive
     * @return valeur dont on souhaite vérifier qu'elle soit positive
     * @throws IllegalArgumentException
     *             si la valeur est strictement négative
     */
    public static int requireNonNegative(int value) {
        if (value < 0)
            throw new IllegalArgumentException();
        return value;
    }
}
