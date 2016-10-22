package ch.epfl.xblast.client;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;


@SuppressWarnings("serial")
public class BeginPanel extends JPanel {
    
    private Image img;
    
    public BeginPanel(Image img){
        this.img  = img;
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        if(img != null)
            g.drawImage(img, 0, 0, null);
    }
    
    protected void setImage(Image img){
        this.img = img;
        this.repaint();
    }

}
