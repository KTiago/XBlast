package ch.epfl.xblast.client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import ch.epfl.xblast.PlayerAction;

public final class KeyboardEventHandler extends KeyAdapter {
    
    public static final Map<Integer,PlayerAction> DEFAULT_KEY_BOARD = Collections.unmodifiableMap(defaultKeyBoard());
    
    private final Map<Integer,PlayerAction> keyMap;
    private final Consumer<PlayerAction> consPlayerAction;
    
    public KeyboardEventHandler(Map<Integer,PlayerAction> keyMap,Consumer<PlayerAction> consPlayerAction){
        this.keyMap = keyMap;
        this.consPlayerAction = consPlayerAction;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        consPlayerAction.accept(keyMap.get(e.getKeyCode()));
    }

    private static Map<Integer,PlayerAction> defaultKeyBoard(){
        Map<Integer, PlayerAction> kb = new HashMap<>();
        kb.put(KeyEvent.VK_UP, PlayerAction.MOVE_N);
        kb.put(KeyEvent.VK_DOWN, PlayerAction.MOVE_S);
        kb.put(KeyEvent.VK_LEFT, PlayerAction.MOVE_W);
        kb.put(KeyEvent.VK_RIGHT, PlayerAction.MOVE_E);
        kb.put(KeyEvent.VK_SPACE, PlayerAction.DROP_BOMB);
        kb.put(KeyEvent.VK_SHIFT, PlayerAction.STOP);
        return kb;
    }
    
}
