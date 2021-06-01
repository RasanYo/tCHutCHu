package ch.epfl.tchu.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

/**
 * classe représentant l'état des cartes du jeu.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class CardState extends PublicCardState{

    private final Deck<Card> remainingDeck;
    private final SortedBag<Card> discardCards;

    /**
     *
     * @param faceUpCards la liste des cartes faces visibles
     * @param remainingDeck la pioche de cartes
     * @param discardCards la defausse de carte
     */
    private CardState(List<Card> faceUpCards, Deck<Card> remainingDeck, SortedBag<Card> discardCards) {
        super(faceUpCards, remainingDeck.size(), discardCards.size());
        this.remainingDeck = remainingDeck;
        this.discardCards = discardCards;
    }

    /**
     *
     * @param deck = la pioche
     * @return un etat dans lequel les 5 cartes disposees faces visibles sont les 5 premieres du tas donne,
     *         la pioche est constituee des cartes du tas restantes,
     *         et la defausse est vide.
     * @throws IllegalArgumentException si le tas donne contient moins de 5 cartes.
     */
    public static CardState of(Deck<Card> deck) {
        Preconditions.checkArgument(deck.size() >= Constants.FACE_UP_CARDS_COUNT);
        return new CardState(deck.topCards(Constants.FACE_UP_CARDS_COUNT).toList(), deck.withoutTopCards(Constants.FACE_UP_CARDS_COUNT), SortedBag.of());
    }

    /**
     *
     * @param slot index de la liste des cartes visible
     * @return retourne un ensemble de cartes identique au recepteur (this),
     *         si ce n'est que la carte face visible d'index slot a ete remplacee par celle se trouvant au sommet de la pioche,
     *         qui en est du meme coup retiree.
     * @throws IndexOutOfBoundsException si slot n'est pas compris entre 0 et 5.
     * @throws IllegalArgumentException si la pioche est vide.
     */
    public CardState withDrawnFaceUpCard(int slot) {
        Preconditions.checkArgument(!faceUpCards().isEmpty());
        Objects.checkIndex(0, Constants.FACE_UP_CARDS_COUNT);

        List<Card> newFaceUpCards = new ArrayList<>(faceUpCards());
        newFaceUpCards.set(slot, remainingDeck.topCard());

        return new CardState(newFaceUpCards, this.remainingDeck.withoutTopCard(), this.discardCards);
    }



        /**
         *
         * @return retourne la carte se trouvant au sommet de la pioche.
         * @throws IllegalArgumentException si la pioche est vide.
         */
    public Card topDeckCard() {
        Preconditions.checkArgument(!remainingDeck.isEmpty());
        return remainingDeck.topCard();
    }

    /**
     *
     * @return retourne un ensemble de cartes identique au recepteur (this),
     *         mais sans la carte se trouvant au sommet de la pioche.
     * @throws IllegalArgumentException si la pioche est vide.
     */
    public CardState withoutTopDeckCard() {
        Preconditions.checkArgument(!remainingDeck.isEmpty());
        return new CardState(faceUpCards(), remainingDeck.withoutTopCard(), discardCards);
    }

    /**
     *
     * @param rng Random rng
     * @return retourne un ensemble de cartes identique au recepteur (this),
     *         si ce n'est que les cartes de la defausse ont ete melangees
     *         au moyen du generateur aleatoire donne afin de constituer la nouvelle pioche.
     * @throws IllegalArgumentException si la pioche du récepteur n'est pas vide.
     */
    public CardState withDeckRecreatedFromDiscards(Random rng) {
        Preconditions.checkArgument(remainingDeck.isEmpty());
        return new CardState(faceUpCards(), Deck.of(SortedBag.of(this.discardCards), rng),  SortedBag.of());
    }

    /**
     *
     * @param additionalDiscards multiensemble de cartes retirees additionelles
     * @return retourne un ensemble de cartes identique au recepteur (this),
     *         mais avec les cartes donnees ajoutees a la defausse.
     */
    public CardState withMoreDiscardedCards(SortedBag<Card> additionalDiscards) {
        return new CardState(faceUpCards(), remainingDeck, SortedBag.of(this.discardCards).union(additionalDiscards));
    }
}
