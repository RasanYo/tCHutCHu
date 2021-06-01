package ch.epfl.tchu.game;

import java.util.List;

/**
 * énumération représentant l'identité d'un joueur.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public enum PlayerId {
    PLAYER_1,
    PLAYER_2;

    /**
     * retourne la liste de tout les joueurs dans une partie de tCHu.
     */
    public static final List<PlayerId> ALL = List.of(values());

    /**
     * retourne le nombre de joueurs dans une partie de tCHu.
     */
    public static final int COUNT = ALL.size();

    /**
     * @return l'identite du joueur suivant
     */
    public PlayerId next() {
        return this.equals(PLAYER_1) ? PLAYER_2 : PLAYER_1;
    }
}
