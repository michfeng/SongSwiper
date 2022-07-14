package com.codepath.michfeng.songswiper.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.RunnableGenre;
import com.codepath.michfeng.songswiper.models.Artist;
import com.codepath.michfeng.songswiper.models.Card;
import com.codepath.michfeng.songswiper.models.Track;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.paging.Paging;
import spotify.models.playlists.PlaylistFull;
import spotify.models.playlists.PlaylistSimplified;
import spotify.models.playlists.requests.CreateUpdatePlaylistRequestBody;
import spotify.models.tracks.TrackSimplified;

public class LikedActivity extends AppCompatActivity {
    private TextView tvSong;
    private TextView tvArtist;
    private ImageView ivAlbum;
    private Button share;
    private Button add;
    private String accessToken;

    private static final String TAG = "LikedActivity";

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
        accessToken = getIntent().getStringExtra("accesstoken");
        Log.i(TAG, "accessToken: " + accessToken);

        Card card = (Card) Parcels.unwrap(getIntent().getParcelableExtra("card"));

        tvSong.setText(card.getTrackName());
        tvArtist.setText(card.getArtistName());

        if (card.getCoverImagePath() != null) {
            Glide.with(this).load(card.getCoverImagePath()).into(ivAlbum);
        }

        // Add liked song, genre, and artist to their respective artist.
        ParseUser currentUser = ParseUser.getCurrentUser();
        Queue<TrackSimplified> likedTracks = (LinkedList<TrackSimplified>) currentUser.get("likedTracks");
        Queue<ArtistFull> likedArtists = (LinkedList<ArtistFull>) currentUser.get("likedArtists");
        Queue<String> likedGenres = (LinkedList<String>) currentUser.get("likedGenres");

        likedTracks.add(card.getTrack());
        likedArtists.add(card.getTrack().getArtists().get(0));

        RunnableGenre runG = new RunnableGenre(new SpotifyApi(accessToken), card.getTrack().getId());
        Thread thread = new Thread(runG);
        thread.setName("runG");
        thread.start();
        try {
            likedGenres.addAll(runG.getGenres());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Handle click event to share liked song.
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Starting intent to ComposeActivity");
                Intent i = new Intent(LikedActivity.this, ComposeActivity.class);

                i.putExtra("song", card.getTrackName());
                i.putExtra("artist", card.getArtistName());
                i.putExtra("image",card.getCoverImagePath());
                i.putExtra("uri", card.getUri());
                i.putExtra("accessToken", accessToken);

                startActivity(i);
            }
        });

        // Handle event to add liked song to playlist.
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotifyApi spotifyApi = new SpotifyApi(accessToken);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Check if there is a playlist for SongSwiper
                        List<PlaylistSimplified> playlists = spotifyApi.getPlaylists(new HashMap<String, String>()).getItems();
                        List<String> names = new ArrayList<>();

                        for (PlaylistSimplified p : playlists)
                            names.add(p.getName());

                        // If playlist doesn't exist, make new playlist.
                        if (!names.contains("My Song Swiper Liked Tracks")) {
                            // Make body of playlist POST request with appropriate parameters.
                            String playlistName = "My Song Swiper Liked Tracks";
                            String playlistDescription = "auto-generated by Song Swiper";

                            CreateUpdatePlaylistRequestBody body = new CreateUpdatePlaylistRequestBody(
                                    playlistName, playlistDescription, true, false);

                            // Make POST request to API.
                            spotifyApi.createPlaylist(ParseUser.getCurrentUser().getUsername(), body);
                            Log.i(TAG, "added playlist");

                            // Re-get the playlists because this wrapper doesn't allow us to get the id of the new playlist.
                            playlists = spotifyApi.getPlaylists(new HashMap<String, String>()).getItems();
                        }

                        for (PlaylistSimplified p : playlists) {
                            if (p.getName().equals("My Song Swiper Liked Tracks")) {
                                // Add song to beginning of Song Swiper playlist.
                                String id = p.getId();
                                List<String> track = new ArrayList<>();
                                track.add(card.getUri());

                                spotifyApi.addItemsToPlaylist(track, id, 0);
                                Log.i(TAG, "added " + card.getTrackName());
                                Toast.makeText(LikedActivity.this, "Song successfully added to playlist!", Toast.LENGTH_LONG).show();

                            }
                        }
                    }
                });

                thread.start();

                // Redirect back to swiping.
                Intent intent = new Intent(LikedActivity.this, MainActivity.class);
                Log.i(TAG, "access token: " + getIntent().getStringExtra("accessToken"));
                intent.putExtra("accessToken", getIntent().getStringExtra("accessToken"));
                startActivity(intent);
            }
        });
    }
}