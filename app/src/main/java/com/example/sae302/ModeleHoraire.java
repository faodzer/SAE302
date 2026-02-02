package com.example.sae302;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * ModeleHoraire
 * -------------
 * Cette classe définit la structure de données pour les horaires de bus.
 * Elle permet de mapper les résultats du dataset "prochains passages"
 * vers des objets Java exploitables par l'application.
 */
public class ModeleHoraire {

    /**
     * Représente l'objet racine de la réponse JSON renvoyée par l'API.
     */
    public static class Reponse {
        @SerializedName("records")
        public List<Record> records;
    }

    /**
     * Représente une entrée (une ligne) de la réponse.
     */
    public static class Record {
        @SerializedName("fields")
        public Fields fields;
    }

    /**
     * Contient les données métiers spécifiques à chaque passage de bus.
     */
    public static class Fields {
        @SerializedName("nomarret")
        public String arret;

        // Date et heure théorique d'arrivée au format ISO 8601
        @SerializedName("arrivee")
        public String heureArrivee;

        // Heure de départ prévue (utilisée si l'heure d'arrivée est absente)
        @SerializedName("depart")
        public String heureDepart;

        @SerializedName("destination")
        public String destination;

        // --- LOGIQUE MÉTIER INTÉGRÉE ---

        /**
         * Traite la chaîne de caractères brute reçue de l'API pour extraire
         * uniquement l'heure et les minutes.
         * * @return String L'heure formatée (ex: "14:30") ou "--:--" en cas d'erreur.
         */
        public String getHeureAffichee() {
            // Priorité à l'heure d'arrivée, sinon on utilise l'heure de départ
            String brute = (heureArrivee != null) ? heureArrivee : heureDepart;

            if (brute != null && brute.contains("T")) {
                try {
                    // Extraction de la partie horaire après le délimiteur "T"
                    // ex: "2024-01-22T14:30:00+01:00" -> "14:30"
                    return brute.split("T")[1].substring(0, 5);
                } catch (Exception e) {
                    return "--:--";
                }
            }
            return "--:--";
        }

        /**
         * Récupère la valeur brute complète pour permettre un tri chronologique
         * précis dans les listes.
         */
        public String getHeureBrute() {
            return (heureArrivee != null) ? heureArrivee : heureDepart;
        }
    }
}