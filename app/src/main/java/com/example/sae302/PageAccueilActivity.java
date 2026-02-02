package com.example.sae302;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * PageAccueilActivity
 * -------------------
 * Activité principale servant de conteneur pour la navigation par onglets.
 * Elle utilise un BottomNavigationView pour permettre à l'utilisateur de basculer
 * entre les différentes fonctionnalités (Lignes, Plan, Infos) sans changer d'activité.
 */
public class PageAccueilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Liaison avec le layout XML définissant la barre de navigation et le conteneur de fragments
        setContentView(R.layout.activity_page_acceuil);

        // Initialisation du composant de navigation basse
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        /**
         * Configuration du fragment par défaut au premier lancement.
         * On vérifie 'savedInstanceState == null' pour éviter de recréer le fragment
         * lors d'une rotation de l'écran.
         */
        if (savedInstanceState == null) {
            // Chargement initial du fragment ListFragment (Liste des lignes)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ListFragment())
                    .commit();

            // Synchronisation visuelle de l'icône sélectionnée dans la barre de navigation
            bottomNav.setSelectedItemId(R.id.nav_list);
        }

        /**
         * Gestionnaire d'événements pour le changement d'onglet.
         * Cette logique remplace le contenu du conteneur (FrameLayout) par le fragment choisi.
         */
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            // Sélection du fragment correspondant à l'icône cliquée
            if (id == R.id.nav_list) {
                selectedFragment = new ListFragment(); // Onglet Liste des bus
            } else if (id == R.id.nav_map) {
                selectedFragment = new MapFragment();  // Onglet Carte du réseau
            } else if (id == R.id.nav_info) {
                selectedFragment = new InfoFragment(); // Onglet Infos Pratiques (Alertes trafic)
            }

            // Exécution de la transaction de fragment si une destination valide est trouvée
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}