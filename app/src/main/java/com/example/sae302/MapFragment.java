package com.example.sae302;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * MapFragment
 * -----------
 * Ce fragment est destiné à l'affichage de la carte interactive du réseau.
 * * État actuel : En cours de développement.
 * Rôle futur : Intégrer une API de cartographie (type Google Maps ou OpenStreetMap)
 * pour permettre aux utilisateurs de visualiser géographiquement les arrêts et les
 * lignes de bus en temps réel.
 */
public class MapFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /**
         * Chargement de l'interface graphique.
         * Note : Le layout fragment_map contient actuellement un message d'attente
         * pour informer l'utilisateur que cette fonctionnalité sera disponible prochainement.
         */
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
}