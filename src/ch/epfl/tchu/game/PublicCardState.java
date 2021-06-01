package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * classe représentant l'état publique des cartes.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public class PublicCardState {

    private final List<Card> faceUpCards = new ArrayList<>();
    private final int deckSize;
    private final int discardsSize;

    /**
     * construit un état publique des cartes du jeu.
     *
     * @param faceUpCards  liste de cartes face retournée.
     * @param deckSize     taille de la pioche de cartes.
     * @param discardsSize taille de la défausse.
     * @Throws IllegalArgumentException si la liste de cartes face retournées ne contient pas 5 cartes,
     * et si la taille de la pioche et de la défausse est inférieur à 0.
     */
    public PublicCardState(List<Card> faceUpCards, int deckSize, int discardsSize) {
        Preconditions.checkArgument(faceUpCards.size() == Constants.FACE_UP_CARDS_COUNT
                && deckSize >= 0 && discardsSize >= 0);
        this.faceUpCards.addAll(faceUpCards);
        this.deckSize = deckSize;
        this.discardsSize = discardsSize;
    }


    /**
     * @return List<Card> la liste comportant les 5 cartes face visible
     */
    public List<Card> faceUpCards() {
        return new ArrayList<>(faceUpCards);
    }

    /**
     * @param slot int index de la carte
     * @return Card la carte a l'index donne
     * @throws IndexOutOfBoundsException si on a pas 0 <= slot < 5
     */
    public Card faceUpCard(int slot) {
        Objects.checkIndex(0, Constants.FACE_UP_CARDS_COUNT);
        return faceUpCards().get(slot);
    }

    /**
     * @return int la taille de la pioche
     */
    public int deckSize() {
        return deckSize;
    }

    /**
     * @return boolean true si la pioche est vide
     */
    public boolean isDeckEmpty() {
        return deckSize == 0;
    }

    /**
     * @return int la taille de la defausse
     */
    public int discardsSize() {
        return discardsSize;
    }

}
