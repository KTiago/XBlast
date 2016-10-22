package ch.epfl.xblast.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import ch.epfl.xblast.Direction;
import ch.epfl.xblast.PlayerAction;
import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.Time;

public class Main {
    private final static int maxNumberOfPlayers = PlayerID.values().length;
    private static final int GAMESTATE_MAX_SIZE = 409;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        
        // Le nombre de joueurs est 4 par défaut, ou récupère celui passé en
        // argument si il y en a un
        int numberOfPlayers = maxNumberOfPlayers;
        if(args.length > 1)
            throw new IllegalArgumentException();
        else if(args.length == 1){
            numberOfPlayers = Integer.parseInt(args[0]);
            if(numberOfPlayers < 0 || numberOfPlayers > maxNumberOfPlayers)
                throw new IllegalArgumentException();
        }
        // Création du channel UDP
        DatagramChannel channel =
                DatagramChannel.open(StandardProtocolFamily.INET);
        channel.bind(new InetSocketAddress(2016));
        
        // Phase - 1
        // Réception des demandes de la part des joueurs pour rejoindre la partie
        // boucle jusqu'à ce qu'assez de joueurs aient rejoint la partie
        SocketAddress senderAddress;
        Map<SocketAddress, PlayerID> playerAddresses = new HashMap<>();
        ByteBuffer bufferJoinGame = ByteBuffer.allocate(1);
        
        while(playerAddresses.size() < numberOfPlayers){
            senderAddress = channel.receive(bufferJoinGame);
            if(!playerAddresses.containsKey(senderAddress) && bufferJoinGame.get(0) == PlayerAction.JOIN_GAME.ordinal()){
                playerAddresses.put(senderAddress, PlayerID.values()[playerAddresses.size()]);
            }
            bufferJoinGame.clear(); 
        }
        
        GameState gameState = Level.DEFAULT_LEVEL.gameState();
        BoardPainter boardPainter = Level.DEFAULT_LEVEL.boardPainter();
        List<Byte> serializedGameState;
        ByteBuffer bufferGameState = ByteBuffer.allocate(GAMESTATE_MAX_SIZE + 1);
        ByteBuffer bufferActionValue = ByteBuffer.allocate(1);
        long initialTime = System.nanoTime();
        Random rng = new Random();
        AI ai2 = new AI(PlayerID.PLAYER_2, rng);
        AI ai3 = new AI(PlayerID.PLAYER_3, rng);
        AI ai4 = new AI(PlayerID.PLAYER_4, rng);
        
        while(!gameState.isGameOver()){
            //envoi du gameState à chaque joueur
            serializedGameState = GameStateSerializer.serialize(boardPainter, gameState);
            bufferGameState.put((byte) 0);
            for(Byte b : serializedGameState){
                bufferGameState.put(b);
            }
            bufferGameState.flip();
            for(SocketAddress playerAddress : playerAddresses.keySet()){
                bufferGameState.put(0, (byte)playerAddresses.get(playerAddress).ordinal());
                bufferGameState.rewind();
                channel.send(bufferGameState, playerAddress);
            }
            
            bufferGameState.clear();
            
            //attente du prochain coup d'horloge
            long waitingTime = ((long)(gameState.ticks() + 1) * Ticks.TICK_NANOSECOND_DURATION) - (System.nanoTime() - initialTime);
            if(waitingTime > 0)
                Thread.sleep(waitingTime / Time.US_PER_S);
            
            //réception des actions de chaque joueur
            Map<PlayerID, Optional<Direction>> speedChangeEvents = new HashMap<>();
            Set<PlayerID> bombDropEvents = new HashSet<>();
            channel.configureBlocking(false);
            do{         
                bufferActionValue.clear();
                senderAddress = channel.receive(bufferActionValue);
                if(senderAddress != null && playerAddresses.containsKey(senderAddress)){
                    byte valueAction = bufferActionValue.get(0);
                    PlayerAction action = null;
                    
                    //vérifie que le bit envoyé par le joueur soit correct
                    //pour éviter qu'un joueur modifie son programme pour faire planter le seveur
                    if(valueAction >= 0 && valueAction < PlayerAction.values().length)
                        action = PlayerAction.values()[valueAction];
                    
                    PlayerID id = playerAddresses.get(senderAddress);
                    switch(action){
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
                
            //si senderAddress vaut null, cela veut dire qu'il n'y as plus d'octet à recevoir
            //on sort donc de la boucle    
            }while(senderAddress != null);
            Direction d2 = ai2.move(gameState);
            speedChangeEvents.put(PlayerID.PLAYER_2, Optional.of(d2));
            Direction d3 = ai3.move(gameState);
            speedChangeEvents.put(PlayerID.PLAYER_3, Optional.of(d3));
            Direction d4 = ai4.move(gameState);
            
            if(ai2.dropBomb(gameState)){
                bombDropEvents.add(PlayerID.PLAYER_2);
            }
            if(ai3.dropBomb(gameState)){
                bombDropEvents.add(PlayerID.PLAYER_3);
            }
            if(ai4.dropBomb(gameState)){
                bombDropEvents.add(PlayerID.PLAYER_4);
            }
            speedChangeEvents.put(PlayerID.PLAYER_4, Optional.of(d4));
            gameState = gameState.next(speedChangeEvents, bombDropEvents);
            
        }
        if(gameState.winner().isPresent()){
            System.out.println("Le gagnant est : " + gameState.winner().get().name());
        }
    }
}
