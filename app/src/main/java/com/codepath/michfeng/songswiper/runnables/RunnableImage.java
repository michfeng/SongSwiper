package com.codepath.michfeng.songswiper.runnables;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.generic.Image;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.tracks.TrackFull;

public class RunnableImage implements Runnable {
    String id;
    SpotifyApi spotifyApi;
    Image image;
    private volatile boolean finish;

    private static final String TAG = "RunnableRecs";

    public RunnableImage(SpotifyApi api, String id) {
        this.id = id;
        this.spotifyApi = api;
    }

    @Override
    public void run() {
        TrackFull track = spotifyApi.getTrack(id, new HashMap<>());
        image = track.getAlbum().getImages().get(0);

        finish = true;

        synchronized (this) {
            this.notify();
        }
    }
    public Image getImage() throws InterruptedException {

        synchronized (this) {
            if (!finish)
                this.wait();
        }

        return image;
    }
}


