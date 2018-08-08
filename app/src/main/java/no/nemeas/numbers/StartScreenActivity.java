package no.nemeas.numbers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartScreenActivity extends Activity {

    private ImageView splashImage;
    private int lvl = 1;

    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.startscreen);

        this.splashImage = (ImageView) findViewById(R.id.splash);

        this.splashImage.setOnClickListener(new View.OnClickListener() {
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
}
