package ch.epfl.xblast.client;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import ch.epfl.xblast.PlayerAction;
import ch.epfl.xblast.PlayerID;

public class Main {
    private static DatagramChannel channel;
    private static final int GAMESTATE_MAX_SIZE = 409;
    private static XBlastComponent xbc;
    private static SocketAddress address;
    private static PlayerID id;
    
    public static void main(String[] args) throws IOException,
            InterruptedException, InvocationTargetException, UnsupportedAudioFileException, LineUnavailableException {
        // Le nom d'hôte est local host par défaut, ou récupère celui passé en
        // argument si il y en a un
        String hostName = "localhost";
        if (args.length > 1)
            throw new IllegalArgumentException();
        else if (args.length == 1)
            hostName = args[0];

        // Création du canal de communication UDP
        channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.configureBlocking(false);
        address = new InetSocketAddress(hostName, 2016);

        // Buffer qui contient l'octet correspondant à une demande pour
        // rejoindre la partie
        ByteBuffer bufferJoinGame = ByteBuffer.allocate(1);
        bufferJoinGame.put((byte) PlayerAction.JOIN_GAME.ordinal());
        bufferJoinGame.flip();

        // Buffer qui contient le gameState
        ByteBuffer bufferGameState = ByteBuffer.allocate(GAMESTATE_MAX_SIZE + 1);
        SocketAddress senderAddress;
        // Tant que l'on a pas reçu le premier gameState, senderAddress est
        // null.
        // On reste alors dans la boucle pour envoyer une demande pour rejoindre
        // la partie
        do {
            // Envoi du buffer à l'hôte
            channel.send(bufferJoinGame, address);
            Thread.sleep(1000);
            // réception du gameState envoyé par l'hôte
            senderAddress = channel.receive(bufferGameState);
        } while (senderAddress == null);
        bufferGameState.flip();
        // récupère le gameState
        id = PlayerID.values()[(int) bufferGameState.get(0)];

        // affichage de la partie
        SwingUtilities.invokeAndWait(() -> createUI());
        xbc.setGameState(receiveGameState(), id);
        channel.configureBlocking(true);
        Sounds.play("music.wav", 5);
        while (true) {
            SwingUtilities.invokeAndWait(() -> xbc.setGameState(receiveGameState(), id));
        }
    }

    /**
     * Retourne le gameState reçu par l'hôte
     * 
     * @param bufferGameState
     * @return
     * @throws IOException
     */
    private static GameState receiveGameState(){
        ByteBuffer bufferGameState = ByteBuffer.allocate(GAMESTATE_MAX_SIZE + 1);
        try {
            channel.receive(bufferGameState);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Byte> serializedGameState = new ArrayList<>();
        bufferGameState.flip();
        bufferGameState.get();
        while (bufferGameState.hasRemaining()) {
            Byte b = (Byte) bufferGameState.get();
            serializedGameState.add(b);
        }
        if(serializedGameState.remove(serializedGameState.size()-5)==1){
            Sounds.play("explosion.wav", 0);
        }
        for(int i = 0; i < 4; ++i){
            if(serializedGameState.remove(serializedGameState.size() - (4-i)) == 1 && id.ordinal() == i)
                Sounds.play("pickup.wav", 0);
        }
        return GameStateDeserializer.deserializeGameState(serializedGameState);
    }

    private static void createUI() {
        xbc = new XBlastComponent();
        JFrame window = new JFrame("XBlast");
        window.setSize(XBlastComponent.GAME_HEIGHT, XBlastComponent.GAME_WIDTH);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().add(xbc);
        window.pack();
        window.setVisible(true);
        Consumer<PlayerAction> c = y -> {
            if (y != null) {
                ByteBuffer bufferPlayerAction = ByteBuffer.allocate(1);
                bufferPlayerAction.put((byte) y.ordinal());
                bufferPlayerAction.flip();
                try {
                    channel.send(bufferPlayerAction, address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        xbc.addKeyListener(new KeyboardEventHandler(KeyboardEventHandler.DEFAULT_KEY_BOARD, c));
        xbc.requestFocusInWindow();
    }
}
