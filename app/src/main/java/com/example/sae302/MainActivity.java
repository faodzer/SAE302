package com.example.sae302;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable checkInternetLoop;
    private static final long MIN_SPLASH_DURATION = 3000;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enregistre l'heure de début
        startTime = System.currentTimeMillis();

        // On définit la boucle de vérification
        checkInternetLoop = new Runnable() {
            @Override
            public void run() {
                // Calcule le temps écoulé depuis le lancement
                long elapsedTime = System.currentTimeMillis() - startTime;

                // 1. On vérifie si on a internet
                if (isInternetAvailable()) {
                    // 2. Si on a internet, on vérifie si les 3 secondes sont passées
                    if (elapsedTime >= MIN_SPLASH_DURATION) {
                        // Si oui, on passe à la page suivante
                        goToNextActivity();
                    } else {
                        // Sinon, on attend juste le temps restant
                        long remainingTime = MIN_SPLASH_DURATION - elapsedTime;
                        handler.postDelayed(MainActivity.this::goToNextActivity, remainingTime);
                    }
                } else {
                    // 3. Si on n'a PAS internet
                    // On affiche un message...
                    Toast.makeText(MainActivity.this, "Aucune connexion internet détectée. Réessai...", Toast.LENGTH_SHORT).show();
                    // ...et on relance cette même vérification dans 3 secondes.
                    handler.postDelayed(this, 3000);
                }
            }
        };
    }

    private void goToNextActivity() {
        if (isFinishing()) return;
        startActivity(new Intent(MainActivity.this, pageAcceuil.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // On lance la boucle de vérification une seule fois
        handler.post(checkInternetLoop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // On arrête toutes les vérifications quand l'app passe en arrière-plan
        handler.removeCallbacksAndMessages(null);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // La méthode getActiveNetworkInfo() est obsolète sur les nouvelles versions d'Android,
        // mais elle reste la plus simple et fonctionne encore pour ce cas d'usage.
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
