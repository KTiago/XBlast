/**
 * Classe principale du client
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ch.epfl.xblast.PlayerAction;
import ch.epfl.xblast.PlayerID;

public class Main {
    private static final int GAMESTATE_MAX_SIZE = 409;
    private static final int WAITING_TIME = 1000;
    private static final int PORT = 2016;
    private static XBlastComponent xbc;
    private static SocketAddress address;
    
    private static boolean canBegin = false;
    private static String hostName;
    private final static ImageCollection MENU_IMAGES = new ImageCollection("menu");
    private static final int GAMESETTINGS_SIZE = 49;
    private static JPanel menu;
    
    /**
     * Méthode main de la partie client du jeu XBlast
     * 
     * @param args
     *          le tableau doit être de taille 1 et contenir l'addresse
     *          de l'hôte sous forme d'une chaîne de caractères
     */
    public static void main(String[] args) {
        try (DatagramChannel channel = DatagramChannel
                .open(StandardProtocolFamily.INET)) {
            Sounds.play("intro.wav", 0);
            SwingUtilities.invokeAndWait(() -> createUI(channel));
            while (!canBegin){
                Thread.sleep(WAITING_TIME);
            }
            if(hostName == null){
                hostName = "localhost";
            }
            address = new InetSocketAddress(hostName, PORT);
            
            // Création du canal de communication UDP
            channel.configureBlocking(false);
            // Buffer qui contient l'octet correspondant à une demande pour
            // rejoindre la partie
            ByteBuffer bufferJoinGame = ByteBuffer.allocate(1);
            bufferJoinGame.put((byte) PlayerAction.JOIN_GAME.ordinal());
            bufferJoinGame.flip();
            
            // Buffer qui contient le gameState
            ByteBuffer bufferGameState = ByteBuffer
                    .allocate(GAMESTATE_MAX_SIZE + 1);
            
            // Tant que l'on a pas reçu le premier gameState, senderAddress est
            // null.
            // On reste alors dans la boucle pour envoyer une demande pour
            // rejoindre la partie
            do {
                // Envoi du buffer à l'hôte
                channel.send(bufferJoinGame, address);
                bufferJoinGame.rewind();
                Thread.sleep(WAITING_TIME);
                // réception du gameState envoyé par l'hôte
            } while (channel.receive(bufferGameState) == null);
            
            // récupère le gameState
            // affichage de la partie
            channel.configureBlocking(true);
            menu.setVisible(false);
            Sounds.stopWaiting();
            Sounds.play("music.wav", 3);
            while (true) {
                bufferGameState.flip();
                List<Byte> serializedGameState = new ArrayList<>();
                PlayerID id = PlayerID.values()[(int) bufferGameState.get()];
                while (bufferGameState.hasRemaining()) {
                    serializedGameState.add(bufferGameState.get());
                }
                int b = serializedGameState.remove(serializedGameState.size()-1);
                if(b >> 4 == 1){
                    Sounds.play("explosion.wav", 0);
                }
                for(int i = 0; i < 4; ++i){
                    if((((b >> (3-i))) & 1) == 1 && id.ordinal() == i)
                        Sounds.play("pickup.wav", 0);
                }
                bufferGameState.clear();
                SwingUtilities.invokeLater(
                        () -> xbc.setGameState(
                                GameStateDeserializer.deserializeGameState(serializedGameState),
                                id));
                channel.receive(bufferGameState);
            }
        } catch (IOException | InterruptedException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    /**
     * Crée une fenêtre et y ajoute un XBlastComponent
     * 
     * @param channel
     *          le canal de communication 
     */
    private static void createUI(DatagramChannel channel) {
      //JFrame initialisation
        JFrame window = new JFrame("XBlast");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cont = window.getContentPane();

        //Xbc initialisation
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
        xbc = new XBlastComponent();
        xbc.addKeyListener(new KeyboardEventHandler(KeyboardEventHandler.DEFAULT_KEY_BOARD, c));
        
        createMenu(cont);
        //Everything is displayed on the Screen 

        cont.add(menu);
        window.pack();
        window.setVisible(true);
    }
    
    private static void createMenu(Container cont){
        //JPanel contenant tout le menu
        menu = new JPanel(new BorderLayout());
        
        //JPanel contenant les images du menu
        GridLayout g = new GridLayout(3,1);
        JPanel imagesMenu = new JPanel(g);
        imagesMenu.setBackground(Color.WHITE);
        imagesMenu.setPreferredSize(new Dimension(XBlastComponent.GAME_WIDTH, XBlastComponent.GAME_HEIGHT - GAMESETTINGS_SIZE));

        
        
        //BeginPanel pour le fond
        //BeginPanel est une sous-classe de JPanel permettant d'afficher des images
        BeginPanel backGroundP1 = new BeginPanel(MENU_IMAGES.image(1));
        imagesMenu.add(backGroundP1);
        BeginPanel backGroundP2 = new BeginPanel(MENU_IMAGES.image(2));
        imagesMenu.add(backGroundP2);
        
        //JLabel pour le gif Loading
        Icon loadingGif = new ImageIcon("images/menu/000_loadingScreen.gif");
        JLabel loading = new JLabel(loadingGif);
        loading.setVisible(false);
        imagesMenu.add(loading);
        
        menu.add(imagesMenu,BorderLayout.CENTER);

        //JPanel for game settings Design
        GridLayout gr = new GridLayout(1,3);
        gr.setHgap(20);

        JPanel gameSettings = new JPanel(gr);
        gameSettings.setPreferredSize(new Dimension(XBlastComponent.GAME_WIDTH, GAMESETTINGS_SIZE));
        gameSettings.setBackground(Color.WHITE);
        
        //JLabel pour dire d'entrer l'adresse IP
        JLabel enterIP = new JLabel("Entrez l'adresse IP de l'hôte : ");
        enterIP.setFont(new Font("Arial", Font.BOLD, 20));
        enterIP.setOpaque(true);
        enterIP.setBackground(Color.WHITE);
        enterIP.setHorizontalAlignment(JLabel.RIGHT);
        gameSettings.add(enterIP);
        
        //JTextField
        JTextField IpHost = new JTextField("localhost");
        IpHost.setFont(new Font("Arial",Font.ITALIC,20));
        gameSettings.add(IpHost);
        
        //JButton initialisation
        JButton begin = new JButton("Jouer !");
        begin.setFont(new Font("Arial",Font.BOLD,20));
        begin.addActionListener(e -> {
            Sounds.playWaiting();
            canBegin = true;
            gameSettings.removeAll();
            loading.setVisible(true);
            hostName = IpHost.getText();
            cont.add(xbc);
            xbc.requestFocusInWindow();
            });
        gameSettings.add(begin);
        menu.add(gameSettings,BorderLayout.PAGE_END);
    }
}
