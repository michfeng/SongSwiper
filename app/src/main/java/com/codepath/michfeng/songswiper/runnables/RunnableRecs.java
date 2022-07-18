package com.codepath.michfeng.songswiper.runnables;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.codepath.michfeng.songswiper.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
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
    List<String> likedTracks;
    List<String> likedArtists;
    List<String> likedGenres;


    private static final String TAG = "RunnableRecs";

    public RunnableRecs(SpotifyApi api, Context context, ParseUser user,
                        List<String> likedTracks, List<String> likedArtists, List<String> likedGenres) {
        this.spotifyApi = api;
        this.context = context;
        this.user = user;
        this.likedTracks = likedTracks;
        this.likedArtists = likedArtists;
        this.likedGenres = likedGenres;
    }

    @Override
    public void run() {
        ArrayList<String>[] seeds = new ArrayList[3];
        seeds[0] = new ArrayList<String>();
        seeds[1] = new ArrayList<String>();
        seeds[2] = new ArrayList<String>();

        Log.i(TAG, "checkpoint 1");

        //List<String> [] likedObjects = getLikedObjects(user);

        Log.i(TAG, "liked tracks  * " + likedTracks.toString());
        Log.i(TAG, "liked artists * " + likedArtists.toString());
        Log.i(TAG, "liked genres * " + likedGenres.toString());

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

        Log.i(TAG, "liked tracks" + likedTracks.toString());
        Log.i(TAG, "liked artists" + likedArtists.toString());
        Log.i(TAG, "liked genres" + likedGenres.toString());

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
        Log.i(TAG,"seed tracks: " + seeds[2].toString());
        Log.i(TAG,"seed genres: " + seeds[1].toString());
        Log.i(TAG,"seed artists: " + seeds[0].toString());

        recs = spotifyApi.getRecommendations(seeds[0], seeds[1], seeds[2], new HashMap<String, String>());

        while (recs == null) {
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

    // Generates seeds.
    private ArrayList<String>[] getSeeds(List<String> topArtists, List<String> topGenres, List<String> topTracks,
                                         List<String> likedArtists, List<String> likedGenres,
                                         List<String> likedTracks) {

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
                    seeds[0].add(likedArtists.remove(0));

                seeds[1].add(likedGenres.remove(0));
                seeds[1].add(topGenres.get(0));

                seeds[2].add(likedTracks.remove(0));
                seeds[2].add(topTracks.get(0));
                break;

            // Genres get one seed
            case 1:
                if (randType == 0)
                    seeds[1].add(topGenres.get(0));
                else
                    seeds[1].add(likedGenres.remove(0));

                seeds[0].add(likedTracks.remove(0));
                seeds[0].add(topTracks.get(0));

                seeds[2].add(likedTracks.remove(0));
                seeds[2].add(topTracks.get(0));

                break;

            // Tracks get one seed
            case 2:
                if (randType == 0)
                    seeds[2].add(topTracks.get(0));
                else
                    seeds[2].add(likedTracks.remove(0));

                seeds[0].add(likedArtists.remove(0));
                seeds[0].add(topArtists.get(0));

                seeds[1].add(likedGenres.remove(0));
                seeds[1].add(topGenres.get(0));

                break;

            default:    // Unreachable
                break;
        }

        // Update liked fields in Parse database.
        Log.i(TAG, "new liked tracks" + likedTracks.toString());
        Log.i(TAG, "new liked artists" + likedArtists.toString());
        Log.i(TAG, "new liked genres" + likedGenres.toString());
        updateLikedObject(likedTracks, likedArtists, likedGenres, user.getString("likedObjectsId"));

        return seeds;
    }

    // Retrieve liked objects from Parse database.
    private List<String>[] getLikedObjects(ParseUser currentUser) {
        List<String> [] ret = new List[]{new ArrayList<>()};

        final List<String>[] likedTracks = new List[]{new ArrayList<>()};
        final List<String>[] likedArtists = new List[]{new ArrayList<>()};
        final List<String>[] likedGenres = new List[]{new ArrayList<>()};

        ParseQuery<ParseObject> query = ParseQuery.getQuery("LikedObjects");
        Log.i(TAG, "currentId " + currentUser.getString("likedObjectsId"));
        query.getInBackground(currentUser.getString("likedObjectsId"), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject obj, ParseException e) {
                if (e == null) {
                    // No exception, so we have successfully found corresponding LikedObjects for user.
                    // Note that there should only be one row corresponding to each user, so we only
                    // need to retrieve the first out of this returned list.

                    Log.i(TAG, "checkpointttt");

                    likedTracks[0] = (ArrayList<String>) obj.get("likedTracks");
                    likedArtists[0] = (ArrayList<String>) obj.get("likedArtists");
                    likedGenres[0] = (ArrayList<String>) obj.get("likedGenres");

                    Log.i(TAG, "liked tracks  * " + likedTracks[0].toString());
                    Log.i(TAG, "liked artists * " + likedArtists[0].toString());
                    Log.i(TAG, "liked genres * " + likedGenres[0].toString());

                    Log.i(TAG, "objectid 1: " + obj.getObjectId());
                } else {
                    Log.e(TAG, "error in query: " + e.getMessage());
                }
            }
        });

        Log.i(TAG, "liked tracks  ** " + likedTracks[0].toString());
        Log.i(TAG, "liked artists ** " + likedArtists[0].toString());
        Log.i(TAG, "liked genres ** " + likedGenres[0].toString());

        return new List[]{likedTracks[0], likedArtists[0], likedGenres[0]};
    }

    private void updateLikedObject(List<String> likedTracks, List<String> likedArtists,
                                   List<String> likedGenres, String objId) {
        // Make query to update LikedObjects object.
        ParseObject object = new ParseObject("LikedObjects");
        object.setObjectId(objId);
        object.put("likedTracks", likedTracks);
        object.put("likedArtists", likedArtists);
        object.put("likedGenres", likedGenres);
        // Log.i(TAG, object.getObjectId());
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error updating liked object: " + e.getMessage() + ", " + e.getCause());
                    e.printStackTrace();
                } else {
                    Log.i(TAG, "Successfully updated object with id " + objId);
                }
            }
        });
    }
}
