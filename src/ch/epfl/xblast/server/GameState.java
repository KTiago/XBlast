/**
 /**
 * L'état d'une partie
 * 
 * @author Benno Schneeberger (258711)
 * @author Tiago Kieliger (258981)
 */

package ch.epfl.xblast.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import ch.epfl.cs108.Sq;
import ch.epfl.xblast.ArgumentChecker;
import ch.epfl.xblast.Cell;
import ch.epfl.xblast.Direction;
import ch.epfl.xblast.Lists;
import ch.epfl.xblast.PlayerID;
import ch.epfl.xblast.SubCell;
import ch.epfl.xblast.server.Player;
import ch.epfl.xblast.server.Player.DirectedPosition;
import ch.epfl.xblast.server.Player.LifeState;

public final class GameState {

    private static final int BLOCKED_BY_BOMB_DISTANCE = 6;
    private static final List<Block> RANDOM_BONUS = Arrays
            .asList(Block.BONUS_BOMB, Block.BONUS_RANGE, Block.FREE);
    private static final List<List<PlayerID>> PERMUTATIONS = Collections
            .unmodifiableList(Lists.permutations(Arrays.asList(PlayerID.values())));
    private static final Random RANDOM = new Random(2016);
    
    private final int ticks;
    private final Board board0;
    private final List<Player> players0;
    private final List<Bomb> bombs0;
    private final List<Sq<Sq<Cell>>> explosions0;
    private final List<Sq<Cell>> blasts0;
    private final boolean explodingSound;
    private final Set<PlayerID> upgradedPlayers;
    
