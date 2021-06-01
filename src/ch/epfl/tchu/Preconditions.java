package ch.epfl.tchu;

/**
 * classe n'ayant qu'une méthode ayant pour but de lancer une execption
 * si l'argument (booléenne) est faux.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Preconditions {

    private Preconditions() {
    }

    /**
     * @param shouldBeTrue une booléenne qui doit être vraie.
     * @throws IllegalArgumentException si la booléenne est fausse.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
