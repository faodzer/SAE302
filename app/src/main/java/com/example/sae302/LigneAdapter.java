package com.example.sae302;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * LigneAdapter
 * ------------
 * Cette classe sert de pont entre la source de données (la liste des objets ModeleLigne.Record)
 * et la vue utilisateur (le RecyclerView).
 * Elle gère la création des éléments graphiques et lie les données de chaque ligne de bus
 * aux composants correspondants.
 */
public class LigneAdapter extends RecyclerView.Adapter<LigneAdapter.ViewHolder> {

    private final List<ModeleLigne.Record> listeLignes;
    private final OnLigneClickListener listener;

    /**
     * Interface de communication
     * Permet de déléguer la gestion du clic sur une ligne au Fragment ou à l'Activité appelante.
     */
    public interface OnLigneClickListener {
        void onLigneClick(ModeleLigne.Record ligne);
    }

    /**
     * Constructeur de l'adaptateur.
     * @param listeLignes Liste des données à afficher.
     * @param listener Callback pour la gestion des événements de clic.
     */
    public LigneAdapter(List<ModeleLigne.Record> listeLignes, OnLigneClickListener listener) {
        this.listeLignes = listeLignes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Gonflage" du layout XML item_bus pour créer une instance visuelle d'une ligne
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bus, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Récupération de l'objet de données correspondant à la position actuelle dans la liste
        ModeleLigne.Record ligne = listeLignes.get(position);

        if (ligne.fields != null) {
            // Affectation du numéro de ligne (ex: C1) au TextView correspondant
            holder.txtNumero.setText(ligne.fields.getNumeroAffiche());
            // Affectation du nom complet de la ligne (ex: Cesson-Sévigné - Chantepie)
            holder.txtNom.setText(ligne.fields.nom);

            // Installation du gestionnaire de clic sur l'ensemble de la cellule
            holder.itemView.setOnClickListener(v -> listener.onLigneClick(ligne));
        }
    }

    @Override
    public int getItemCount() {
        // Retourne la taille de la liste pour informer le RecyclerView du nombre d'éléments à créer
        return (listeLignes != null) ? listeLignes.size() : 0;
    }

    /**
     * Classe ViewHolder
     * -----------------
     * Conteneur d'objets permettant de garder en mémoire les références des vues (TextView).
     * Cela évite de répéter l'opération coûteuse findViewById() lors du défilement de la liste.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumero, txtNom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Liaison des variables avec les IDs du layout XML item_bus
            txtNumero = itemView.findViewById(R.id.textLigneNumero);
            txtNom = itemView.findViewById(R.id.textLigneNom);
        }
    }
}