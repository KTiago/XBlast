/**
 * Classe principale du serveur
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.server;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import ch.epfl.xblast.Direction;
import ch.epfl.xblast.PlayerAction;
import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.Time;

public class Main {
    private static final int GAMESTATE_MAX_SIZE = 409;
    private static final int PORT = 2016;
    
    private static int numberOfPlayers;
    private static int numberOfAI;
    private static boolean serverIsStarted;
    private static String HOST_IP;
    private final static List<AI> AIs = new ArrayList<>();
    private static PanelGameState panel;
    /**
     * Méthode main de la partie serveur du jeu XBlast
     * 
     * @param args
     *          le tableau doit être de taille 1 et contenir le nombre
     *          de joueurs sous la forme d'un String        
     */
    public static void main(String[] args) {

        try (DatagramChannel channel = DatagramChannel
                .open(StandardProtocolFamily.INET)) {
            //Récupère l'IP de l'hôte
            HOST_IP = InetAddress.getLocalHost().getHostAddress();
            SwingUtilities.invokeAndWait(() -> createUI());
            
            while(!serverIsStarted){
                Thread.sleep(1000);
            }
            
            // Création du channel UDP
            channel.bind(new InetSocketAddress(PORT));
            
            // Phase - 1
            // Réception des demandes de la part des joueurs pour rejoindre la
            // partie
            // boucle jusqu'à ce qu'assez de joueurs aient rejoint la partie
            SocketAddress senderAddress;
            Map<SocketAddress, PlayerID> playerAddresses = new HashMap<>();
            ByteBuffer bufferJoinGame = ByteBuffer.allocate(1);
            while (playerAddresses.size() < numberOfPlayers) {
                senderAddress = channel.receive(bufferJoinGame);
                bufferJoinGame.flip();
                if (!playerAddresses.containsKey(senderAddress)
                        && bufferJoinGame.hasRemaining()
                        && bufferJoinGame.get() == PlayerAction.JOIN_GAME.ordinal()) {
                    playerAddresses.put(
                            senderAddress,
                            PlayerID.values()[playerAddresses.size()]);
                }
                bufferJoinGame.clear();
            }

            // Phase - 2
            // envoi du gameState aux clients
            GameState gameState = Level.DEFAULT_LEVEL.gameState();
            panel.setGameState(gameState);
            BoardPainter boardPainter = Level.DEFAULT_LEVEL.boardPainter();
            ByteBuffer bufferGameState = ByteBuffer
                    .allocate(GAMESTATE_MAX_SIZE + 1);
            ByteBuffer bufferActionValue = ByteBuffer.allocate(1);
            long initialTime = System.nanoTime();
            List<Byte> serializedGameState;
            
            Random rng = new Random();
            for(int i = 0; i < numberOfAI; ++i){
                AIs.add(new AI(PlayerID.values()[3 - i], rng));
            }
            
            channel.configureBlocking(false);
            while (!gameState.isGameOver()) {
                // envoi du gameState à chaque joueur
                serializedGameState = GameStateSerializer
                        .serialize(boardPainter, gameState);
                
                // prépare un octet à la première position
                // qui sera ennsuite modifié selon le destinataire
                bufferGameState.put((byte) 0);
                for (Byte b : serializedGameState) {
                    bufferGameState.put(b);
                }
                bufferGameState.flip();
                for (SocketAddress playerAddress : playerAddresses.keySet()) {
                    bufferGameState.put(0, (byte) playerAddresses
                            .get(playerAddress).ordinal());
                    bufferGameState.rewind();
                    channel.send(bufferGameState, playerAddress);
                }
                bufferGameState.clear();

                // attente du prochain coup d'horloge
                long waitingTime = ((long) (gameState.ticks() + 1)
                        * Ticks.TICK_NANOSECOND_DURATION)
                        - (System.nanoTime() - initialTime);
                
                if (waitingTime > 0)
                    Thread.sleep(waitingTime / Time.NS_PER_MS, (int) waitingTime % Time.NS_PER_MS);

                // réception des actions de chaque joueur
                Map<PlayerID, Optional<Direction>> speedChangeEvents = new HashMap<>();
                Set<PlayerID> bombDropEvents = new HashSet<>();

                while ((senderAddress = channel.receive(bufferActionValue)) != null){
                    bufferActionValue.flip();
                    if (playerAddresses.containsKey(senderAddress)
                            && bufferActionValue.hasRemaining()) {
                        byte valueAction = bufferActionValue.get();
                        PlayerAction action = null;

                        // vérifie que le bit envoyé par le joueur soit correct
                        // pour éviter qu'un joueur modifie son programme pour
                        // faire planter le seveur
                        if (valueAction >= 0 && valueAction < PlayerAction.values().length)
                            action = PlayerAction.values()[valueAction];

                        PlayerID id = playerAddresses.get(senderAddress);
                        switch (action) {
                        case MOVE_N:
                            speedChangeEvents.put(id, Optional.of(Direction.N));
                            break;
                        case MOVE_E:
                            speedChangeEvents.put(id, Optional.of(Direction.E));
                            break;
                        case MOVE_S:
                            speedChangeEvents.put(id, Optional.of(Direction.S));
                            break;
                        case MOVE_W:
                            speedChangeEvents.put(id, Optional.of(Direction.W));
                            break;
                        case STOP:
                            speedChangeEvents.put(id, Optional.empty());
                            break;
                        case DROP_BOMB:
                            bombDropEvents.add(id);
                            break;
                        default:
                            break;
                        }
                    }
                    bufferActionValue.clear();
                }

                for(AI ai : AIs){
                    speedChangeEvents.put(ai.id(), Optional.of(ai.move(gameState)));
                    if(ai.dropBomb(gameState))
                        bombDropEvents.add(ai.id());
                }
                
                gameState = gameState.next(speedChangeEvents, bombDropEvents);
                panel.setGameState(gameState);
            }
            panel.setGameOver(true);
        } catch (IOException | InterruptedException | InvocationTargetException e) {
            e.printStackTrace();}
    }
    
    private static void createUI(){
        //JFrame initialisation
        JFrame window = new JFrame("XBlast Serveur");
        window.setPreferredSize(new Dimension(960,400));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cont = window.getContentPane();
        
        JPanel menu = createJPanelMenu(cont);

        

        cont.add(menu);
        window.pack();
        window.setVisible(true);
    }
    
    private static JPanel createJPanelMenu(Container cont){
        //JPanel contenant tout le menu
        GridLayout grid = new GridLayout(4,1);
        JPanel menu = new JPanel(grid);
        menu.setBackground(Color.WHITE);
        menu.setBorder(new EmptyBorder(20, 20, 20, 20));
        menu.setOpaque(true);
        
        /*
         * PREMIERE LIGNE ADRESSSE IP
         */
        //JLabel contenant l'adresse IP de l'hôte
        
        JLabel IPAddress = new JLabel("Votre Adresse IP est : "+ HOST_IP);
        IPAddress.setHorizontalAlignment(JLabel.CENTER);
        IPAddress.setFont(new Font("Arial",Font.BOLD,35));
        
        
        /*
         * DEUXIEME LIGNE MESSAGE ERROR JOUEUR
         */
        //JLabel affichant une erreur si le joueur essaie
        //de lancer la partie 
        JLabel error = new JLabel("Veuillez sélectionner votre nombre de joueur et d'AI (min 1, max 4)");
        error.setFont(new Font("Arial",Font.BOLD,25));
        error.setVerticalAlignment(JLabel.CENTER);
        
        /*
         * TROISIEME LIGNE TEXTE REGLAGE
         */
        //JPanel Prenant tous les textes
        JPanel text = new JPanel(new GridLayout(1,3));
        text.setOpaque(false);
        
        //JLabel pour l'IA
        JLabel AIText = new JLabel("Nombre d'IA");
        AIText.setHorizontalAlignment(JLabel.LEFT);
        AIText.setVerticalAlignment(JLabel.BOTTOM);
        AIText.setFont(new Font("Arial",Font.BOLD,20));
        text.add(AIText);
        
        //JLabel pour les joueurs
        JLabel playerText = new JLabel("Nombre de joueurs");
        playerText.setHorizontalAlignment(JLabel.LEFT);
        playerText.setVerticalAlignment(JLabel.BOTTOM);
        playerText.setFont(new Font("Arial",Font.BOLD,20));
        text.add(playerText);
        
        //JLabel Vide pour l'alignement
        JLabel empty  = new JLabel();
        text.add(empty);
        
        
        /*
         * QUATRIEME LIGNE REGLAGE
         */
        //JPanel for game settings Design
        JPanel gameSettings = new JPanel(new GridLayout(1,3));
        
        //JComboBox choix du nombre de joueurs et du nombre d'AI
        String [] cPlayer = new String[5];
        String [] cAI = new String[5];

        for(int i = 0;i <= 4;i++){
            String v = String.valueOf(i);
            cPlayer[i] = v;
            cAI[i] = v;
        }
        JComboBox<String> choixJoueurs = new JComboBox<>(cPlayer);
        JComboBox<String> choixAI = new JComboBox<>(cAI);
        gameSettings.add(choixAI);
        gameSettings.add(choixJoueurs);
        
        panel = new PanelGameState();
        //JButton initialisation
        JButton begin = new JButton("START SERVER");
        begin.addActionListener(e -> {
            numberOfPlayers = Integer
                    .parseInt((String) choixJoueurs.getSelectedItem());
            numberOfAI = Integer.parseInt((String) choixAI.getSelectedItem());
            if (numberOfPlayers + numberOfAI == 0) {
                error.setForeground(Color.RED);
                error.setText("Nombre de joueurs insuffisant (min 1)");
            } else if (numberOfPlayers + numberOfAI > 4) {
                error.setForeground(Color.RED);
                error.setText("Nombre de joueurs trop grand (max 4)");
            } else {
                menu.setVisible(false);
                serverIsStarted = true;
                cont.add(panel);
            }
        });
        gameSettings.add(begin);

        
        /*
         * AFFICHAGE DES COMPOSANTES
         */
        menu.add(IPAddress);
        menu.add(error);
        menu.add(text);
        menu.add(gameSettings);
        
        return menu;
    }
}
