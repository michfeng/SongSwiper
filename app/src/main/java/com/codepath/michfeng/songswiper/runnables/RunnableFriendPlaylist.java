package com.codepath.michfeng.songswiper.runnables;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.paging.Paging;
import spotify.models.playlists.PlaylistSimplified;
import spotify.models.playlists.PlaylistTrack;

public class RunnableFriendPlaylist implements Runnable {
    List<PlaylistTrack> playlist;
    SpotifyApi spotifyApi;
    String userId;

    private volatile boolean finish;
    private static final String TAG = "RunnablePlaylist";

    public RunnableFriendPlaylist(SpotifyApi api, String id) {
        this.userId = id;
        this.spotifyApi = api;
    }

    @Override
    public void run() {
        List<PlaylistSimplified> playlists = spotifyApi.getUserPlaylists(userId, new HashMap<String, String>()).getItems();
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

        if (playlist != null)
            return playlist;
        else
            return new ArrayList<>();
    }
}
