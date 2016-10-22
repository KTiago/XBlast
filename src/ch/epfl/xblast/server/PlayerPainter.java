/**
 * Peintre du joueur
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 * 
 */
package ch.epfl.xblast.server;

import ch.epfl.xblast.PlayerID;

public final class PlayerPainter {
    
    private static final int NBR_USED_IMAGES_PLAYER = 14;
    private static final int NBR_TOTAL_IMAGES_PLAYER = 20;
    private static final int NBR_OF_PLAYERS = PlayerID.values().length;
    private static final int NBR_IMAGES_BY_DIRECTION = 3;

    private PlayerPainter(){}
    
    /**
     * Retourne l'octet correspondant à l'image à utiliser pour dessiner ce
     * joueur à ce coup d'horloge
     * 
     * @param tick
     *            le coup d'horloge
     * @param player
     *            le joueur dont on veut savoir l'image
     * @return Retourne l'octet correspondant à l'image à utiliser pour dessiner
     *         ce joueur
     */
    public static byte byteForPlayer(int tick, Player player){
        byte indexImage = 0;
        boolean isWhite = false;
        switch(player.lifeState().state()){
        
        //Si le joueur est mort, ne l'affiche pas (il n'y a pas d'image 15)
        case DEAD:
            return NBR_USED_IMAGES_PLAYER + 1;
            
        //Si le joueur est mourant affiche l'image correspondante selon
        //le fait qu'il lui reste une vie ou pas
        case DYING:
            //l'avant-dernière image est l'image du joueur en train de mourir
            //l'antépénultième, celle du joueur qui perd une vie
            indexImage += player.lifeState().lives() == 1
                    ? NBR_USED_IMAGES_PLAYER - 1 : NBR_USED_IMAGES_PLAYER - 2;
            break;
            
        //S'il est invulnérable et que le tick est impair, utilise les images blanches
        case INVULNERABLE:
            if(tick % 2 != 0)
                isWhite = true;
            
        //Choisi l'octet de l'image selon sa position et sa direction
        case VULNERABLE:
            indexImage += player.direction().ordinal() * NBR_IMAGES_BY_DIRECTION;
            int component = player.direction().isHorizontal()
                    ? player.position().x() : player.position().y();
            //Si la composante (x ou y selon la direction) est paire
            //On utilise la première image correspond à sa direction
            if(component % 2 != 0)
                //Si le reste de la division par 4 de la composante est 1
                //on utilise la deuxième image, et la troisième sinon
                indexImage += component % 4 == 1 ? 1 : 2;
            break;
        }
        //Renvoie l'octet de l'image correspondant au joueur et à son état
        return indexImage += isWhite ? NBR_TOTAL_IMAGES_PLAYER * NBR_OF_PLAYERS
                : player.id().ordinal() * NBR_TOTAL_IMAGES_PLAYER;
    }
}
