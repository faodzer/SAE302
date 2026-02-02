package com.example.sae302;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * InfoFragment
 * ------------
 * Fragment responsable de l'affichage de l'onglet "Infos Pratiques".
 * Il récupère la liste complète des alertes trafic en cours sur l'ensemble
 * du réseau STAR pour informer l'utilisateur des perturbations globales.
 */
public class InfoFragment extends Fragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialisation de l'interface utilisateur à partir du layout fragment_info
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        // Liaison avec le RecyclerView défini dans le XML
        recyclerView = view.findViewById(R.id.recyclerAlertes);
        // Configuration du LayoutManager pour un affichage en liste verticale
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Déclenchement de la récupération des données
        chargerLesAlertes();

        return view;
    }

    /**
     * Effectue une requête API asynchrone pour récupérer les alertes trafic.
     */
    private void chargerLesAlertes() {
        // Appel au service API pour le dataset spécifique aux alertes (limité à 50 résultats)
        ApiClient.getService().getAllAlertes("alertes-trafic-en-temps-reel-sur-les-lignes-du-reseau-star", 50)
                .enqueue(new Callback<ModeleAlerteInterne.Reponse>() {
                    @Override
                    public void onResponse(Call<ModeleAlerteInterne.Reponse> call, Response<ModeleAlerteInterne.Reponse> response) {
                        // Vérification de la validité de la réponse
                        if (response.isSuccessful() && response.body() != null) {
                            List<ModeleAlerteInterne.Record> liste = response.body().records;

                            // Gestion du cas où aucune alerte n'est signalée
                            if (liste == null || liste.isEmpty()) {
                                Toast.makeText(getContext(), "Aucun incident ✅", Toast.LENGTH_SHORT).show();
                            } else {
                                // Initialisation et affectation de l'adaptateur pour afficher les données
                                AlerteAdapterInterne adapter = new AlerteAdapterInterne(liste);
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ModeleAlerteInterne.Reponse> call, Throwable t) {
                        // Affichage d'un message d'erreur en cas de problème réseau
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Erreur chargement", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // ==========================================
    // 1. LE MODÈLE DE DONNÉES (POJO)
    // Structure permettant de mapper le JSON reçu
    // ==========================================
    public static class ModeleAlerteInterne {
        public static class Reponse {
            @SerializedName("records") public List<Record> records;
        }
        public static class Record {
            @SerializedName("fields") public Fields fields;
        }
        public static class Fields {
            @SerializedName("titre") public String titre;
            @SerializedName("description") public String description;
            @SerializedName("niveau") public String niveau;
        }
    }

    // ==========================================
    // 2. L'ADAPTER (Gestionnaire de la liste)
    // Lie les données aux éléments graphiques
    // ==========================================
    private static class AlerteAdapterInterne extends RecyclerView.Adapter<AlerteAdapterInterne.ViewHolder> {

        private final List<ModeleAlerteInterne.Record> listeAlertes;

        public AlerteAdapterInterne(List<ModeleAlerteInterne.Record> listeAlertes) {
            this.listeAlertes = listeAlertes;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Création visuelle d'un élément d'alerte à partir du layout item_alerte
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_alerte, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Liaison des données d'un incident spécifique aux TextViews correspondants
            ModeleAlerteInterne.Record alerte = listeAlertes.get(position);
            if (alerte.fields != null) {
                holder.titre.setText(alerte.fields.titre);
                holder.description.setText(alerte.fields.description);
            }
        }

        @Override
        public int getItemCount() {
            return listeAlertes != null ? listeAlertes.size() : 0;
        }

        /**
         * ViewHolder
         * Classe interne stockant les références vers les composants graphiques
         * pour optimiser le défilement de la liste.
         */
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titre, description;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titre = itemView.findViewById(R.id.tvTitreAlerte);
                description = itemView.findViewById(R.id.tvDescriptionAlerte);
            }
        }
    }
}