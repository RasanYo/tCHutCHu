package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Ticket;

/**
 * interface fonctionnel représentant les actions que peut effectuer le joueur.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public interface ActionHandlers {

    /**
     * Gestionnaire pour le tirage de billets
     */
    @FunctionalInterface
    interface DrawTicketsHandler{
        void onDrawTickets();
    }

    /**
     * Gestionnaire pour la tirage de cartes
     */
    @FunctionalInterface
    interface DrawCardHandler{
        void onDrawCard(int i);
    }

    /**
     * Gestionnaire pour l'emparage d'une route
     */
    @FunctionalInterface
    interface ClaimRouteHandler{
        void onClaimRoute(Route route, SortedBag<Card> sc);
    }

    /**
     * Gestionnaire pour la selection de billets
     */
    @FunctionalInterface
    interface ChooseTicketsHandler{
        void onChooseTickets(SortedBag<Ticket> st);
    }

    /**
     * Gestionnaire pour la selection de cartes
     */
    @FunctionalInterface
    interface ChooseCardsHandler{
        void onChooseCards(SortedBag<Card> sc);
    }

    //    ################################################# EXTENSIONS #################################################

    @FunctionalInterface
    interface ChooseNameHandler{
        void onChooseName(String name);
    }

    @FunctionalInterface
    interface DestroyRouteHandler{
        void onDestroyRoute(Route route);
    }
}