/**
 * L'état d'une partie client
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */


package ch.epfl.xblast.client;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.SubCell;

public final class GameState {
    private final List<Player> players;
    private final List<Image> board;
    private final List<Image> bombsAndBlasts;
    private final List<Image> score;
    private final List<Image> time;

    /**
     * Construit l'état de jeu client pour les joueurs et les images du plateau,
     * des bombes et particules d'explosion, du score et du temps.
     * 
     * @param players
     *            le joueurs
     * @param board
     *            images du plateau de jeu
     * @param bombsAndBlasts
     *            images des bombes et particules d'explosion
     * @param score
     *            images du score
     * @param time
     *            images du temps restant
     * @throws nullPointerException
     *          si l'un des arguments est nul
     */
    public GameState(List<Player> players, List<Image> board,
            List<Image> bombsAndBlasts, List<Image> score, List<Image> time) {
        this.players = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(players)));
        this.board = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(board)));
        this.bombsAndBlasts = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(bombsAndBlasts)));
        this.score = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(score)));
        this.time = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(time)));
    }
    
    /**
     * Retourne les joueurs
     * 
     * @return les joueurs
     */
    public List<Player> players() {
        return players;
    }

    /**
     * Retourne les image du plateau de jeu
     * 
     * @return les images du plateau de jeu
     */
    public List<Image> board() {
        return board;
    }

    /**
     * Retourne les images des bombes et particules d'explosion
     * 
     * @return les images des bombes et particules d'explosion
     */
    public List<Image> bombsAndBlasts() {
        return bombsAndBlasts;
    }

    /**
     * Retourne les images du score
     * 
     * @return les images du score
     */
    public List<Image> score() {
        return score;
    }

    /**
     * Retourne les images du temps restant
     * 
     * @return les images du temps restant
     */
    public List<Image> time() {
        return time;
    }

    /**
     * Un joueur du point de vue client
     * 
     * @author Benno Schneeberger (258711)
     * @author Tiago Kieliger (258981)
     */
    public final static class Player {
        private final PlayerID id;
        private final int lives;
        private final SubCell position;
        private final Image image;

        /**
         * Construit un joueur avec les variables passées en argument
         * 
         * @param id
         *          l'identifiant du joueur
         * @param lives
         *          le nombre de vies du joueur
         * @param position
         *          la position du joueur
         * @param image
         *          l'image associée au joueur
         */
        public Player(PlayerID id, int lives, SubCell position, Image image) {
            this.id = id;
            this.lives = lives;
            this.position = position;
            this.image = image;
        }

        
        
        /**
         * Retourne l'identifiant du joueur
         * 
         * @return l'identifiant du joueur
         */
        public PlayerID id() {
            return id;
        }

        /**
         * Retourne le nombre de points de vie du joueur
         * 
         * @return le nombre de points de vie du joueur
         */
        public int lives() {
            return lives;
        }

        /**
         * Retourne la position du joueur
         * 
         * @return la position du joueur
         */
        public SubCell position() {
            return position;
        }

        /**
         * Retourne l'image associée au joueur
         * 
         * @return l'image associée au joueur
         */
        public Image image() {
            return image;
        }

    }
}
