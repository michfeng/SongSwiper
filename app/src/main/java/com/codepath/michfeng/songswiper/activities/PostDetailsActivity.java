package com.codepath.michfeng.songswiper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.models.Post;

import java.util.ArrayList;

import spotify.api.spotify.SpotifyApi;
import spotify.models.players.requests.ChangePlaybackStateRequestBody;

public class PostDetailsActivity extends AppCompatActivity {

    private ImageView ivUser;
    private TextView tvUser;
    private ImageView ivAlbum;
    private TextView tvCaption;
    private TextView date;
    private ImageView btnPlay;

    private static final String TAG = "PostDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Log.i(TAG, "I made it here");

        ivUser = findViewById(R.id.ivDetailsUser);
        tvUser = findViewById(R.id.tvDetailUsername);
        ivAlbum = findViewById(R.id.ivDetailsAlbum);
        tvCaption = findViewById(R.id.tvDetailsCaption);
        date = findViewById(R.id.date);
        btnPlay = findViewById(R.id.ivPlayDetails);

        Post post = getIntent().getParcelableExtra("post");

        tvUser.setText(post.getUser().getUsername());
        tvCaption.setText(post.getCaption());
        date.setText(Post.calculateTimeAgo(post.getCreatedAt()));

        Glide.with(this).load(post.getUser().getParseFile("profilePicture").getUrl()).circleCrop().into(ivUser);
        Glide.with(this).load(post.getImage()).into(ivAlbum);

        // Handle click for play button.
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String accessToken = getIntent().getStringExtra("accessToken");
                        Log.i(TAG, "accessToken: " + accessToken);

                        SpotifyApi api = new SpotifyApi(accessToken);

                        // Context to play in (can be playlist/album/artist). Here we want the album of the track.
                        ArrayList<String> uris = new ArrayList<>();
                        Log.i(TAG, "uri: " + post.getUri());
                        Log.i(TAG, "uri size" + uris.size());
                        uris.add(post.getUri());

                        ChangePlaybackStateRequestBody body = new ChangePlaybackStateRequestBody();
                        body.setUris(uris);

                        api.changePlaybackState(body);
                    }
                });
                thread.start();
            }
        });
    }
}