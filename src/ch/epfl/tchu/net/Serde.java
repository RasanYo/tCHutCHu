package ch.epfl.tchu.net;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * interface représentant les objet capable de sérialiser
 * et désérialiser des valeurs d'un type donné.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public interface Serde<T> {



    /**
     * Methode appelee pour serialiser un objet
     *
     * @param toSerialize objet que nous souhaitons serialiser, c'est-a-dire transformer en String
     * @return la chaine de caractere correspondant a l'objet serialise
     */
    String serialize(T toSerialize);

    /**
     * Methode appelee pour deserialiser un message
     *
     * @param toDeserialize message obtenu que nous souhaitons deserialiser, c'est-a-dire transformer la String
     *                      en l'objet qui lui correspond
     * @return l'objet correspondant au message
     */
    T deserialize(String toDeserialize);


    /**
     * Methode appelee pour obtenir le serde correspondant a la fonction de serialisation et de deserialisation
     *
     * @param serializer   fonction de serialisation
     * @param deserializer fonction de deserialisation
     * @return le serde correspondant aux deux fonctions
     */
    static <T> Serde<T> of(Function<T, String> serializer, Function<String, T> deserializer) {
        return new Serde<>() {
            @Override
            public String serialize(T toSerialize) {
                return serializer.apply(toSerialize);
            }

            @Override
            public T deserialize(String toDeserialize) {
                return deserializer.apply(toDeserialize);
            }
        };
    }

    /**
     * Methode qui retourne le serde correspondant a un ensemble de valeurs enumerees
     *
     * @param list liste de valeurs enumerees
     * @param <T>  type des valeurs
     * @return le serde correspondant a l'ensemble passee en argument
     */
    static <T> Serde<T> oneOf(List<T> list) {
        Preconditions.checkArgument(!list.isEmpty());
        Function<T, String> serializer = t -> String.valueOf(list.indexOf(t));
        Function<String, T> deserializer = s -> list.get(Integer.parseInt(s));
        return of(serializer, deserializer);
    }

    /**
     * Methode qui donne le serde capable de (de)serialiser
     * des listes de valeurs (de)serialiser par le serde donne,
     *
     * @param serde     le serde correspondant a l'ensemble de valeurs enumerees en question
     * @param separator caractere pour delimiter les valeurs serialisees
     * @param <T>       type de valeurs enumerees
     * @return le serde capable de (de)serialiser des listes des valeurs
     */
    static <T> Serde<List<T>> listOf(Serde<T> serde, CharSequence separator) {
        Preconditions.checkArgument(separator.length() > 0);
        Function<List<T>, String> serializer = l -> l.isEmpty() ? "" : l.stream()
                .map(serde::serialize)
                .collect(Collectors.joining(separator));

        Function<String, List<T>> deserializer = s -> s.equals("") ?
                List.of() :
                Arrays.stream(s.split(Pattern.quote(separator.toString()), -1))
                        .map(serde::deserialize)
                        .collect(Collectors.toList());

        return of(serializer, deserializer);
    }

    /**
     * Methode qui donne le serde capable de (de)serialiser
     * des multiensemble de valeurs (de)serialiser par le serde donne,
     *
     * @param serde     le serde correspondant a l'ensemble de valeurs enumerees en question
     * @param separator caractere pour delimiter les valeurs serialisees
     * @param <T>       type de valeurs enumerees
     * @return la serde capable de (de)serialiser des multiensembles des valeurs
     */
    static <T extends Comparable<T>> Serde<SortedBag<T>> bagOf(Serde<T> serde, CharSequence separator) {
        Preconditions.checkArgument(separator.length() > 0);
        Function<SortedBag<T>, String> serializer = sb -> sb.isEmpty() ?
                "" :
                listOf(serde, separator).serialize(sb.toList());

        Function<String, SortedBag<T>> deserializer = s -> s.equals("") ?
                SortedBag.of() :
                SortedBag.of(listOf(serde, separator).deserialize(s));

        return of(serializer, deserializer);
    }

}
