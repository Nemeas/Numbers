package no.nemeas.numbers;

import android.accounts.Account;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartScreenActivity extends Activity {

    private int lvl = 1;
    private static final int RC_LEADERBOARD_UI = 9004;
    private FirebaseAnalytics mFirebaseAnalytics;

    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.startscreen);

        GoogleSignInAccount account = (GoogleSignInAccount) getIntent().getParcelableExtra("account");

        PlayersClient playersClient = Games.getPlayersClient(this, account);

        playersClient.getCurrentPlayer().addOnCompleteListener(new OnCompleteListener<Player>() {
            @Override
            public void onComplete(@NonNull Task<Player> task) {
                Player p = task.getResult();
                CircleImageView profileImage = (CircleImageView) findViewById(R.id.profile_image);
                new DownloadImageTask(profileImage).execute(p.getIconImageUrl() == null ? null : p.getIconImageUrl());
            }
        });

        ((ImageView) findViewById(R.id.show_leaderboard_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLeaderboard();
            }
        });

        Log.d(GameActivity.DEBUG, account.toJson());

        ImageView splashImage = (ImageView) findViewById(R.id.splash);
        splashImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lvl += 1;
            }
        });

        animateImage((ImageView) findViewById(R.id.show_leaderboard_button));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Set Portrait

        this.playButton = (Button) findViewById(R.id.playButton);
        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent i = new Intent(getThis(), GameActivity.class);
            i.putExtra("lvl", lvl);
            startActivity(i);
            }
        });
    }

    private void animateImage(final ImageView image) {
        RotateAnimation rotate = new RotateAnimation(
                -15, 35,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(1400);
        rotate.setRepeatMode(RotateAnimation.REVERSE);
        rotate.setInterpolator(new AccelerateInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);
        image.startAnimation(rotate);
    }

    private Activity getThis() {
        return this;
    }

    private void showLeaderboard() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
            .getLeaderboardIntent(getString(R.string.leaderboard_highscore))
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {

                    Bundle bundle = new Bundle();
                    mFirebaseAnalytics.logEvent("show_leaderboard", bundle);

                    startActivityForResult(intent, RC_LEADERBOARD_UI);
                }
            });
    }

    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        boolean userLoggedOut = (responseCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) && (requestCode == RC_LEADERBOARD_UI);
        if (userLoggedOut) {

            Bundle bundle = new Bundle();
            mFirebaseAnalytics.logEvent("logout", bundle);

            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
            finish();
        }
    }
}
