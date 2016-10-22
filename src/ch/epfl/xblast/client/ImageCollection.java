/**
 * Collection d'images
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast.client;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

public final class ImageCollection {
    private final Map<Integer, Image> imageCollection = new HashMap<>();

    /**
     * Construit une Collection d'image sachant le
     * nom du répertoire
     * 
     * @param directoryName
     *          le nom du repertoire
     */
    public ImageCollection(String directoryName) {
        try {
            File directory = new File(ImageCollection.class
                    .getClassLoader()
                    .getResource(directoryName)
                    .toURI());
            for (File file : directory.listFiles()) {
                String name = file.getName();
                try {
                    int number = Integer.parseInt(name.substring(0, 3));
                    imageCollection.put(number, ImageIO.read(file));
                } catch (NumberFormatException | IOException e) {
                }
            }
        } catch (URISyntaxException e1) {
        }
    }

    /**
     * Retourne une image d'index donné sous la forme 
     * d'une instance de classe image
     * 
     * @param index
     *            l'index donné
     * @return une image d'index donné
     * @throws NoSuchElementException
     *             si aucune image ne correspond à l'index
     */
    public Image image(int index) {
        if(!imageCollection.containsKey(index))
            throw new NoSuchElementException();
        return imageCollection.get(index);
    }

    /**
     * Retourne une image d'index donné sous la forme
     * d'une instance de classe image
     * 
     * @param index
     *          l'index donné
     * @return une image d'index donné
     */
    public Image imageOrNull(int index) {
        //Si la clé n'existe pas, get retourne null
        return imageCollection.get(index);
    }
}
