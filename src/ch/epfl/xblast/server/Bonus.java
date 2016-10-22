/**
 * Enum qui représente les différents bonus disponibles dans le jeu
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */
package ch.epfl.xblast.server;

public enum Bonus {
    INC_BOMB {
        @Override
        public Player applyTo(Player player) {
            //vérification que le nombre limite de bombes qu'on joueur peut posséder ne soit pas dépassé
            return player.maxBombs() < MAXNUMBEROFBOMBS ? player.withMaxBombs(player.maxBombs() + 1) : player;
        }
    },

    INC_RANGE {
        @Override
        public Player applyTo(Player player) {
            //vérification que la portée limite des bombes d'un joueur ne soit pas dépassée
            return player.bombRange() < MAXBOMBRANGE ? player.withBombRange(player.bombRange() + 1) : player;
        }
    };
    private final static int MAXNUMBEROFBOMBS = 9;
    private final static int MAXBOMBRANGE = 9;
    abstract public Player applyTo(Player player);
}
