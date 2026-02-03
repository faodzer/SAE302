package com.example.sae302;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * DetailsActivity
 * ----------------
 * Activité responsable de l'affichage détaillé d'une ligne de bus spécifique.
 * Elle permet de visualiser les horaires en temps réel, de filtrer par direction
 * et d'afficher les alertes de trafic (incidents) en cours.
 */

public class DetailsActivity extends AppCompatActivity {

    // --- VUES (Interface Graphique) ---
    private RecyclerView recyclerPassages;
    private TextView tvPasDeBus;

    // Composants dédiés à l'affichage des alertes
    private CardView carteAlerte;
    private TextView tvTitreAlerte, tvDescriptionAlerte;

    // Boutons permettant de choisir le terminus (direction)
    private Button btnDirection1, btnDirection2;


    // Liste temporaire pour stocker la totalité des horaires récupérés depuis l'API
    private List<ModeleHoraire.Record> listeCompleteDesPassages;
    private String destination1 = null;
    private String destination2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // 1. Initialisation des Vues à partir du layout XML
        carteAlerte = findViewById(R.id.carteAlerte);
        tvTitreAlerte = findViewById(R.id.tvTitreAlerte);
        tvDescriptionAlerte = findViewById(R.id.tvDescriptionAlerte);
        tvPasDeBus = findViewById(R.id.tvPasDeBus);
        recyclerPassages = findViewById(R.id.recyclerPassages);
        btnDirection1 = findViewById(R.id.btnDirection1);
        btnDirection2 = findViewById(R.id.btnDirection2);

        // Configuration du gestionnaire de mise en page pour la liste des horaires
        recyclerPassages.setLayoutManager(new LinearLayoutManager(this));

        // 2. Configuration de la Toolbar (Barre de titre supérieure)
        Toolbar toolbar = findViewById(R.id.toolbarDetails);
        String numeroVisible = getIntent().getStringExtra("CLE_NUMERO_AFFICHE");
        toolbar.setTitle(numeroVisible != null ? "Ligne " + numeroVisible : "Détails");
        setSupportActionBar(toolbar);
        // Gestion du bouton de retour arrière
        toolbar.setNavigationOnClickListener(v -> finish());

        // 3. Définition des écouteurs de clics pour le filtrage par direction
        btnDirection1.setOnClickListener(v -> filtrerEtAfficher(destination1, btnDirection1, btnDirection2));
        btnDirection2.setOnClickListener(v -> filtrerEtAfficher(destination2, btnDirection2, btnDirection1));

