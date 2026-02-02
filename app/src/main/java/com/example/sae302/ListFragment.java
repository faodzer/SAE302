package com.example.sae302;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ListFragment
 * ------------
 * Fragment principal de l'application affichant la liste des lignes de bus.
 * Il gère la récupération des données géographiques, le nettoyage des doublons
 * et l'organisation (tri) des lignes selon la hiérarchie du réseau STAR.
 */
public class ListFragment extends Fragment implements LigneAdapter.OnLigneClickListener {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialisation de la vue du fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Configuration du RecyclerView pour un affichage en liste
        recyclerView = view.findViewById(R.id.recyclerViewBus);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Appel de la méthode de chargement des données au démarrage
        chargerLignes();

        return view;
    }

    /**
     * Interroge l'API pour récupérer la totalité des lignes du réseau.
     */
    private void chargerLignes() {
        // Appel au dataset des lignes (limité à 500 pour obtenir l'intégralité du réseau)
        ApiClient.getService().getAllBusLines("lignes-du-reseau-star-de-rennes-metropole", 500)
                .enqueue(new Callback<ModeleLigne.Reponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ModeleLigne.Reponse> call, @NonNull Response<ModeleLigne.Reponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            List<ModeleLigne.Record> listeBrute = response.body().records;
                            List<ModeleLigne.Record> listeNettoyee = new ArrayList<>();

                            // --- LOGIQUE DE DÉDUPLICATION ---
                            // Le dataset peut contenir plusieurs enregistrements pour une même ligne (tronçons différents).
                            // On utilise un HashSet pour mémoriser les numéros déjà ajoutés et éviter les doublons.
                            Set<String> numerosDejaVus = new HashSet<>();

                            if (listeBrute != null) {
                                for (ModeleLigne.Record ligne : listeBrute) {
                                    if (ligne.fields != null && ligne.fields.numero != null) {
                                        // Si le numéro de ligne n'est pas encore dans notre Set, on l'ajoute à la liste finale
                                        if (!numerosDejaVus.contains(ligne.fields.numero)) {
                                            listeNettoyee.add(ligne);
                                            numerosDejaVus.add(ligne.fields.numero);
                                        }
                                    }
                                }

                                // --- LOGIQUE DE TRI ---
                                // Tri de la liste nettoyée en utilisant un comparateur personnalisé basé sur le "poids" des lignes.
                                Collections.sort(listeNettoyee, new Comparator<ModeleLigne.Record>() {
                                    @Override
                                    public int compare(ModeleLigne.Record r1, ModeleLigne.Record r2) {
                                        String num1 = r1.fields.numero;
                                        String num2 = r2.fields.numero;

                                        // Calcul de l'importance (poids) de chaque ligne
                                        int poids1 = getPoidsLigne(num1);
                                        int poids2 = getPoidsLigne(num2);

                                        // Si les poids sont différents, on trie par importance (ex: Chronostar avant Urbain)
                                        if (poids1 != poids2) return poids1 - poids2;

                                        // Si les poids sont identiques, on effectue un tri alphanumérique classique
                                        return num1.compareToIgnoreCase(num2);
                                    }
                                });
                            }

                            // Initialisation de l'adaptateur avec la liste triée et dédupliquée
                            LigneAdapter adapter = new LigneAdapter(listeNettoyee, ListFragment.this);
                            recyclerView.setAdapter(adapter);

                        } else {
                            afficherErreur("Erreur serveur : " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ModeleLigne.Reponse> call, @NonNull Throwable t) {
                        afficherErreur("Problème de connexion");
                    }
                });
    }

    /**
     * Attribue une priorité numérique à une ligne selon son type pour le tri.
     * @param numero Le numéro de la ligne (ex: "C1", "12", "a")
     * @return int Le poids de la ligne (plus le chiffre est petit, plus la ligne est prioritaire)
     */
    private int getPoidsLigne(String numero) {
        if (numero == null || numero.isEmpty()) return 999;
        String numLower = numero.toLowerCase();

        // Priorité 1 : Lignes Chronostar (commencent par C)
        if (numLower.startsWith("c") && numLower.length() < 4) return 1;

        // Priorité 2 : Métro (a ou b)
        if (numLower.equals("a") || numLower.equals("b")) return 2;

        // Priorité 3 : Lignes Urbaines (numéros inférieurs à 100)
        if (estNumerique(numero) && Integer.parseInt(numero) < 100) return 3;

        // Priorité 4 : Lignes Suburbaines (numéros 100 et +)
        if (estNumerique(numero)) return 4;

        // Priorité 10 : Lignes Scolaires (ts)
        if (numLower.startsWith("ts")) return 10;

        // Priorité 20 : Lignes Express (ex)
        if (numLower.contains("ex")) return 20;

        return 100;
    }

    /**
     * Vérifie si une chaîne de caractères représente un nombre entier.
     */
    private boolean estNumerique(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void afficherErreur(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gère l'événement de clic sur une ligne pour naviguer vers l'écran de détails.
     */
    @Override
    public void onLigneClick(ModeleLigne.Record ligne) {
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        if (ligne.fields != null) {
            // Transmission des identifiants nécessaires à l'activité de détails
            intent.putExtra("CLE_ID_POUR_API", ligne.fields.numero);
            intent.putExtra("CLE_NUMERO_AFFICHE", ligne.fields.getNumeroAffiche());
        }
        startActivity(intent);
    }
}