package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * classe représentant une pioche.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 *
 * @param <C> le type des éléments de la pioche.
 */
public final class Deck<C extends Comparable<C>> {

    private final List<C> cardDeck;

    private Deck(List<C> cardDeck) {
        this.cardDeck = cardDeck;
    }

    /**
     * @param cards tas de cartes a melanger
     * @param rng   generateur aleatoire pour melanger les cartes
     * @param <C>   type de cartes
     * @return un tas de cartes ayant les memes cartes que le multiensemble cards, melangees au moyen du generateur de nombres aleatoires rng
     */
    public static <C extends Comparable<C>> Deck<C> of(SortedBag<C> cards, Random rng) {
        List<C> cardDeck = cards.toList();
        Collections.shuffle(cardDeck, rng);
        return new Deck<>(cardDeck);
    }

    /**
     * @return le nombre de cartes que contient le tas
     */
    public int size() {
        return cardDeck.size();
    }

    /**
     * @return boolean true si le tas est vide
     */
    public boolean isEmpty() {
        return cardDeck.isEmpty();
    }

    /**
     * @return la carte au sommet du tas
     * @throws IllegalArgumentException si le tas est vide
     */
    public C topCard() {
        Preconditions.checkArgument(!isEmpty());
        C topCard = cardDeck.get(0);
        return topCard;
    }

    /**
     * @return la carte au sommet du tas
     * @throws IllegalArgumentException si le tas est vide
     */
    public Deck<C> withoutTopCard() {
        Preconditions.checkArgument(!isEmpty());
        return withoutTopCards(1);
    }

    /**
     * @param count int nombre de cartes au sommet à retourner
     * @return le multiensemble des count cartes au sommet
     * @throws IllegalArgumentException si 0 <= count <= taille du tas
     */
    public SortedBag<C> topCards(int count) {
        Preconditions.checkArgument(count <= size() && count >= 0);
        List<C> topC = cardDeck.subList(0, count);
        return SortedBag.of(topC);
    }

    /**
     * @param count nombre de cartes a enlever du sommet du tas
     * @return identique au deck de depart sans les count cartes au sommet du tas initial
     * @throws IllegalArgumentException si count n'est pas compris entre 0 et la taille de la pioche (this).
     */
    public Deck<C> withoutTopCards(int count) {
        Preconditions.checkArgument(count >= 0 && count <= this.size());
        return new Deck<>(cardDeck.subList(count, size()));
    }

}
