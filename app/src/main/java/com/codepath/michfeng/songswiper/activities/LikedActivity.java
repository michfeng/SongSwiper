package com.codepath.michfeng.songswiper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.models.Card;

import org.parceler.Parcels;

public class LikedActivity extends AppCompatActivity {
    TextView tvSong;
    TextView tvArtist;
    ImageView ivAlbum;
    Button share;
    Button add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked);

        // Initialize fields to their respective elements in layout.
        tvSong = (TextView) findViewById(R.id.tvSongLiked);
        tvArtist = (TextView) findViewById(R.id.tvArtistLiked);
        ivAlbum = (ImageView) findViewById(R.id.ivAlbumLiked);
        share = (Button) findViewById(R.id.btnShareLiked);
        add = (Button) findViewById(R.id.btnAddLiked);

        Card card = (Card) Parcels.unwrap(getIntent().getParcelableExtra("card"));

        tvSong.setText(card.getTrackName());
        tvArtist.setText(card.getArtistName());


        // Handle click event for buttons.
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LikedActivity.this, ComposeActivity.class);

                i.putExtra("song", card.getTrackName());
                i.putExtra("artist", card.getArtistName());

                startActivity(i);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}