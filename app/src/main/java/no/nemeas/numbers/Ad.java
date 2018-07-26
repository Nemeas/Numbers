package no.nemeas.numbers;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class Ad {
    private InterstitialAd mInterstitialAd;

    private final GameActivity context;

    public Ad(GameActivity context) {
        this.context = context;

        MobileAds.initialize(context, "ca-app-pub-8731827103414918~6135545007");

        doStuff();
    }

    public void doStuff() {
        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    public InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("ad", "Loaded");
                //mNextLevelButton.setEnabled(true);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d("ad", "failed to load");
                //mNextLevelButton.setEnabled(true);
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                Log.d("ad", "ad closed");
                context.nextLvl();
            }
        });
        return interstitialAd;
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        // mNextLevelButton.setEnabled(false);
        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

    public void showAd() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(context, "Ad did not load", Toast.LENGTH_SHORT).show();
            context.nextLvl();
        }
    }
}