        // 4. Récupération des paramètres transmis par l'activité précédente et lancement des requêtes API
        String idApi = getIntent().getStringExtra("CLE_ID_POUR_API");
        if (idApi != null) {
            chargerHoraires(idApi);
            verifierAlertes(idApi);
        } else {
            Toast.makeText(this, "Erreur : ID ligne manquant", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Interroge l'API pour récupérer les passages en temps réel de la ligne.
     * @param codeTechnique Identifiant unique de la ligne pour l'API.
     */
    private void chargerHoraires(String codeTechnique) {
        // Requête demandant jusqu'à 100 enregistrements pour couvrir les deux directions
        ApiClient.getService().getPassagesTempsReel(
                "prochains-passages-des-lignes-de-bus-du-reseau-star-en-temps-reel",
                "nomcourtligne:\"" + codeTechnique + "\"",
                100
        ).enqueue(new Callback<ModeleHoraire.Reponse>() {
            @Override
            public void onResponse(@NonNull Call<ModeleHoraire.Reponse> call, @NonNull Response<ModeleHoraire.Reponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listeCompleteDesPassages = response.body().records;

                    if (listeCompleteDesPassages == null || listeCompleteDesPassages.isEmpty()) {
                        // Affichage d'un message informatif si aucun bus n'est en circulation
                        afficherEtatVide(true);
                    } else {
                        // Analyse des données pour identifier les terminus et configurer l'interface
                        analyserDirections(listeCompleteDesPassages);
                        // Affichage par défaut de la première direction identifiée
                        filtrerEtAfficher(destination1, btnDirection1, btnDirection2);
                    }
                } else {
                    Toast.makeText(DetailsActivity.this, "Erreur serveur (Horaires)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ModeleHoraire.Reponse> call, @NonNull Throwable t) {
                Toast.makeText(DetailsActivity.this, "Erreur connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Parcourt la liste des passages pour extraire les deux destinations principales de la ligne.
     */
    private void analyserDirections(List<ModeleHoraire.Record> liste) {
        for (ModeleHoraire.Record r : liste) {
            if (r.fields == null || r.fields.destination == null) continue;

            String dest = r.fields.destination;

            // Mémorisation de la première destination unique rencontrée
            if (destination1 == null) {
                destination1 = dest;
            }
            // Mémorisation de la seconde destination unique si elle diffère de la première
            else if (!dest.equals(destination1) && destination2 == null) {
                destination2 = dest;
            }
        }

        // Mise à jour du texte et de la visibilité des boutons de direction
        configurerBouton(btnDirection1, destination1);
        configurerBouton(btnDirection2, destination2);
    }

    /**
     * Affiche ou masque un bouton selon si une destination a été trouvée.
     */
    private void configurerBouton(Button btn, String destination) {
        if (destination != null) {
            btn.setText("Vers " + destination);
            btn.setVisibility(View.VISIBLE);
        } else {
            btn.setVisibility(View.GONE);
        }
    }

    /**
     * Filtre la liste globale pour n'afficher que les bus allant vers la destination sélectionnée.
     */
    private void filtrerEtAfficher(String destinationFiltre, Button btnActif, Button btnInactif) {
        if (listeCompleteDesPassages == null || destinationFiltre == null) return;

        // 1. Mise à jour visuelle des boutons (Changement de couleur pour indiquer la sélection)
        int couleurActive = getResources().getColor(R.color.purple_500);
        int couleurInactive = 0xFFAAAAAA; // Gris statique
        btnActif.setBackgroundColor(couleurActive);
        btnInactif.setBackgroundColor(couleurInactive);

        // 2. Création de la sous-liste contenant uniquement les passages correspondants au filtre
        List<ModeleHoraire.Record> listeFiltree = new ArrayList<>();
        for (ModeleHoraire.Record r : listeCompleteDesPassages) {
            if (r.fields != null && destinationFiltre.equals(r.fields.destination)) {
                listeFiltree.add(r);
            }
        }

        // 3. Mise à jour de l'affichage du RecyclerView
        if (listeFiltree.isEmpty()) {
            afficherEtatVide(true);
        } else {
            afficherEtatVide(false);

            // Tri de la liste filtrée par ordre chronologique de passage
            Collections.sort(listeFiltree, (r1, r2) -> {
                String h1 = r1.fields.getHeureBrute();
                String h2 = r2.fields.getHeureBrute();
                if (h1 == null) return 1;
                if (h2 == null) return -1;
                return h1.compareTo(h2);
            });

            // Attribution de la liste filtrée à l'adaptateur du RecyclerView
            recyclerPassages.setAdapter(new PassageAdapter(listeFiltree));
        }
    }

    /**
     * Gère la visibilité entre la liste des résultats et le message d'absence de bus.
     */
    private void afficherEtatVide(boolean vide) {
        recyclerPassages.setVisibility(vide ? View.GONE : View.VISIBLE);
        tvPasDeBus.setVisibility(vide ? View.VISIBLE : View.GONE);
    }


    // LES ALERTES


    /**
     * Vérifie si des incidents de trafic concernent actuellement la ligne.
     * @param codeTechnique Identifiant de la ligne pour filtrer les alertes.
     */
    private void verifierAlertes(String codeTechnique) {
        ApiClient.getService().getAlertesLigne(
                "alertes-trafic-en-temps-reel-sur-les-lignes-du-reseau-star",
                "titre:\"" + codeTechnique + "\""
        ).enqueue(new Callback<ModeleAlerteDetails.Reponse>() {
            @Override
            public void onResponse(Call<ModeleAlerteDetails.Reponse> call, Response<ModeleAlerteDetails.Reponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ModeleAlerteDetails.Record> alertes = response.body().records;

                    // Si des alertes existent, on affiche la zone d'alerte avec les informations reçues
                    if (alertes != null && !alertes.isEmpty()) {
                        ModeleAlerteDetails.Fields incident = alertes.get(0).fields;
                        carteAlerte.setVisibility(View.VISIBLE);
                        tvTitreAlerte.setText(incident.titre);
                        tvDescriptionAlerte.setText(incident.description);
                    } else {
                        // Aucune alerte : on masque totalement le composant
                        carteAlerte.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ModeleAlerteDetails.Reponse> call, Throwable t) {
                // Erreur réseau : on masque par défaut la zone d'alerte
                carteAlerte.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Classe de modèle interne
     * Structure de données dédiée à la réception des alertes trafic via l'API.
     */
    public static class ModeleAlerteDetails {
        public static class Reponse {
            @SerializedName("records") public List<Record> records;
        }
        public static class Record {
            @SerializedName("fields") public Fields fields;
        }
        public static class Fields {
            @SerializedName("titre") public String titre;
            @SerializedName("description") public String description;
        }
    }
}