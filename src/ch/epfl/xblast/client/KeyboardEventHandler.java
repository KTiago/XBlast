/**
 * Un Auditeur d'événements claviers
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import ch.epfl.xblast.PlayerAction;

public final class KeyboardEventHandler extends KeyAdapter {
    
    public static final Map<Integer,PlayerAction> DEFAULT_KEY_BOARD = defaultKeyBoard();
    
    private final Map<Integer,PlayerAction> keyMap;
    private final Consumer<PlayerAction> consPlayerAction;
    
    /**
     * Construit un auditeur d'événement clavier étant donné une table associatives
     *  associant des actions de joueur à des codes de touche et un consommateur 
     *  d'action de joueur 
     * 
     * @param keyMap
     *          association des actions de joueurs à des codes de touche
     * @param consPlayerAction
     *          consommateur d'action de joueur
     * @throws NullPointerException
     *           si l'un des deux arguments est null
     */
    public KeyboardEventHandler(Map<Integer,PlayerAction> keyMap,Consumer<PlayerAction> consPlayerAction){
        this.keyMap = Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(keyMap)));
        this.consPlayerAction = Objects.requireNonNull(consPlayerAction);
    }
    
    @Override
    /**
     * Redéfinition de la méthode keyPressed afin que la pression de l'une des
     * touches dont le code fait partie des clefs de la table passée au
     * constructeur provoque l'appel de la méthode accept du consommateur
     * 
     */
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyMap.containsKey(keyCode))
            consPlayerAction.accept(keyMap.get(keyCode));
    }

    
    /**
     * Retourne la table associative par défaut pour le jeu XBlast
     * 
     * @return la table associative par défaut pour le jeu XBlast
     */
    private static Map<Integer,PlayerAction> defaultKeyBoard(){
        Map<Integer, PlayerAction> kb = new HashMap<>();
        kb.put(KeyEvent.VK_UP, PlayerAction.MOVE_N);
        kb.put(KeyEvent.VK_DOWN, PlayerAction.MOVE_S);
        kb.put(KeyEvent.VK_LEFT, PlayerAction.MOVE_W);
        kb.put(KeyEvent.VK_RIGHT, PlayerAction.MOVE_E);
        kb.put(KeyEvent.VK_SPACE, PlayerAction.DROP_BOMB);
        kb.put(KeyEvent.VK_SHIFT, PlayerAction.STOP);
        return Collections.unmodifiableMap(kb);
    }
    
}
