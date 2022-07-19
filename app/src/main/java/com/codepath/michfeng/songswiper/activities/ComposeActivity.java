package com.codepath.michfeng.songswiper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.fragments.FeedFragment;
import com.codepath.michfeng.songswiper.fragments.SwipeFragment;
import com.codepath.michfeng.songswiper.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ComposeActivity extends AppCompatActivity {

    EditText etCompose;
    Button post;
    private ParseUser currentUser;
    private String photoPath;
    private String uri;
    private String id;

    private final static String TAG = "ComposeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        etCompose = (EditText) findViewById(R.id.etCompose);
        post = (Button) findViewById(R.id.btnPost);
        currentUser = ParseUser.getCurrentUser();
        uri = getIntent().getStringExtra("uri");
        id = getIntent().getStringExtra("id");
        Log.i(TAG, uri);

        photoPath = getIntent().getStringExtra("image");

        // Set up listener for post button.
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etCompose.getText().toString();

                if (description.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save post through outer method.
                savePost(description, currentUser, photoPath, uri, id);
            }
        });
    }

    private void savePost(String description, ParseUser user, String photoUrl, String uri, String id)  {
        Post post = new Post();
        post.setCaption(description);
        post.setUser(user);
        post.setImage(photoUrl);
        post.setUri(uri);
        post.setId(id);
        Log.i(TAG, "likes " + post.getLikes());

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // Handle exception in publishing post.
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(ComposeActivity.this, "Error while saving!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Post save was successful");
                }

                // Redirect back to swiping.
                Intent intent = new Intent(ComposeActivity.this, MainActivity.class);
                Log.i(TAG, "access token: " + getIntent().getStringExtra("accessToken"));
                intent.putExtra("accessToken", getIntent().getStringExtra("accessToken"));
                startActivity(intent);
            }
        });
    }
}