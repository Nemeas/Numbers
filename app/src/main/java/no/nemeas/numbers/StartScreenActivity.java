package no.nemeas.numbers;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartScreenActivity extends Activity {

    private int lvl = 1;
    private static final int RC_LEADERBOARD_UI = 9004;

    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

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

        ((Button) findViewById(R.id.show_leaderboard_button)).setOnClickListener(new View.OnClickListener() {
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

    private Activity getThis() {
        return this;
    }

    private void showLeaderboard() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
            .getLeaderboardIntent(getString(R.string.leaderboard_highscore))
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_LEADERBOARD_UI);
                }
            });
    }
}
