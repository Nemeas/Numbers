package no.nemeas.numbers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SignInActivity extends Activity {

    private int RC_SIGN_IN = 13123235;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (isSignedIn())
            spawnStartScreenActivity(GoogleSignIn.getLastSignedInAccount(this));

        setContentView(R.layout.sign_in);
    }

    private void spawnStartScreenActivity(GoogleSignInAccount account) {
        Intent i = new Intent(this, StartScreenActivity.class);
        i.putExtra("account", account);
        startActivity(i);
        finish();
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    @Override
    public void onResume() {
        super.onResume();
        signInSilently();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                spawnStartScreenActivity(signedInAccount);

                Bundle bundle = new Bundle();
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
            } else {
                String message = result.getStatus().getStatusMessage();
                Bundle bundle = new Bundle();
                bundle.putString("message", message == null ? result.getStatus().toString() : message);
                mFirebaseAnalytics.logEvent("sign_in_failed", bundle);
                Toast.makeText(this, "Login failed.. " + result.getStatus().toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = getSignInClient();
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private GoogleSignInOptions getGso() {
        return new GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Games.SCOPE_GAMES_LITE)
            .requestEmail()
            .build();
    }

    private GoogleSignInClient getSignInClient() {
        return GoogleSignIn.getClient(this, getGso());
    }

    private void signInSilently() {

        getSignInClient().silentSignIn().addOnCompleteListener(this,
            new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    if (task.isSuccessful()) {
                        // The signed in account is stored in the task's result.
                        GoogleSignInAccount signedInAccount = task.getResult();
                        spawnStartScreenActivity(signedInAccount);
                    } else {
                        // Player will need to sign-in explicitly using via UI
                        if (((ApiException)task.getException()).getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                            startSignInIntent();
                        }
                    }
                }
            }
        );
    }
}
