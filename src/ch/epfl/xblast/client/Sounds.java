package ch.epfl.xblast.client;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class Sounds {
    private static Clip clip;
    private Sounds(){
    }
    
    /**
     * Joue le son passé en argument avec un certain nombre de loop¨
     * un loop valant 0 lance le son une seule fois.
     * 
     * @param name
     *          le nom du fichier son. Doit être un .wav
     * @param loop
     *          le nombre de fois que l'on doit rejouer le son
     */
    public static void play(String name, int loop){
        URL url = Main.class.getClassLoader().getResource(name);
        AudioInputStream audioIn;
        try {
            audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip;
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(loop);
        } catch (UnsupportedAudioFileException | IOException  | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public static void playWaiting(){
        URL url = Main.class.getClassLoader().getResource("waiting.wav");
        AudioInputStream audioIn;
        try {
            audioIn = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException  | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public static void stopWaiting(){
        clip.close();
    }
}
