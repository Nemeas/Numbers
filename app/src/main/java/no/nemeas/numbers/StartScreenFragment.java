package no.nemeas.numbers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;

import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartScreenFragment extends Fragment {
    private int lvl = 1;

    interface Listener {
        void onShowLeaderBoard();
        void onSignOut();
        void onSignIn();
        void onPlay(int lvl);
    }

    private Listener mListener;
    private View mView;
    private ImageView mLeaderBoardButton;
    private Button mPlayButton;
    private ImageView mSplashImage;
    private CircleImageView mProfileImage;
    private Button mSignInButton;

    public StartScreenFragment setListener(Listener listener) {
        this.mListener = listener;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.startscreen, container, false);

        // cache views
        mLeaderBoardButton = mView.findViewById(R.id.show_leaderboard_button);
        mPlayButton = mView.findViewById(R.id.play_button);
        mSplashImage = mView.findViewById(R.id.splash);
        mProfileImage = mView.findViewById(R.id.profile_image);
        mSignInButton = mView.findViewById(R.id.sign_in_button);

        mLeaderBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLeaderBoard();
            }
        });
        mSplashImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lvl += 1;
            }
        });
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSignIn();
            }
        });
        animateImage(mLeaderBoardButton);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        return mView;
    }

    public void showLogin() {
        mSignInButton.setVisibility(View.VISIBLE);
        mProfileImage.setVisibility(View.INVISIBLE);
        mLeaderBoardButton.setVisibility(View.INVISIBLE);
    }

    public void hideLogin() {
        mSignInButton.setVisibility(View.INVISIBLE);
        mProfileImage.setVisibility(View.VISIBLE);
        mLeaderBoardButton.setVisibility(View.VISIBLE);
    }

    public void setUserImage(String url) {
        new DownloadImageTask(mProfileImage).execute(url);
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

    private void play() {
        mListener.onPlay(this.lvl);
    }

    private void showLeaderBoard() {
        this.mListener.onShowLeaderBoard();
    }

    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (GoogleSignIn.getLastSignedInAccount(mView.getContext()) == null) {
            this.mListener.onSignOut();
        }
    }
}
