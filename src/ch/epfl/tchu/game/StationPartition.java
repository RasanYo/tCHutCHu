package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

/**
 *
 * classe représentant les par
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class StationPartition implements StationConnectivity {

    private final int[] partition;

    /**
     * Constructeur de StationPartition
     *
     * @param partition tableau de integer representant la partition des gares
     */
    private StationPartition(int[] partition) {
        this.partition = partition;
    }

    /**
     * retourne vrai si les gares donnees sont reliees par le reseau du joueur.
     *
     * @param s1 premiere gare
     * @param s2 seconde gare
     * @return true si les gares sont reliees par le reseau du joueur.
     */
    public boolean connected(Station s1, Station s2) {
        return s1.id() >= partition.length || s2.id() >= partition.length ?
                s1.id() == s2.id() : partition[s1.id()] == partition[s2.id()];
    }

    public final static class Builder {

        private final int[] partitionB;

        /**
         * Constructeur du Builder
         *
         * @param stationCount nombre de stations que le joueur peut connecter
         */
        public Builder(int stationCount) {
            Preconditions.checkArgument(stationCount >= 0);
            partitionB = new int[stationCount];
            for (int i = 0; i < stationCount; ++i) {
                partitionB[i] = i;
            }
        }

        /**
         * Builder qui construit une instance StationPartition
         *
         * @return la partition des stations
         */
        public StationPartition build() {
            for (int i = 0; i < partitionB.length; ++i) {
                partitionB[i] = representative(i);
            }
            return new StationPartition(partitionB);
        }

        /**
         * Connecte les partitions qui représentent les stations s1 et s2
         *
         * @param s1 la premiere station
         * @param s2 la deuxieme station
         * @return le meme builder avec les deux partitions connectees
         */
        public Builder connect(Station s1, Station s2) {
            partitionB[representative(s1.id())] = representative(s2.id());
            return this;
        }

        /**
         * @param stationId la station dont nous cherchons le representant
         * @return l'ID du representant
         */
        private int representative(int stationId) {
            while (partitionB[stationId] != stationId) {
                stationId = partitionB[stationId];
            }
            return stationId;
        }
    }
}