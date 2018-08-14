package no.nemeas.numbers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SignInActivity extends Activity {

    private int RC_SIGN_IN = 13123235;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isSignedIn())
            spawnStartScreenActivity(GoogleSignIn.getLastSignedInAccount(this));
        else
            startSignInIntent();

        setContentView(R.layout.sign_in);
    }

    private void spawnStartScreenActivity(GoogleSignInAccount account) {
        Intent i = new Intent(this, StartScreenActivity.class);
        i.putExtra("account", account);
        startActivity(i);
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
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    Log.d(GameActivity.DEBUG, "failed to signin..");
                    message = "something went wrong..";
                }
                new AlertDialog.Builder(this).setMessage(message).setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
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
            });
    }
}
