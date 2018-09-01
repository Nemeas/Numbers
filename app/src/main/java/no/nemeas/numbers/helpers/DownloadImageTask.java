package no.nemeas.numbers.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    CircleImageView bmImage;

    private Listener mListener;

    public DownloadImageTask setListener(Listener listener) {
        mListener = listener;
        return this;
    }

    public interface Listener {
        void onProfileImageSetComplete();
    }

    public DownloadImageTask(CircleImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
        mListener.onProfileImageSetComplete();
    }
}