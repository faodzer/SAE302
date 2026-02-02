package com.example.sae302;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * ModeleLigne
 * -----------
 * Cette classe sert de modèle de données (POJO) pour représenter une ligne de bus.
 * Elle permet de mapper les données JSON provenant du jeu de données
 * "lignes-du-reseau-star-de-rennes-metropole" vers des objets Java exploitables.
 */
public class ModeleLigne {

    /**
     * Classe représentant la racine de la réponse JSON de l'API.
     */
    public static class Reponse {
        // Liste contenant tous les enregistrements (lignes de bus) retournés par la requête
        @SerializedName("records")
        public List<Record> records;
    }

    /**
     * Classe représentant un enregistrement individuel dans le dataset.
     */
    public static class Record {
        // Objet contenant les données métiers de la ligne
        @SerializedName("fields")
        public Fields fields;
    }

    /**
     * Classe contenant les champs de données spécifiques à une ligne de bus.
     */
    public static class Fields {

        // Numéro court de la ligne (ex: "C1", "12", "a").
        // L'annotation "alternate" permet de gérer les différentes versions de nommage du champ selon le dataset.
        @SerializedName(value = "li_num", alternate = {"nomcourt", "numero", "nom_court"})
        public String numero;

        // Nom complet de la ligne (ex: "Cesson-Sévigné (Champs Blancs) / Chantepie (Rosa Parks)")
        @SerializedName(value = "li_nom", alternate = {"nomfamille", "nom", "nom_long"})
        public String nom;

        // Identifiant technique unique utilisé pour les requêtes API croisées
        @SerializedName(value = "id", alternate = {"id_ligne", "li_id"})
        public String idTechnique;

        // Code couleur hexadécimal associé à la ligne sur le réseau
        @SerializedName(value = "couleur", alternate = {"couleur_trace", "li_couleur"})
        public String couleurHexa;

        /**
         * Retourne le numéro de la ligne ou un caractère de remplacement si la donnée est absente.
         * @return String Le numéro à afficher dans l'interface utilisateur.
         */
        public String getNumeroAffiche() {
            return (numero != null) ? numero : "?";
        }
    }
}