    /**
     * Construit l'état du jeu pour le coup d'horloge, le plateau de jeu, les
     * joueurs, les bombes, les explosions et les particules d'explosion donnés
     * 
     * @param ticks
     *            le coup d'horloge
     * @param board
     *            le plateau de jeu
     * @param players
     *            les joueurs
     * @param bombs
     *            les bombes
     * @param explosions
     *            les explosions
     * @param blasts
     *            les particules des explosions
     * @throws NullPointerException
     *             si l'un des cinq derniers arguments est nul
     * @throws IllegalArgumentException
     *             si il n'y a pas 4 joueurs ou si les coups d'horloge sont
     *             strictement négatif
     */
    public GameState(int ticks, Board board, List<Player> players,
            List<Bomb> bombs, List<Sq<Sq<Cell>>> explosions,
            List<Sq<Cell>> blasts, boolean explodingSound, Set<PlayerID> upgradedPlayers) {
        this.ticks = ArgumentChecker.requireNonNegative(ticks);
        this.board0 = Objects.requireNonNull(board);
        this.players0 = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(players)));
        if (players.size() != 4) {
            throw new IllegalArgumentException();
        }
        this.bombs0 = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(bombs)));
        this.explosions0 = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(explosions)));
        this.blasts0 = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(blasts)));
        this.explodingSound = explodingSound;
        this.upgradedPlayers = Collections.unmodifiableSet(Objects.requireNonNull(upgradedPlayers));
    }

    /**
     * Construit l'état du jeu pour le plateau et les joueurs donnés, pour le
     * coup d'horloge 0 et aucune bombe, explosion ou particule d'explosion
     * 
     * @param board
     *            le plateau de jeu
     * @param players
     *            les joueurs
     * @throws IllegalArgumentException
     *             s'il n'y a pas 4 joueurs
     * @throws NullPointerException
     *             si un des deux paramètres est nul
     */
    public GameState(Board board, List<Player> players) {
        this(0, board, players, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), false, Collections.emptySet());
    }

    /**
     * Retourne le coup d'horloge correspondant à l'état
     * 
     * @return le coup d'horloge correspondatn à l'état
     */
    public int ticks() {
        return ticks;
    }

    /**
     * Retourne si le joueur vient de recevoir un bonus
     * 
     * @return si le joueur vient de recevoir un bonus
     */
    
    public boolean isUpgraded(PlayerID id){
        return upgradedPlayers.contains(id);
    }
    /**
     * Retourne vrai si il y a un son d'explosion, et faux sinon
     * 
     * @return vrai si il y a un son d'explosion, et faux sinon;
     */
    public boolean explodingSound(){
        return explodingSound;
    }
    /**
     * Retourne vrai si et seulement si l'état correspond à une partie terminée
     * 
     * @return true si et seulement si l'état correspond à une partie terminée
     */
    public boolean isGameOver() {
        return ticks > Ticks.TOTAL_TICKS || alivePlayers().size() <= 1;
    }

    /**
     * Retourne le temps restant dans la partie, en secondes
     * 
     * @return le temps restant dans la partie
     */
    public double remainingTime() {
        return (double) (Ticks.TOTAL_TICKS - ticks) / Ticks.TICKS_PER_SECOND;
    }

    /**
     * Retourne l'identité du vainqueur de cette partie s'il y en a un, sinon la
     * valeur optionnelle vide
     * 
     * @return identité du vainqueur de cette partie s'il y en a un, sinon la
     *         valeur optionnelle vide
     */
    public Optional<PlayerID> winner() {
        return alivePlayers().size() == 1
                ? Optional.of(alivePlayers().get(0).id())
                : Optional.empty();
    }

    /**
     * Retourne le plateau de jeu
     * 
     * @return le plateau de jeu
     */
    public Board board() {
        return board0;
    }

    /**
     * Retourne les joueurs, sous la forme d'une liste (joueurs morts compris)
     * 
     * @return les joueurs de la partie (mort et vivant)
     */
    public List<Player> players() {
        return players0;
    }

    /**
     * Retourne les joueurs vivants, sous la forme d'une liste
     * 
     * @return les joueurs vivants
     */
    public List<Player> alivePlayers() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player p : players0) {
            if (p.isAlive()) {
                alivePlayers.add(p);
            }
        }
        return alivePlayers;
    }

    /**
     * Retourne une table associant les bombes aux cases qu'elles occupent
     * 
     * @return une table associant les bombes aux cases qu'elles occupent
     */
    public Map<Cell, Bomb> bombedCells() {
        return bombedCells(bombs0);
    }

    /**
     * Retourne l'ensemble des cases sur lesquelles se trouve au moins une
     * particule d'explosion
     * 
     * @return l'ensemble des cases sur lesquelles se trouve au moins une
     *         particule d'explosion
     */
    public Set<Cell> blastedCells() {
        return blastedCells(blasts0);
    }

    /**
     * Retourne l'état du jeu pour le coup d'horloge suivant, en fonction de
     * l'actuel et des événements donnés
     * 
     * @param speedChangeEvents
     *            les changements de directions associés aux joueurs
     * @param bombDropEvents
     *            les dépots de bombes des joueurs
     * @return l'état du jeu pour le coup d'horloge suivant
     */
    public GameState next(Map<PlayerID, Optional<Direction>> speedChangeEvents,
            Set<PlayerID> bombDropEvents) {
        // 1 - les particules d'explosion évoluent
        List<Sq<Cell>> blasts1 = nextBlasts(blasts0, board0, explosions0);

        // Dans cette partie nous créons l'ensemble des bonus consommés et le
        // map "playerBonuses"
        List<Player> playersInOrder = playersInOrder();
        Set<Cell> consumedBonuses = new HashSet<>();
        Map<PlayerID, Bonus> playerBonuses = new HashMap<>();

        for (Player p : playersInOrder) {
            Cell playerPosition = p.position().containingCell();
            if (board0.blockAt(playerPosition).isBonus()
                    && p.position().isCentral()
                    && !consumedBonuses.contains(playerPosition)) {
                consumedBonuses.add(playerPosition);
                playerBonuses.put(p.id(), board0.blockAt(playerPosition).associatedBonus());
            }
        }
        Set<PlayerID> upgradedPlayers = playerBonuses.keySet();
        Set<Cell> blastedCells1 = blastedCells(blasts1);

        // 2 - le plateau de jeu évolue (en fonction des nouvelles particules
        // d'explosion calculée ci-dessus)
        Board board1 = nextBoard(board0, consumedBonuses, blastedCells1);

        // 3 - les explosions évoluent
        List<Sq<Sq<Cell>>> explosions1 = nextExplosions(explosions0);

        // 4 - les bombes existantes et celles nouvellement déposées par les
        // joueurs évoluent, en fonction des nouvelles particules d'explosion
        List<Bomb> bombsTemp = new ArrayList<>(bombs0);
        bombsTemp.addAll(newlyDroppedBombs(playersInOrder, bombDropEvents, bombs0));
        List<Bomb> bombs1 = new ArrayList<>();
        boolean explodingSound1 = false;
        for (Bomb b : bombsTemp) {
            //Les bombes dont la mèches est totalement ou celles touchées par
            //des particules d'explosions explosent
            if (b.fuseLength() == 1 || blastedCells1.contains(b.position())) {
                explosions1.addAll(b.explosion());
                explodingSound1 = true;
            } else {
                bombs1.add(new Bomb(b.ownerId(), b.position(), b.fuseLength() - 1, b.range()));
            }
        }
        // 5 - les joueurs évoluent
        List<Player> players1 = nextPlayers(players0, playerBonuses,
                bombedCells(bombs1).keySet(), board1, blastedCells1,
                speedChangeEvents);
        return new GameState(ticks + 1, board1, players1, bombs1, explosions1,
                blasts1, explodingSound1, upgradedPlayers);
    }

    /**
     * Calcule les particules d'explosion pour l'état suivant étant données
     * celles de l'état courant, le plateau de jeu courant et les explosions
     * courantes.
     * 
     * @param blasts0
     *            les particules d'explosion de l'état courant
     * @param board0
     *            le plateau de jeu courant
     * @param explosions0
     *            les explosions courantes
     * @return les particules d'explosion de l'état suivant
     */
    private static List<Sq<Cell>> nextBlasts(List<Sq<Cell>> blasts0,
            Board board0, List<Sq<Sq<Cell>>> explosions0) {
        List<Sq<Cell>> blasts1 = new ArrayList<>();
        for (Sq<Cell> c : blasts0) {
            if (board0.blockAt(c.head()).isFree() && !(c.tail().isEmpty())) {
                blasts1.add(c.tail());
            }
        }
        for (Sq<Sq<Cell>> c : explosions0) {
            blasts1.add(c.head());
        }
        return blasts1;
    }

    /**
     * Calcule le prochain état du plateau en fonction du plateau actuel, des
     * bonus consommés par les joueurs et les nouvelles particules d'explosion
     * donnés
     * 
     * @param board0
     *            le plateau de jeu courant
     * @param consumedBonuses
     *            les bonus consommés
     * @param blastedCells1
     *            les cases touchées par les particules d'explosions à l'état
     *            suivant
     * @return le prochain état du plateau en fonction du plateau actuel
     */
    private static Board nextBoard(Board board0, Set<Cell> consumedBonuses,
            Set<Cell> blastedCells1) {
        // Création d'une copie du board avant modification, en "vieillissant"
        // chaque séquence de blocks
        List<Sq<Block>> boardRows = new ArrayList<>();
        List<Cell> l = Cell.ROW_MAJOR_ORDER;

        for (Cell c : l) {
            // On remplace dans le board les bonus consommés par un block libre
            if (consumedBonuses.contains(c))
                boardRows.add(Sq.constant(Block.FREE));

            else if (blastedCells1.contains(c)
                    && board0.blockAt(c) == Block.DESTRUCTIBLE_WALL) {
                // On calcule le prochain état des blocks destructibles touchés
                // par des particules d'explosion
                boardRows.add(Sq
                        .repeat(Ticks.WALL_CRUMBLING_TICKS, Block.CRUMBLING_WALL)
                        .concat(Sq.constant(RANDOM_BONUS.get(RANDOM.nextInt(3)))));
            } else if (blastedCells1.contains(c)
                    && board0.blockAt(c).isBonus()) {
                // On calcule le prochain état des blocks bonus touchés par des
                // particules d'explosion
                boardRows.add(board0.blocksAt(c).tail()
                        .limit(Ticks.BONUS_DISAPPEARING_TICKS)
                        .concat(Sq.constant(Block.FREE)));

            } else {
                boardRows.add(board0.blocksAt(c).tail());
            }
        }
        return new Board(boardRows);
    }

    /**
     * Calcule les explosions pour le prochain état en fonction des actuelles
     * 
     * @param explosions0
     *            les explosions de l'état courant
     * @return les explosions pour le prochain état en fonction des actuelles
     */
    private static List<Sq<Sq<Cell>>> nextExplosions(
            List<Sq<Sq<Cell>>> explosions0) {

        List<Sq<Sq<Cell>>> nextExplosion = new ArrayList<>();
        for (Sq<Sq<Cell>> sq : explosions0) {
            if (!sq.tail().isEmpty())
                nextExplosion.add(sq.tail());
        }
        return nextExplosion;
    }

    /**
     * Calcule le prochain état des joueurs en fonction de l'état actuel et des
     * différents paramètres
     * 
     * @param players0
     *            liste des joueurs à l'état courant
     * @param playerBonuses
     *            les bonus consommés associés aux joueurs
     * @param bombedCells1
     *            cases contenant une bombe à l'état suivant
     * @param board1
     *            plateau de jeu à l'état suivant
     * @param blastedCells1
     *            les cases touchées par les particules d'explosions à l'état
     *            suivant
     * @param speedChangeEvents
     *            les changements de directions associés aux joueurs
     * @return le prochain état des joueurs en fonction de l'état actuel et des
     *         différents paramètres
     */
    private static List<Player> nextPlayers(List<Player> players0,
            Map<PlayerID, Bonus> playerBonuses, Set<Cell> bombedCells1,
            Board board1, Set<Cell> blastedCells1,
            Map<PlayerID, Optional<Direction>> speedChangeEvents) {
        
        List<Player> players1 = new ArrayList<>();
        // Evolution de chaque joueur
        for (Player player0 : players0) {

            // 1 - La nouvelle séquence de position dirigée en fonction de
            // l'événement de changement de direction associé au joueur
            Sq<DirectedPosition> sqDPosition = player0.directedPositions();
            if (speedChangeEvents.containsKey(player0.id())) {
                Optional<Direction> dir = speedChangeEvents.get(player0.id());
                
                //si ce n'est pas une commande d'arrêt
                if (dir.isPresent()) {
                    // Si le joueur souhaite aller en avant ou en arrière
                    if (dir.get().isParallelTo(player0.direction())) {
                        // Une séquence infinie dans la direction voulue (avant
                        // ou arrière)
                        sqDPosition = DirectedPosition
                                .moving(player0.directedPositions().head()
                                        .withDirection(dir.get()));
                    }
                    else {
                        // Une séquence jusqu'à la sous case centrale puis dans
                        // la direction voulue
                        DirectedPosition central = sqDPosition
                                .findFirst(u -> u.position().isCentral())
                                .withDirection(dir.get());
                        sqDPosition = sqDPosition
                                .takeWhile(u -> !u.position().isCentral())
                                .concat(DirectedPosition.moving(central));
                    }
                //si c'est une commande d'arrêt
                } else {
                    // Une séquence jusqu'à la sous case centrale puis arrêtée
                    DirectedPosition central = sqDPosition
                            .findFirst(u -> u.position().isCentral());
                    sqDPosition = sqDPosition
                            .takeWhile(u -> !u.position().isCentral())
                            .concat(DirectedPosition.stopped(central));
                }
            }
            // 2 - La séquence de positions dirigées évolue si le joueur peut se
            // déplacer
            SubCell position = player0.position();
            boolean canMove = player0.lifeState().canMove();
            boolean isBlockedByWall = position.isCentral() &&
                    !board1.blockAt(position.containingCell()
                            .neighbor(sqDPosition.head().direction())).canHostPlayer();
            boolean isBlockedByBomb = bombedCells1.contains(position.containingCell())
                    && position.distanceToCentral() == BLOCKED_BY_BOMB_DISTANCE
                    && position.neighbor(sqDPosition.head().direction())
                            .distanceToCentral() < position.distanceToCentral();
            // le personnage se déplace si les conditions sont respectées
            if (canMove && !isBlockedByWall && !isBlockedByBomb)
                sqDPosition = sqDPosition.tail();
            // 3 - L'état du joueur évolue en fonction de sa nouvelle position
            Sq<LifeState> sqLifeState = player0.lifeStates().tail();
            if (blastedCells1.contains(sqDPosition.head().position().containingCell())
                    && player0.lifeState().state() == Player.LifeState.State.VULNERABLE) {
                sqLifeState = player0.statesForNextLife();
            }
            // On crée le nouveau player avec éventuellement les nouveaux
            // attributs
            Player player1 = new Player(player0.id(), sqLifeState, sqDPosition, player0.maxBombs(), player0.bombRange());
            // 4 - Les capacités du joueur évoluent, en fonction du bonus qu'il
            // a éventuellement consommé

            player1 = playerBonuses.containsKey(player1.id())
                    ? playerBonuses.get(player1.id()).applyTo(player1)
                    : player1;
            // On ajoute le joueur ainsi créé à la nouvelle liste
            players1.add(player1);
        }
        return players1;
    }

    /**
     * Retourne la liste des bombes nouvellement posées par les joueurs, étant
     * donnés les joueurs actuels, les événements de dépôt de bombes et les
     * bombes actuelles donnés
     * 
     * @param playersInOrder
     *            liste triée dans l'ordre de résolutions des conflits
     *            des joueurs à l'état courant 
     * @param bombDropEvents
     *            les dépots de bombes des joueurs
     * @param bombs0
     *            liste des bombes à l'état courant
     * @return La liste des bombes nouvellement posées par les joueurs
     */
    private static List<Bomb> newlyDroppedBombs(List<Player> playersInOrder,
            Set<PlayerID> bombDropEvents, List<Bomb> bombs0) {
        List<Bomb> newlyDroppedBombs = new ArrayList<>();
        for (Player p : playersInOrder) {
            // Plusieurs tests pour déterminer si le joueur peut poser une
            // bombe..
            // 1 - si le joueur veut poser une bombe
            boolean bombEvent = bombDropEvents.contains(p.id());
            // 2 - si le joueur est vivant
            boolean isAlive = p.isAlive();
            // 3 - si la case ne contient pas déjà une bombe
            boolean noBombAlready = !bombedCells(bombs0).keySet()
                    .contains(p.position().containingCell());
            int nbrOfBombs = 0;
            for (Bomb b : bombs0) {
                if (b.ownerId() == p.id())
                    // on compte les bombes pour la condition 4
                    nbrOfBombs++;
            }
            // 4 - si le joueur n'a pas atteint sa limite maximale de bombe
            // qu'il peut déposer
            boolean bombLimit = nbrOfBombs < p.maxBombs();
            // on vérifie que les conditions 1 à 4 soient respectées
            if (bombEvent && isAlive && noBombAlready && bombLimit
                    && !newlyDroppedBombs.contains(p.newBomb())) {
                // on vérifie qu'il n'y a pas un joueur qui a la précédence sur
                // ce joueur qui aurait déjà posé une bombe
                // remarque : les conflits entre joueurs sont réglés de manière
                // implicite puisque l'on itère sur la liste
                // playersInOrder qui est déjà triée dans l'ordre de résolution des
                // conflits.
                newlyDroppedBombs.add(p.newBomb());
            }
        }
        return newlyDroppedBombs;
    }

    /**
     * Retourne une table associant les bombes aux cases qu'elles occupent étant
     * donné une liste de bombes
     * 
     * @param bombs
     *            liste de bombes
     * @return une table associant les bombes aux cases qu'elles occupent étant
     *         donné une liste de bombes
     */
    private static Map<Cell, Bomb> bombedCells(List<Bomb> bombs) {
        Map<Cell, Bomb> bombedCells = new HashMap<>();
        for (Bomb b : bombs) {
            bombedCells.put(b.position(), b);
        }
        return bombedCells;
    }

    /**
     * Retourne l'ensemble des cases sur lesquelles se trouve au moins une
     * particule d'explosion étant une liste de séquence de particules
     * d'explosions
     * 
     * @param blasts
     *            les particules d'explosion
     * @return l'ensemble des cases sur lesquelles se trouve au moins une
     *         particule d'explosion étant une liste de séquence de particules
     *         d'explosions
     */
    private static Set<Cell> blastedCells(List<Sq<Cell>> blasts) {
        Set<Cell> blastedCells = new HashSet<>();
        for (Sq<Cell> sq : blasts) {
            blastedCells.add(sq.head());
        }
        return blastedCells;
    }

    /**
     * Retourne la liste des joueurs dont l'ordre dépend des permutations et du
     * nombre de ticks actuel
     * 
     * @return la liste des joueurs dont l'ordre dépend des permutations et du
     *         nombre de ticks actuel
     */
    private List<Player> playersInOrder() {
        List<PlayerID> ids = PERMUTATIONS.get(ticks % PERMUTATIONS.size());
        List<Player> playersInOrder = new ArrayList<>();
        for (PlayerID id : ids) {
            for (Player p : players0) {
                if (p.id() == id)
                    playersInOrder.add(p);
            }
        }
        return playersInOrder;
    }
}

