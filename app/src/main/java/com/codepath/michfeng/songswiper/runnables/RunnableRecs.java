package com.codepath.michfeng.songswiper.runnables;

import android.content.Context;
import android.util.Log;

import com.codepath.michfeng.songswiper.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.saksham.customloadingdialog.LoaderKt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.artists.ArtistSimplified;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.tracks.TrackFull;
import spotify.models.tracks.TrackSimplified;

// Runnable-implemented class with accessor method to get recommendations from new Thread.
public class RunnableRecs implements Runnable {
    RecommendationCollection recs;
    SpotifyApi spotifyApi;
    private volatile boolean finish;
    Context context;
    ParseUser user;

    private static final String TAG = "RunnableRecs";

    public RunnableRecs(SpotifyApi api, Context context, ParseUser user) {
        this.spotifyApi = api;
        this.context = context;
        this.user = user;
    }

    @Override
    public void run() {
        try  {
            ArrayList<String>[] seeds = new ArrayList[3];
            seeds[0] = new ArrayList<String>();
            seeds[1] = new ArrayList<String>();
            seeds[2] = new ArrayList<String>();

            Log.i(TAG, "checkpoint 1");

            ParseUser currentUser = ParseUser.getCurrentUser();

            final Queue<TrackFull>[] likedTracksQ = new Queue[]{new LinkedList<TrackFull>()};
            final Queue<ArtistSimplified>[] likedArtistsQ = new Queue[]{new LinkedList<ArtistSimplified>()};
            final Queue<String>[] likedGenresQ = new Queue[]{new LinkedList<String>()};


            ParseQuery<ParseObject> query = ParseQuery.getQuery("LikedObjects");
            query.whereEqualTo("user", currentUser.getObjectId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        // No exception, so we have successfully found corresponding LikedObjects for user.
                        // Note that there should only be one row corresponding to each user, so we only
                        // need to retrieve the first out of this returned list.
                        ParseObject obj = objects.get(0);

                        likedTracksQ[0] = (LinkedList<TrackFull>) currentUser.get("likedTracks");
                        likedArtistsQ[0] = (LinkedList<ArtistSimplified>) currentUser.get("likedArtists");
                        likedGenresQ[0] = (LinkedList<String>) currentUser.get("likedGenres");

                        Log.i(TAG, "retrieved liked objects");
                    } else {
                        Log.e(TAG, "error in query: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            Queue<TrackFull> likedTracks = likedTracksQ[0];
            Queue<ArtistSimplified> likedArtists = likedArtistsQ[0];
            Queue<String> likedGenres = likedGenresQ[0];

            List<TrackFull> topTrackFull = spotifyApi.getTopTracks(new HashMap<>()).getItems();
            List<ArtistFull> topArtistFull = spotifyApi.getTopArtists(new HashMap<>()).getItems();


            Log.i(TAG, "checkpoint 2");

            List<String> topGenres = new ArrayList<>();
            List<String> topTracks = new ArrayList<>();
            List<String> topArtists = new ArrayList<>();

            // At the moment, having all top seeds is not necessary since we are only using
            // one of each (recommendation API takes 5 total seeds maximum), but I am storing
            // them in case I would like to make more recommendations in the future.
            for (TrackFull t : topTrackFull) topTracks.add(t.getId());
            for (ArtistFull a : topArtistFull) {
                topGenres.addAll(a.getGenres());
                topArtists.add(a.getId());
            }


            // Get seeds.
            if (likedTracks.isEmpty() || likedArtists.isEmpty()) {
                Log.i(TAG, "checkpoint 3");
                seeds[0].add(topArtists.get(0));
                seeds[1].add(topGenres.get(0));
                seeds[1].add(topGenres.get(1));
                seeds[2].add(topTracks.get(0));
                seeds[2].add(topTracks.get(1));
            }
            else {
                Log.i(TAG, "checkpoint 4");
                seeds = getSeeds(topArtists, topGenres, topTracks, likedArtists, likedGenres, likedTracks);
            }

            Log.i(TAG, "checkpoint");
            Log.i(TAG,"seed tracks: " +seeds[2].toString());
            Log.i(TAG,"seed genres: " +seeds[1].toString());
            Log.i(TAG,"seed artists: " +seeds[0].toString());

            recs = spotifyApi.getRecommendations(seeds[0], seeds[1], seeds[2], new HashMap<String, String>());
            Log.i(TAG,"recommended tracks: "+ recs.getTracks().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        finish = true;

        synchronized (this) {
            this.notify();
        }
    }

    public RecommendationCollection getRecs() throws InterruptedException {

        synchronized (this) {
            if (!finish)
                this.wait();
        }

        return recs;
    }

    private ArrayList<String>[] getSeeds(List<String> topArtists, List<String> topGenres, List<String> topTracks,
                                         Queue<ArtistSimplified> likedArtists, Queue<String> likedGenres,
                                         Queue<TrackFull> likedTracks) {

        ArrayList<String> [] seeds = new ArrayList[3];
        seeds[0] = new ArrayList<String>();
        seeds[1] = new ArrayList<String>();
        seeds[2] = new ArrayList<String>();


        // Generate random integer between [0,2] to decide which field gets one seed (other two each get two seeds).
        int randSeed = (int) (Math.random() * 3);

        // If any fields are empty, we use the top field in their place.


        // Generate random integer in [0,1] to decide whether that one seed is a liked or top object.
        int randType = (int) (Math.random() * 2);

        // Decides which field gets one seed (other two each get two seeds).
        switch (randSeed) {
            // Artists get one seed
            case 0:
                if (randType == 0)
                    seeds[0].add(topArtists.get(0));
                else
                    seeds[0].add(likedArtists.remove().getId());

                seeds[1].add(likedGenres.remove());
                seeds[1].add(topGenres.get(0));

                seeds[2].add(likedTracks.remove().getId());
                seeds[2].add(topTracks.get(0));
                break;

            // Genres get one seed
            case 1:
                if (randType == 0)
                    seeds[1].add(topGenres.get(0));
                else
                    seeds[1].add(likedGenres.remove());

                seeds[0].add(likedTracks.remove().getId());
                seeds[0].add(topTracks.get(0));

                seeds[2].add(likedTracks.remove().getId());
                seeds[2].add(topTracks.get(0));

                break;

            // Tracks get one seed
            case 2:
                if (randType == 0)
                    seeds[2].add(topTracks.get(0));
                else
                    seeds[2].add(likedTracks.remove().getId());

                seeds[0].add(likedArtists.remove().getId());
                seeds[0].add(topArtists.get(0));

                seeds[1].add(likedGenres.remove());
                seeds[1].add(topGenres.get(0));

                break;

            default:    // Unreachable
                break;
        }

        // Update liked fields in Parse database.
        user.put("likedTracks", likedTracks);
        user.put("likedArtists", likedArtists);
        user.put("likedGenres", likedGenres);
        user.saveInBackground();


        return seeds;
    }
}
