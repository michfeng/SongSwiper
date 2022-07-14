package com.codepath.michfeng.songswiper.connectors;

import android.util.Log;
import android.widget.Toast;

import com.codepath.michfeng.songswiper.activities.LikedActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.paging.Paging;
import spotify.models.playlists.PlaylistSimplified;
import spotify.models.playlists.PlaylistTrack;

public class RunnablePlaylist implements Runnable {
    List<PlaylistTrack> playlist;
    SpotifyApi spotifyApi;

    private volatile boolean finish;
    private static final String TAG = "RunnablePlaylist";

    public RunnablePlaylist(SpotifyApi api) {
        this.spotifyApi = api;
    }

    @Override
    public void run() {
        Log.i(TAG, "I am here");

        List<PlaylistSimplified> playlists = spotifyApi.getPlaylists(new HashMap<String, String>()).getItems();
        List<String> names = new ArrayList<>();
        String id = "";

        for (PlaylistSimplified p : playlists) {
            if (p.getName().equals("My Song Swiper Liked Tracks")) {
                Log.i(TAG, "Playlist exists!");
                id = p.getId();
            }
        }

        if (!id.isEmpty()) {
            Paging<PlaylistTrack> pagingTracks = spotifyApi.getPlaylistTracks(id, new HashMap<>());
            playlist = pagingTracks.getItems();
            Log.i(TAG, playlist.toString());
        }

        finish = true;

        synchronized (this) {
            this.notify();
        }
    }
    public List<PlaylistTrack> getPlaylist() throws InterruptedException {

        synchronized (this) {
            if (!finish)
                this.wait();
        }

        return playlist;
    }
}
