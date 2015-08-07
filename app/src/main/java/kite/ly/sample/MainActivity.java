package kite.ly.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;

import ly.kite.instagramphotopicker.InstagramPhoto;
import ly.kite.instagramphotopicker.InstagramPhotoPicker;


public class MainActivity extends ActionBarActivity {

    private static final String CLIENT_ID = "aa314a392fdd4de7aa287a6614ea8897";
    private static final String REDIRECT_URI = "psapp://instagram-callback";
    private static final int REQUEST_CODE_INSTAGRAM_PICKER = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonLaunchInstagramPickerClicked(View view) {
        InstagramPhotoPicker.startPhotoPickerForResult(this, CLIENT_ID, REDIRECT_URI, REQUEST_CODE_INSTAGRAM_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_INSTAGRAM_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                Parcelable[] photos = data.getParcelableArrayExtra(InstagramPhotoPicker.EXTRA_SELECTED_PHOTOS);
                InstagramPhoto[] instagramPhotos = new InstagramPhoto[photos.length];
                System.arraycopy(photos, 0, instagramPhotos, 0, photos.length);

                Toast.makeText(this, "User selected " + instagramPhotos.length + " Instagram photos", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < instagramPhotos.length; ++i) {
                    Log.i("dbotha", "Photo: " + instagramPhotos[i].getFullURL());
                }

             } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Instagram Picking Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("dbotha", "Unknown result code: " + resultCode);
            }
        }
    }
}
