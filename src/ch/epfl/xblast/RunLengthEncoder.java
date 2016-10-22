/**
 * Une classe utilitaire pour l'encodage par plages
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class RunLengthEncoder {
    private final static int MAX_RUN_LENGTH = 130;
    private final static int MAX_UNENCODED_RUN_LENGTH = 2;
    
    private RunLengthEncoder() {}

    /**
     * Encode par plage la liste d'octets fournie en paramètre
     * 
     * @param list
     *            la liste d'octets
     * @return la version encodée de la liste fournie en paramètre
     * @throws IllegalArgumentException
     *             si l'un des éléments de la liste est strictement négatif
     */
    public static List<Byte> encode(List<Byte> list) {
        List<Byte> encoded = new ArrayList<>();
        int runLength = 0;
        Byte previous = list.get(0);
        for (Byte b : list) {
            ArgumentChecker.requireNonNegative(b);
            if (b == previous && runLength < MAX_RUN_LENGTH)
                runLength++;
            else {
                addToCode(runLength, previous, encoded);
                runLength = 1;
                previous = b;
            }
        }
        addToCode(runLength, previous, encoded);
        return Collections.unmodifiableList(encoded);
    }

    /**
     * Décode la liste d'octets fournie en paramètre. C'est l'opération
     * réciproque de la méthode "encode"
     * 
     * @param list
     *            la liste à décoder
     * @return la liste d'octets fournie en paramètre décodée
     * @throws IllegalArgumentException
     *             si le dernier élément de la liste est strictement négatif
     */
    public static List<Byte> decode(List<Byte> list) {
        List<Byte> decoded = new ArrayList<>();
        Iterator<Byte> it = list.iterator();
        Byte b;
        while (it.hasNext()) {
            b = it.next();
            if (b < 0) {
                if (!it.hasNext())
                    throw new IllegalArgumentException();
                decoded.addAll(Collections.nCopies(MAX_UNENCODED_RUN_LENGTH - b, it.next()));
            } else {
                decoded.add(b);
            }
        }
        return Collections.unmodifiableList(decoded);
    }

    
    private static void addToCode(int count, byte previous, List<Byte> encoded) {
        if (count <= MAX_UNENCODED_RUN_LENGTH) {
            encoded.addAll(Collections.nCopies(count, previous));
        } else {
            encoded.add((byte)(MAX_UNENCODED_RUN_LENGTH - count));
            encoded.add(previous);
        }
    }
}
