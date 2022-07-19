package com.codepath.michfeng.songswiper.runnables;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.artists.ArtistSimplified;
import spotify.models.paging.Paging;
import spotify.models.playlists.PlaylistSimplified;
import spotify.models.playlists.PlaylistTrack;
import spotify.models.tracks.TrackFull;

public class RunnableLiked implements Runnable{
    List<String> genres;
    SpotifyApi spotifyApi;
    String id;
    TrackFull track;
    List<ArtistSimplified> artists;

    private volatile boolean finish;
    private static final String TAG = "RunnableLiked";

    public RunnableLiked(SpotifyApi api, String id) {
        this.id = id;
        this.spotifyApi = api;
    }

    @Override
    public void run() {

        genres = new ArrayList<String>();
        track = spotifyApi.getTrack(id, new HashMap<>());
        for (ArtistSimplified artistSimplified : track.getArtists()) {
            ArtistFull artist = spotifyApi.getArtist(artistSimplified.getId());
            genres.addAll(artist.getGenres());
        }

        artists = track.getArtists();
        finish = true;

        synchronized (this) {
            this.notify();
        }
    }
    public List<String> getGenres() throws InterruptedException {

        synchronized (this) {
            if (!finish)
                this.wait();
        }

        return genres;
    }

    public TrackFull getTrack() throws InterruptedException {
        synchronized (this) {
            if (!finish)
                this.wait();
        }

        return track;
    }

    public List<ArtistSimplified> getArtists() throws InterruptedException {
        synchronized (this) {
            if (!finish)
                this.wait();
        }

        return artists;
    }
}