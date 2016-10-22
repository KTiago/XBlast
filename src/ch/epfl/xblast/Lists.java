/**
  * Lists contient des méthodes travaillant sur les listes
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public final class Lists {

    /*
     * Le constructeur est vide et privé afin que la classe ne soit pas instantiable
     */
    private Lists() {}

    /**
     * Retourne une version symétrique de la liste donnée, ou lève l'exception IllegalArgumentException
     * si la liste est vide
     * 
     * @param t
     *            une list d'objets génériques
     * @return la version symétrique de la liste donnée en paramètre
     * @throws IllegalArgumentException
     *             si la liste est vide
     */
    public static <T> List<T> mirrored(List<T> t) {
        if (t.isEmpty()) {
            throw new IllegalArgumentException();
        } else {
            //Crée une liste à partir de la liste donnée sans le dernier élément
            //l'inverse puis ajoute le contenu de la liste donnée au début
            List<T> mirroredList = new LinkedList<>(t.subList(0, t.size() - 1));
            Collections.reverse(mirroredList);
            mirroredList.addAll(0, t);
            return mirroredList;
        }
    }

    /**
     * Retourne les permutations de la liste donnée en argument dans un ordre quelconque
     * 
     * @param l
     *            liste dont on calcule les permutations.
     * @return une liste qui contient chaque permutation de la liste donnée en argument
     */
    public static <T> List<List<T>> permutations(List<T> l) {
        //Création de la liste des permutations
        List<List<T>> list = new LinkedList<>();
        
        //Si la liste donnée en argument est vide, on retourne une liste contenant une liste vide
        if (l.isEmpty()) {
            list.add(new ArrayList<T>(l));
            return list;
        } else {
            //On calcule récursivement les permutations de la liste amuputée de son premier élément
            list = permutations(l.subList(1, l.size()));
            
            //Création d'une nouvelle liste formée de "list" avec le premier
            //élément placé à chaque position possible.
            List<List<T>> permutations = new LinkedList<>();
            T firstElement = l.get(0);
            for (List<T> x : list) {
                for (int i = 0; i < x.size() + 1; ++i) {
                    List<T> temp = new LinkedList<T>(x);
                    temp.add(i, firstElement);
                    permutations.add(temp);
                }
            }
            return permutations;
        }
    }
}
