package ch.epfl.xblast.server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class PanelGameState extends JPanel {

    private GameState gameState;
    private boolean isGameOver = false;
    private JLabel j1;
    private JLabel j2;
    private JLabel j3;
    private JLabel j4;
    private JLabel jG;
    
    public PanelGameState(){
        setLayout(new GridLayout(5, 1));
        setBorder(new EmptyBorder(10, 20, 10, 10));
        setBackground(Color.WHITE);
        
        // JLabel pour le joueur 1
        j1 = new JLabel();
        j1.setFont(new Font("Arial", Font.BOLD, 25));
        
        add(j1);
        
        // JLabel pour le joueur 2
        j2 = new JLabel();
        j2.setFont(new Font("Arial", Font.BOLD, 25));
        add(j2);

        // JLabel pour le joueur 3
        j3 = new JLabel();
        j3.setFont(new Font("Arial", Font.BOLD, 25));
        add(j3);

        // JLabel pour le joueur 4
        j4 = new JLabel();
        j4.setFont(new Font("Arial", Font.BOLD, 25));
        add(j4);
        
        //JLabel pour le status de jeu
        jG = new JLabel();
        jG.setFont(new Font("Arial", Font.BOLD, 35));
        jG.setHorizontalAlignment(JLabel.CENTER);
        add(jG);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(960, 400);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameState != null) {
            List<Player> p = gameState.players();
            Player p1 = p.get(0);
            j1.setText("Le joueur " + p1.id() + " possède " + p1.lives()
            + " vie(s). (Max bombes : " +p1.maxBombs()+", portée bombes : "+p1.bombRange()+")");
            Player p2 = p.get(1);
            j2.setText("Le joueur " + p2.id() + " possède " + p2.lives()
            + " vie(s). (Max bombes : " +p2.maxBombs()+", portée bombes : "+p2.bombRange()+")");
            Player p3 = p.get(2);
            j3.setText("Le joueur " + p3.id() + " possède " + p3.lives()
            + " vie(s). (Max bombes : " +p3.maxBombs()+", portée bombes : "+p3.bombRange()+")");
            Player p4 = p.get(3);
            j4.setText("Le joueur " + p4.id() + " possède " + p4.lives()
            + " vie(s). (Max bombes : " +p4.maxBombs()+", portée bombes : "+p4.bombRange()+")");
            String textG;
            if (isGameOver) {
                if (gameState.winner().isPresent()) {
                    textG = "Partie terminée, le gagnant est : "
                            + gameState.winner().get().name();
                } else {
                    textG = "Partie terminée, il n'y a pas de gagnant.";
                }
            } else {
                textG = "Partie en cours ...";
            }
            jG.setText(textG);
        }
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        repaint();
    }

    public void setGameOver(boolean isGameOver) {
        this.isGameOver = isGameOver;
        repaint();
    }

}
