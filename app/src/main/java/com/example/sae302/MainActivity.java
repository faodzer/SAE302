package com.example.sae302;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity
 * ------------
 * Cette activité sert d'écran de démarrage (Splash Screen) à l'application.
 * Ses rôles principaux sont :
 * 1. Afficher le logo de l'application pendant un temps minimum.
 * 2. Vérifier la présence d'une connexion internet avant de permettre l'accès aux données.
 * 3. Assurer la transition vers l'écran principal une fois les conditions remplies.
 */
public class MainActivity extends AppCompatActivity {

    // Le Handler permet de planifier des actions dans le futur sur le thread principal (UI Thread)
    private final Handler planificateur = new Handler(Looper.getMainLooper());

    // Runnable définit la "tâche" de vérification qui sera exécutée de manière répétée
    private Runnable tacheVerificationReseau;

    // Constante définissant la durée minimale d'affichage du logo (3000 ms = 3 secondes)
    private static final long TEMPS_ATTENTE_LOGO = 3000;

    // Variable stockant le timestamp précis du lancement de l'activité
    private long topDepartChrono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Liaison avec le layout XML activity_main (contenant le logo)
        setContentView(R.layout.activity_main);

        // Enregistrement de l'heure de démarrage pour calculer le temps d'affichage
        topDepartChrono = System.currentTimeMillis();

        /**
         * Définition de la logique de vérification réseau.
         * Cette tâche est encapsulée dans un Runnable pour pouvoir être planifiée ou annulée.
         */
        tacheVerificationReseau = new Runnable() {
            @Override
            public void run() {
                // Calcul du temps écoulé depuis l'ouverture de l'application
                long tempsEcoule = System.currentTimeMillis() - topDepartChrono;

                // Vérification de l'état de la connexion internet
                if (estConnecteAuReseau()) {
                    // --- CAS A : CONNEXION ÉTABLIE ---

                    if (tempsEcoule >= TEMPS_ATTENTE_LOGO) {
                        // Si le temps d'attente minimal est dépassé, on change d'écran immédiatement
                        lancerPageAccueil();
                    } else {
                        // Si la connexion est trouvée très vite, on attend la fin du délai des 3 secondes
                        long tempsRestant = TEMPS_ATTENTE_LOGO - tempsEcoule;
                        planificateur.postDelayed(MainActivity.this::lancerPageAccueil, tempsRestant);
                    }
                } else {
                    // --- CAS B : ABSENCE DE CONNEXION ---

                    // Information utilisateur via un message éphémère (Toast)
                    Toast.makeText(MainActivity.this, "En attente de connexion...", Toast.LENGTH_SHORT).show();

                    // RELANCE DE LA VÉRIFICATION : L'action se rappelle elle-même toutes les 3 secondes
                    // Cela crée une boucle de surveillance active du réseau.
                    planificateur.postDelayed(this, 3000);
                }
            }
        };
    }

    /**
     * Gère la transition sécurisée vers l'activité principale (PageAccueilActivity).
     */
    private void lancerPageAccueil() {
        // Sécurité : on n'effectue pas la transition si l'activité est en train d'être détruite
        if (isFinishing()) return;

        try {
            // Création d'un Intent pour basculer vers le nouvel écran
            Intent intent = new Intent(MainActivity.this, PageAccueilActivity.class);
            startActivity(intent);

            // Appel de finish() pour retirer MainActivity de la pile d'activités.
            // Ainsi, un appui sur le bouton "Retour" quittera l'application directement.
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur de transition", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Interroge les services système Android pour connaître l'état de la connectivité.
     * @return boolean Vrai si le réseau est disponible et connecté (Wi-Fi ou Données mobiles).
     */
    private boolean estConnecteAuReseau() {
        ConnectivityManager gestionnaireReseau = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (gestionnaireReseau == null) return false;

        // Récupération de l'état du réseau actif
        NetworkInfo infoReseau = gestionnaireReseau.getActiveNetworkInfo();

        return infoReseau != null && infoReseau.isConnected();
    }

    // --- GESTION DU CYCLE DE VIE ---

    @Override
    protected void onResume() {
        super.onResume();
        // Lorsque l'application revient au premier plan, on lance la vérification réseau
        planificateur.post(tacheVerificationReseau);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * Lorsque l'activité n'est plus visible, on stoppe impérativement les tâches planifiées.
         * Cela évite des fuites de mémoire et une consommation inutile de batterie en arrière-plan.
         */
        planificateur.removeCallbacksAndMessages(null);
    }
}