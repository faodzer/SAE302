package com.example.sae302;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * PassageAdapter
 * --------------
 * Cet adaptateur est chargé de gérer l'affichage de la liste des prochains passages
 * dans l'écran de détails d'une ligne.
 * Il fait le lien entre les données de temps réel (ModeleHoraire) et le layout
 * graphique individuel (item_passage).
 */
public class PassageAdapter extends RecyclerView.Adapter<PassageAdapter.ViewHolder> {

    // Liste des enregistrements de passages récupérés via l'API
    private final List<ModeleHoraire.Record> listePassages;

    /**
     * Constructeur de l'adaptateur.
     * @param listePassages La liste des objets contenant les informations de passage.
     */
    public PassageAdapter(List<ModeleHoraire.Record> listePassages) {
        this.listePassages = listePassages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Inflation" du layout XML item_passage pour chaque ligne de la liste
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_passage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Récupération des données pour la position demandée
        ModeleHoraire.Record passage = listePassages.get(position);

        if (passage.fields != null) {
            /**
             * Utilisation de la logique métier définie dans la classe ModeleHoraire
             * pour transformer la date ISO brute en une heure lisible (HH:mm).
             */
            holder.tvHeure.setText(passage.fields.getHeureAffichee());

            // Affichage du nom de l'arrêt de bus
            holder.tvArret.setText(passage.fields.arret);

            // Affichage de la destination finale (terminus) du véhicule
            holder.tvDestination.setText("Vers " + passage.fields.destination);
        }
    }

    @Override
    public int getItemCount() {
        // Retourne le nombre total d'éléments à afficher dans le RecyclerView
        return (listePassages != null) ? listePassages.size() : 0;
    }

    /**
     * ViewHolder
     * ----------
     * Classe interne optimisant les performances en conservant les références
     * vers les composants graphiques de l'élément de liste.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeure, tvArret, tvDestination;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Liaison des variables Java aux identifiants définis dans le XML item_passage
            tvHeure = itemView.findViewById(R.id.txtHeure);
            tvArret = itemView.findViewById(R.id.txtArret);
            tvDestination = itemView.findViewById(R.id.txtDestination);
        }
    }
}