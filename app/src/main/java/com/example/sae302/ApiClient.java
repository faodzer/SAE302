package com.example.sae302;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Classe ApiClient
 * -----------------
 * Cette classe est responsable de la gestion de la connexion réseau.
 * Elle utilise le modèle de conception "Singleton" pour garantir qu'une seule instance
 * de Retrofit est créée et réutilisée dans toute l'application.
 */
public class ApiClient {

    // Instance unique de Retrofit (le client HTTP)
    private static Retrofit retrofit = null;

    /**
     * Méthode statique pour obtenir l'interface de service API.
     * Si l'instance n'existe pas encore, elle est initialisée ici.
     *
     * @return StarApiService L'interface permettant d'effectuer les requêtes.
     */
    public static StarApiService getService() {
        if (retrofit == null) {


            // Création du client HTTP
            OkHttpClient client = new OkHttpClient.Builder()
                    .build();

            // Configuration de l'instance Retrofit
            // - baseUrl : L'URL racine du serveur de données (Rennes Métropole)
            // - addConverterFactory : Convertisseur pour transformer automatiquement le JSON en objets Java
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://data.rennesmetropole.fr/") // Base par défaut
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        // Génération de l'implémentation de l'interface StarApiService
        return retrofit.create(StarApiService.class);
    }
}