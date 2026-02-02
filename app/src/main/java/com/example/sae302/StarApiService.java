package com.example.sae302;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface StarApiService
 * ------------------------
 * Cette interface définit les points d'entrée (endpoints) des API utilisées.
 * Retrofit utilise ces méthodes pour construire les requêtes HTTP dynamiquement.
 * Elle permet de séparer la configuration des URL de la logique métier de l'application.
 */
public interface StarApiService {

    /**
     * 1. RÉCUPÉRATION DE LA LISTE DES LIGNES (Utilisé par ListFragment)
     * Interroge le catalogue pour obtenir les informations statiques (numéros, noms).
     * * @param dataset Nom du jeu de données (ex: "lignes-du-reseau-star...")
     * @param rows Nombre maximum de résultats à retourner.
     * @return Un objet Call encapsulant la réponse convertie en ModeleLigne.
     */
    @GET("api/records/1.0/search/")
    Call<ModeleLigne.Reponse> getAllBusLines(
            @Query("dataset") String dataset,
            @Query("rows") int rows
    );

    /**
     * 2. HORAIRES EN TEMPS RÉEL (Utilisé par DetailsActivity)
     * Note : Cette requête utilise une URL absolue vers STAR Explore pour les données dynamiques.
     * * @param dataset Nom du jeu de données des passages.
     * @param filtre Requête de filtrage (ex: "idligne:C1") pour cibler une ligne précise.
     * @param rows Limite du nombre de passages à récupérer.
     * @return Un objet Call encapsulant les horaires (ModeleHoraire).
     */
    @GET("api/records/1.0/search/")
    Call<ModeleHoraire.Reponse> getPassagesTempsReel(
            @Query("dataset") String dataset,
            @Query("q") String filtre,
            @Query("rows") int rows
    );

    /**
     * 3. LISTE GLOBALE DES ALERTES (Utilisé par InfoFragment)
     * Récupère l'ensemble des perturbations du réseau pour l'onglet d'information.
     * * @param dataset Dataset des alertes trafic.
     * @param rows Nombre d'alertes à afficher.
     * @return Utilise le modèle de données interne défini dans InfoFragment.
     */
    @GET("api/records/1.0/search/")
    Call<InfoFragment.ModeleAlerteInterne.Reponse> getAllAlertes(
            @Query("dataset") String dataset,
            @Query("rows") int rows
    );

    /**
     * 4. ALERTES SPÉCIFIQUES À UNE LIGNE (Utilisé par DetailsActivity)
     * Permet d'afficher un bandeau d'incident uniquement si la ligne consultée est impactée.
     * * @param dataset Dataset des alertes trafic.
     * @param filtre Recherche par titre ou identifiant de ligne.
     * @return Utilise le modèle de données interne défini dans DetailsActivity.
     */
    @GET("api/records/1.0/search/")
    Call<DetailsActivity.ModeleAlerteDetails.Reponse> getAlertesLigne(
            @Query("dataset") String dataset,
            @Query("q") String filtre
    );
}