package com.codepath.michfeng.songswiper.activities;

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
import com.codepath.michfeng.songswiper.models.Track;
import com.codepath.michfeng.songswiper.runnables.RunnableLiked;
import com.codepath.michfeng.songswiper.models.Card;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.artists.ArtistSimplified;
import spotify.models.playlists.PlaylistSimplified;
import spotify.models.playlists.requests.CreateUpdatePlaylistRequestBody;
import spotify.models.tracks.TrackFull;
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

        // Add liked song, genre, and artist to their respective queues stored in user.
        // Query for LikedObjects object corresponding to user.
        ParseUser currentUser = ParseUser.getCurrentUser();

        /* final Queue<TrackFull>[] likedTracksQ = new Queue[]{new ArrayList<TrackFull>()};
        final Queue<ArtistSimplified>[] likedArtistsQ = new Queue[]{new ArrayList<ArtistSimplified>()};
        final Queue<String>[] likedGenresQ = new Queue[]{new ArrayList<String>()}; */

        final List<String>[] likedTracksQ = new List[]{new ArrayList<>()};
        final List<String>[] likedArtistsQ = new List[]{new ArrayList<>()};
        final List<String>[] likedGenresQ = new List[]{new ArrayList<>()};

        ParseQuery<ParseObject> query = ParseQuery.getQuery("LikedObjects");
        Log.i(TAG, "currenId" + currentUser.getString("likedObjectsId"));
        query.getInBackground(currentUser.getString("likedObjectsId"), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject obj, ParseException e) {
                if (e == null) {
                    // No exception, so we have successfully found corresponding LikedObjects for user.
                    // Note that there should only be one row corresponding to each user, so we only
                    // need to retrieve the first out of this returned list.

                    likedTracksQ[0] = (ArrayList<String>) obj.get("likedTracks");
                    likedArtistsQ[0] = (ArrayList<String>) obj.get("likedArtists");
                    likedGenresQ[0] = (ArrayList<String>) obj.get("likedGenres");

                    Log.i(TAG, "objectid 1: " + obj.getObjectId());

                    List<String> likedTracks = likedTracksQ[0];
                    List<String> likedArtists = likedArtistsQ[0];
                    List<String> likedGenres = likedGenresQ[0];

                    Log.i(TAG, "liked tracks init " + likedTracks.toString());
                    Log.i(TAG, "liked artists init " + likedArtists.toString());
                    Log.i(TAG, "liked genres init " + likedGenres.toString());

                    RunnableLiked runG = new RunnableLiked(new SpotifyApi(accessToken), card.getId());
                    Thread thread = new Thread(runG);
                    thread.setName("runG");
                    thread.start();
                    try {
                        Log.i(TAG, "adding");
                        likedGenres.addAll(runG.getGenres());
                        likedTracks.add(runG.getTrack().getId());

                        for (ArtistSimplified a : runG.getArtists()) {
                            likedArtists.add(a.getId());
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    Log.i(TAG, "liked tracks after " + likedTracks.toString());
                    Log.i(TAG, "liked artists after " + likedArtists.toString());
                    Log.i(TAG, "liked genres after " + likedGenres.toString());

                    updateLikedObject(likedTracks, likedArtists, likedGenres, obj.getObjectId());
                } else {
                    Log.e(TAG, "error in query: " + e.getMessage());
                }
            }
        });

        // Record that current user has liked a new song for user statistics.
        currentUser.put("numLiked", currentUser.getInt("numLiked") + 1);
        currentUser.saveInBackground();

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
                i.putExtra("id", card.getId());

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
                                runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      Toast.makeText(LikedActivity.this, "Song successfully added to playlist!", Toast.LENGTH_LONG).show();
                                                  }
                                              }
                                );
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

    private void updateLikedObject(List<String> likedTracks, List<String> likedArtists,
                                   List<String> likedGenres, String objId) {

        // Make query to update LikedObjects object.
        ParseObject object = new ParseObject("LikedObjects");
        object.setObjectId(objId);
        object.put("likedTracks", likedTracks);
        object.put("likedArtists", likedArtists);
        object.put("likedGenres", likedGenres);
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error updating liked object: " + e.getMessage());
                } else {
                    Log.i(TAG, "Successfully updated object with id " + objId);
                }
            }
        });
    }
}