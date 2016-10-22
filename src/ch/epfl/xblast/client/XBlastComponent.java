/**
 * Elément JComponent pour une partie de XBlast
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.client.GameState.Player;

@SuppressWarnings("serial")
public final class XBlastComponent extends JComponent{
    public static final int GAME_WIDTH = 960;
    public static final int GAME_HEIGHT = 688;
    private static final String fontName = "Arial";
    private static final int fontSize = 25;
    private static final int[] LIVES_X = {96, 240, 768, 912};
    private static final int LIVES_Y = 659;
    private static final int NBR_OF_PLAYERS = PlayerID.values().length;
    
    private GameState gameState;
    private PlayerID id;
    
    
    @Override
    /**
     * Retourne la taille idéale du composant
     * 
     * @return la taille idéale du composant
     */
    public Dimension getPreferredSize(){
        return new Dimension(GAME_WIDTH, GAME_HEIGHT);
    }

    /**
     * Modifie l'état du jeu affiché par le composant étant donné un 
     * état de jeu et l'identité du joueur 
     * 
     * @param gameState
     *          état de jeu
     * @param id
     *          identité du joueur
     */
    public void setGameState(GameState gameState, PlayerID id){
        this.gameState = gameState;
        this.id = id;
        repaint();
    }

    @Override
    /**
     * (Re)dessine le contenu du composant (ici l'état)
     * 
     * @param g
     *      le contexte graphique à utiliser pour effectuer le dessin
     */
    protected void paintComponent(Graphics g0){
        if(gameState != null && id != null){
            
            Graphics2D g = (Graphics2D)g0;
            
            // initialisation de quelques variables
            int x = 0, y  = 0;
            Image image = null;
            Image imageBombsAndBlasts = null;
            
            // initialisations des différents itérateurs
            Iterator<Image> itBombsAndBlasts = gameState.bombsAndBlasts().iterator();
            Iterator<Image> itBoard = gameState.board().iterator();
            Iterator<Image> itScore = gameState.score().iterator();
            Iterator<Image> itTime = gameState.time().iterator();
            
            // boucle qui affiche en une fois : le board, le score et le temps
            while(itBoard.hasNext() || itScore.hasNext() || itTime.hasNext()){
                if(itBoard.hasNext() && itBombsAndBlasts.hasNext()){
                    image = itBoard.next();
                    imageBombsAndBlasts = itBombsAndBlasts.next();
                }
                else if(itScore.hasNext()){
                    image = itScore.next();
                    imageBombsAndBlasts = null;
                }
                else if(itTime.hasNext()){
                    image = itTime.next();
                    imageBombsAndBlasts = null;
                }
                
                g.drawImage(image, x, y, null);
                g.drawImage(imageBombsAndBlasts, x, y, null);
                
                //parcours l'écran dans l'ordre de lecture. y est incrémenté dès
                //qu'une nouvelle ligne commence
                x = (x + image.getWidth(null)) % GAME_WIDTH;
                if(x == 0){
                    y += image.getHeight(null);
                }
            }
            
            Font font = new Font(fontName, Font.BOLD, fontSize);
            g.setColor(Color.WHITE);
            g.setFont(font);
            Iterator<Player> itPlayer = gameState.players().iterator();
            
            for(int i = 0; i < NBR_OF_PLAYERS; ++i){
                g.drawString(Integer.toString(itPlayer.next().lives()), LIVES_X[i], LIVES_Y);
            }
            
            List<Player> players = new ArrayList<>(gameState.players());
            Comparator<Player> c1 = (i, j) -> {
                return Integer.compare(i.position().y(), j.position().y());
            };
            Comparator<Player> c2 = (i, j) -> {
                return Integer.compare((i.id().ordinal() - id.ordinal() + 3) % NBR_OF_PLAYERS ,
                                       (j.id().ordinal() - id.ordinal() + 3) % NBR_OF_PLAYERS);
            };
            Collections.sort(players, c1.thenComparing(c2));
            for(Player p : players){
                g.drawImage(p.image(), p.position().x() * 4 - 24, p.position().y() * 3 - 52, null);
            } 
        }
    }
}
