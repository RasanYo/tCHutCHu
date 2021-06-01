package ch.epfl.tchu.game;

import java.util.List;

/**
 * énumération représentant une couleur.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public enum Color {
    BLACK,
    VIOLET,
    BLUE,
    GREEN,
    YELLOW,
    ORANGE,
    RED,
    WHITE,
    BOMB;

    /**
     * retourne la liste de toute les couleurs.
     */
    public static final List<Color> ALL = List.of(values());

    /**
     * retourne le nombre de couleurs du jeu tCHu.
     */
    public static final int COUNT = ALL.size();

}
