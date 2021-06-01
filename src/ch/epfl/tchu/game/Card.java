package ch.epfl.tchu.game;

import java.util.List;

/**
 * énumération représentant une carte.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public enum Card {
    BLACK(Color.BLACK),
    VIOLET(Color.VIOLET),
    BLUE(Color.BLUE),
    GREEN(Color.GREEN),
    YELLOW(Color.YELLOW),
    ORANGE(Color.ORANGE),
    RED(Color.RED),
    WHITE(Color.WHITE),
    LOCOMOTIVE(null),

    BOMB(Color.BOMB);

    /**
     * retourne la liste de toute les cartes dans tCHu.
     */
    public static final List<Card> ALL = List.of(values());

    /**
     * retourne la taille de la liste de cartes dans tCHu.
     */
    public static final int COUNT = ALL.size();

    /**
     * retourne la liste de wagons dans tCHu.
     */
    public static final List<Card> CARS = ALL.subList(0, COUNT - 2);

    private final Color color;

    Card(Color color) {
        this.color = color;
    }

    /**
     * @param color la couleur dont nous cherchons la carte qui correspond
     * @return Card la carte correspondant à la couleur passee en parametre
     */
    public static Card of(Color color) {
        return color == null ? LOCOMOTIVE : Card.valueOf(color.name());
    }

    /**
     * @return la couleur de la carte
     */
    public Color color() {
        return color;
    }

}
