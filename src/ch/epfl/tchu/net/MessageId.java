package ch.epfl.tchu.net;

/**
 * énumération représentant les types de messages que peut recevoir/envoyer un client/mandataire.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public enum MessageId {
    INIT_PLAYERS,
    RECEIVE_INFO,
    UPDATE_STATE,
    SET_INITIAL_TICKETS,
    CHOOSE_INITIAL_TICKETS,
    NEXT_TURN,
    CHOOSE_TICKETS,
    DRAW_SLOT,
    ROUTE,
    CARDS,
    CHOOSE_ADDITIONAL_CARDS,

    //    ################################################# EXTENSIONS ########################################

    SEND_NAME,
    DESTROY_ROUTE

}
