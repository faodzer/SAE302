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

// "extends AppCompatActivity" signifie que c'est un écran android standard
public class MainActivity extends AppCompatActivity {

    // Le Handler permet de faire attendre les 3sec.
    // Looper.getMainLooper() permet d'exécuter sur l'interface graphique principal.
    private final Handler planificateur = new Handler(Looper.getMainLooper());

    // Runnable est une "Tâche" que l'on éxecute plus tard.
    // C'est ici qu'on va mettre notre logique de vérification.
    private Runnable tacheVerificationReseau;

    // pour que le logo s'affiche au moins 3000ms (3 secondes)
    private static final long TEMPS_ATTENTE_LOGO = 3000;

    // Variable qui stocke l'heure a la quelle l'app a démarré
    private long topDepartChrono;

    // --- LE DÉMARRAGE DE L'ACTIVITÉ ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On sélectionne le XML : on affiche le XML à l'écran (le logo)
        setContentView(R.layout.activity_main);

        // On déclenche le chronomètre au moment précis où l'écran se crée
        // System.currentTimeMillis() donne l'heure actuelle en millisecondes
        topDepartChrono = System.currentTimeMillis();

        // DÉFINITION DE LA TÂCHE
        // Attention : Ici on DÉFINIT la tâche, on ne la lance pas encore.
        tacheVerificationReseau = new Runnable() {
            @Override
            public void run() {
                // 1. On calcule combien de temps s'est écoulé depuis le lancement
                long tempsEcoule = System.currentTimeMillis() - topDepartChrono;

                // 2. On vérifie le "billet d'entrée" (Internet)
                if (estConnecteAuReseau()) {
                    // CAS A : Il y a internet !

                    if (tempsEcoule >= TEMPS_ATTENTE_LOGO) {
                        // Si ça fait déjà plus de 3 secondes qu'on attend par sur PageAcceuil
                        lancerPageAccueil();
                    } else {
                        // Si ça fait moins de 3 secondes (ex: connexion ultra rapide en 0.5s)
                        // On calcule le temps qu'il reste à attendre
                        long tempsRestant = TEMPS_ATTENTE_LOGO - tempsEcoule;

                        // On dit au planificateur : "Lance la page d'accueil dans X tempsRestant"
                        planificateur.postDelayed(MainActivity.this::lancerPageAccueil, tempsRestant);
                    }
                } else {
                    // CAS B : Pas d'internet !

                    // On affiche un petit message temporaire (Toast) en bas de l'écran
                    Toast.makeText(MainActivity.this, "En attente de connexion...", Toast.LENGTH_SHORT).show();

                    // LA BOUCLE : On dit au planificateur "Relance MOI-MÊME (this) dans 3 secondes"
                    // C'est ça qui crée la boucle de vérification tant qu'il n'y a pas de réseau.
                    planificateur.postDelayed(this, 3000);
                }
            }
        };
    }

    // --- METHODE DE NAVIGATION (Ouvrir la porte) ---
    private void lancerPageAccueil() {
        // Sécurité : Si l'activité est déjà en train de se fermer, on arrête tout pour éviter un crash
        if (isFinishing()) return;

        try {
            // L'Intent est une "Intention" de changer d'écran.
            // On part de "MainActivity.this" vers "PageAccueilActivity.class"
            // (Assure-toi d'avoir bien renommé ton fichier en PageAccueilActivity !)
            Intent intent = new Intent(MainActivity.this, PageAccueilActivity.class);
            startActivity(intent);

            // finish() est CRUCIAL : cela détruit l'écran de chargement.
            // Si l'utilisateur fait "Retour" depuis l'accueil, il quittera l'appli au lieu de revenir sur le logo.
            finish();
        } catch (Exception e) {
            // Si quelque chose plante (ex: nom de fichier incorrect), on l'écrit dans les logs (Logcat)
            Log.e("SAE302_ERROR", "Erreur lors du lancement", e);
            Toast.makeText(this, "Erreur de transition", Toast.LENGTH_LONG).show();
        }
    }

    // --- METHODE TECHNIQUE (Vérifier le badge) ---
    private boolean estConnecteAuReseau() {
        // On demande au système Android le service qui gère les connexions
        ConnectivityManager gestionnaireReseau = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Si le service n'existe pas (rare), on considère qu'il n'y a pas de réseau
        if (gestionnaireReseau == null) return false;

        // On récupère les infos du réseau actif (WiFi ou 4G)
        NetworkInfo infoReseau = gestionnaireReseau.getActiveNetworkInfo();

        // On retourne VRAI si on a des infos ET qu'elles disent "Connecté"
        return infoReseau != null && infoReseau.isConnected();
    }

    // --- CYCLE DE VIE (Quand l'utilisateur quitte/revient sur l'appli) ---

    @Override
    protected void onResume() {
        super.onResume();
        // QUAND L'ÉCRAN DEVIENT VISIBLE :
        // On lance la tâche de vérification immédiatement.
        planificateur.post(tacheVerificationReseau);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // QUAND L'ÉCRAN N'EST PLUS VISIBLE (L'utilisateur a mis l'appli en fond) :
        // On supprime toutes les tâches prévues.
        // C'est très important : ça évite que la boucle continue de tourner et de vider la batterie pour rien.
        planificateur.removeCallbacksAndMessages(null);
    }
}