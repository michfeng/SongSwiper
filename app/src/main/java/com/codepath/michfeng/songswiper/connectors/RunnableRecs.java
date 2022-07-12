package com.codepath.michfeng.songswiper.connectors;

import android.util.Log;

import androidx.viewpager2.widget.ViewPager2;

import com.codepath.michfeng.songswiper.models.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.tracks.TrackFull;
import spotify.models.tracks.TrackSimplified;

// Runnable-implemented class with accessor method to get recommendations from new Thread.
public class RunnableRecs implements Runnable {
    RecommendationCollection recs;
    SpotifyApi spotifyApi;
    private volatile boolean finish;

    private static final String TAG = "RunnableRecs";

    public RunnableRecs(SpotifyApi api) {
        this.spotifyApi = api;
    }

    @Override
    public void run() {
        try  {
            List<TrackFull> topTrackFull = spotifyApi.getTopTracks(new HashMap<>()).getItems();
            List<ArtistFull> topArtistFull = spotifyApi.getTopArtists(new HashMap<>()).getItems();

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


            // Making lists for specific seeds.
            List<String> seed_artists = (new ArrayList<>());
            seed_artists.add(topArtists.get(0));
            seed_artists.add(topArtists.get(1));

            List<String> seed_genres = (new ArrayList<>());
            seed_genres.add(topGenres.get(0));

            List<String> seed_tracks = (new ArrayList<>());
            seed_tracks.add(topTracks.get(0));

            Log.i(TAG,"seed tracks: " +seed_tracks.toString());
            Log.i(TAG,"seed genres: " +seed_genres.toString());
            Log.i(TAG,"seed artists: " +seed_artists.toString());


            recs = spotifyApi.getRecommendations(seed_artists, seed_genres, seed_tracks, new HashMap<String, String>());
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
}
