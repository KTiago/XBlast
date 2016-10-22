/**
 * Des constantes relatives au temps
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast;

public interface Time {
    public static final int S_PER_MIN = 60;
    public static final int MS_PER_S = 1000;
    public static final int US_PER_S = 1000 * MS_PER_S;
    public static final int NS_PER_S = 1000 * US_PER_S;
    public static final int NS_PER_MS = NS_PER_S / MS_PER_S;
}
