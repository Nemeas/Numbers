package no.nemeas.numbers;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends FragmentActivity implements
        GamePlayFragment.Listener,
        StartScreenFragment.Listener {

    // Fragments
    private GamePlayFragment mGamePlayFragment;
    private StartScreenFragment mStartScreenFragment;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient;

    // Client variables
    private LeaderboardsClient mLeaderBoardsClient;
    private EventsClient mEventsClient;
    private PlayersClient mPlayersClient;

    // request codes we use when invoking an external activity
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_LEADER_BOARD_UI = 9004;

    // Firebase
    private FirebaseAnalytics mFirebaseAnalytics;

    private final LeaderboardOutbox mOutbox = new LeaderboardOutbox();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        setLayout();

        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);

        // Create the fragments used by the UI.
        mGamePlayFragment = new GamePlayFragment().setListener(this).setScreenSize(point);
        mStartScreenFragment = new StartScreenFragment().setListener(this);

        switchToFragment(mStartScreenFragment);
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, try again later
            return;
        }
        if (mOutbox.mScore >= 0) {
            mLeaderBoardsClient.submitScore(getString(R.string.leaderboard_highscore),
                    mOutbox.mScore);
            mOutbox.mScore = -1;
        }
    }

    private void updateLeaderboards(int finalScore) {
        if (mOutbox.mScore < finalScore) {
            mOutbox.mScore = finalScore;
        }
    }

    @Override
    public void onBackPressed() {
        if (mGamePlayFragment.isVisible()) {
            // mGamePlayFragment.stop();
            switchToFragment(mStartScreenFragment);
            signInSilently();
            return;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Settings.DEBUG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();
    }

    private void signInSilently() {
        Log.d(Settings.DEBUG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
            new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    if (task.isSuccessful()) {
                        Log.d(Settings.DEBUG, "signInSilently(): success");
                        onConnected(task.getResult());
                    } else {
                        Log.d(Settings.DEBUG, "signInSilently(): failure", task.getException());
                        onDisconnected();
                    }
                }
            }
        );
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(Settings.DEBUG, "onConnected(): connected to Google APIs");

        mLeaderBoardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mEventsClient = Games.getEventsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        mStartScreenFragment.hideLogin();

        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
            .addOnCompleteListener(new OnCompleteListener<Player>() {
                @Override
                public void onComplete(@NonNull Task<Player> task) {
                    String imageUrl = null;
                    if (task.isSuccessful()) {
                        imageUrl = task.getResult().getIconImageUrl();
                    } else {
                        Exception e = task.getException();
                        Log.d(Settings.DEBUG, "Could not get player image", e);
                    }
                    mStartScreenFragment.setUserImage(imageUrl);
                }
            }
        );

        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
        }
    }

    private void onDisconnected() {
        Log.d(Settings.DEBUG, "onDisconnected()");

        mStartScreenFragment.showLogin();

        mLeaderBoardsClient = null;
        mPlayersClient = null;
    }

    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    // Switch UI to the given fragment
    private void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag).commit();
    }

    private void setLayout() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Set Portrait
    }

    @Override
    public void onNewHighScore(int score) {
        // update leaderboards
        updateLeaderboards(score);
    }

    @Override
    public void onGamePlayBackPressed() {
        switchToFragment(mStartScreenFragment);
        signInSilently();
    }

    @Override
    public void shake() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    @Override
    public void onShowLeaderBoard() {
        mLeaderBoardsClient.getLeaderboardIntent(getString(R.string.leaderboard_highscore))
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_UNUSED);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(Settings.DEBUG, "Show leaderboard fail", e);
                }
            }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty())
                    message = "obs";

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    @Override
    public void onSignOut() {
        finish();
    }

    @Override
    public void onSignIn() {
        startSignInIntent();
    }

    @Override
    public void onPlay(int lvl) {
        switchToFragment(mGamePlayFragment);
    }

    private class LeaderboardOutbox {
        int mScore = -1;

        boolean isEmpty() {
            return mScore < 0;
        }
    }
}
