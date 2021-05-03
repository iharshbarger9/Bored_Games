package com.example.boredgames;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class ProfileActivity extends AppCompatActivity {

    int RESULT_LOAD_IMAGE = 0;
    private String pfpPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Log.e("onCreate()", "method called");

        setContentView(R.layout.activity_profile);

        // Set Profile Picture image view's long press activity: to choose a new profile picture
        ImageView iv_profile_display_picture = (ImageView) findViewById(R.id.iv_profile_display_picture);
        iv_profile_display_picture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_GET_CONTENT , android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                return true;
            }
        });

        // Set "Set Profile" button's click activity: save display name and pfp's Uri string to the room DB
        Button bt_profile_set_profile = (Button) findViewById(R.id.bt_profile_set_profile);
        bt_profile_set_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get display name
                EditText et_profile_display_name = (EditText) findViewById(R.id.et_profile_display_name);
                String displayName = et_profile_display_name.getText().toString();

                if (displayName.length()==0){
                    //EditText is empty
                    Toast.makeText(getApplicationContext(), R.string.profile_toast_enter_display_name,Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get phone's unique ID
                String androidID = Settings.Secure.getString(ProfileActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);

                // Store display name and any pfp Uri string in DB
                Profile profile = new Profile(androidID, displayName, pfpPath);
                AppDatabase db = AppDatabase.getDatabase(getApplication());

                AppDatabase.setProfile(profile);

                // Check if profile already exists

                /*AppDatabase.getProfile(androidID, prof -> {
                    if (prof != null) {
                        // This profile already exists.

                    }
                });*/

            }
        });


        AppDatabase db = AppDatabase.getDatabase(getApplication());

        // Check if a profile already exists for this phone. If so, apply those saved value to display name editText and pfp ImageView
        AppDatabase.getProfile(/*Settings.Secure.getString(ProfileActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID),*/ prof -> {
            if (prof != null) {
                // This profile already exists.

                // Set editText's text
                EditText et_profile_display_name = (EditText) findViewById(R.id.et_profile_display_name);
                et_profile_display_name.setText(prof.getDisplayName());


                //Toast.makeText(getApplicationContext(), prof.getPfpPath(), Toast.LENGTH_SHORT).show();
                // Set imageView's image
             //  iv_profile_display_picture.setImageURI(Uri.fromFile(new File(prof.getPfpPath())));    //     THIS DOESN'T WORK
               // iv_profile_display_picture.setImageURI(Uri.parse(new File(prof.getPfpPath()).toString()));

              /*  File imgFile;
                try {imgFile = new  File(new URI(prof.getPfpPath()));

                    if(imgFile.exists()){

                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                        iv_profile_display_picture.setImageBitmap(myBitmap);

                    }
                }
                catch (URISyntaxException e) {} */



            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            // Save Uri path
            pfpPath = selectedImage.getPath();
            //pfpPath = selectedImage.getEncodedPath();
            //pfpPath = selectedImage.getLastPathSegment();
            //pfpPath = selectedImage.getPathSegments()

            // Set pfp image view to be the selected image
            ImageView iv_profile_display_picture = (ImageView) findViewById(R.id.iv_profile_display_picture);
            iv_profile_display_picture.setImageURI(selectedImage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("onResume()", "method called");

        AppDatabase db = AppDatabase.getDatabase(getApplication());

        // Check if a profile already exists for this phone. If so, apply those saved value to display name editText and pfp ImageView
        AppDatabase.getProfile(/*Settings.Secure.getString(ProfileActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID),*/ prof -> {
            if (prof != null) {
                // This profile already exists.

                // Set editText's text
                EditText et_profile_display_name = (EditText) findViewById(R.id.et_profile_display_name);
                et_profile_display_name.setText(prof.getDisplayName());
                Toast.makeText(getApplicationContext(), et_profile_display_name.getText().toString(),Toast.LENGTH_SHORT).show();

                //Toast.makeText(getApplicationContext(), prof.getPfpPath(), Toast.LENGTH_SHORT).show();
                // Set imageView's image
                //  iv_profile_display_picture.setImageURI(Uri.fromFile(new File(prof.getPfpPath())));    //     THIS DOESN'T WORK
                // iv_profile_display_picture.setImageURI(Uri.parse(new File(prof.getPfpPath()).toString()));

              /*  File imgFile;
                try {imgFile = new  File(new URI(prof.getPfpPath()));

                    if(imgFile.exists()){

                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                        iv_profile_display_picture.setImageBitmap(myBitmap);

                    }
                }
                catch (URISyntaxException e) {} */



            } else {Log.e("null profile", "null profile");}
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.e("onRestart()", "method called");

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e("onStart()", "method called");

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("myDisplayName", ((EditText) findViewById(R.id.et_profile_display_name)).getText().toString());
        // etc.
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String myDisplayName = savedInstanceState.getString("myDisplayName");
        EditText et_profile_display_name = (EditText) findViewById(R.id.et_profile_display_name);
        et_profile_display_name.setText(myDisplayName);
    }

}