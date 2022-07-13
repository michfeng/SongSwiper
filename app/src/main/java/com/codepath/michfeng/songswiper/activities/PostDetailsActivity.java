package com.codepath.michfeng.songswiper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.models.Post;

public class PostDetailsActivity extends AppCompatActivity {

    private ImageView ivUser;
    private TextView tvUser;
    private ImageView ivAlbum;
    private TextView tvCaption;

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

        Post post = getIntent().getParcelableExtra("post");

        tvUser.setText(post.getUser().getUsername());
        tvCaption.setText(post.getCaption());

        Glide.with(this).load(post.getUser().getParseFile("profilePicture").getUrl()).circleCrop().into(ivUser);
        Glide.with(this).load(post.getImage()).into(ivAlbum);
    }
}