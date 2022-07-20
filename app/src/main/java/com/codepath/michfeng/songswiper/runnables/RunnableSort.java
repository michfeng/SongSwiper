package com.codepath.michfeng.songswiper.runnables;

import android.util.Log;

import com.codepath.michfeng.songswiper.models.Post;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import spotify.api.spotify.SpotifyApi;
import spotify.models.audio.AudioFeatures;
import spotify.models.audio.AudioFeaturesCollection;
import spotify.models.paging.Paging;
import spotify.models.playlists.PlaylistSimplified;
import spotify.models.playlists.PlaylistTrack;
import spotify.models.tracks.TrackFull;

public class RunnableSort implements Runnable {
    List<PlaylistTrack> playlist;
    SpotifyApi spotifyApi;
    List<Post> posts;
    List<Post> sorted;

    private volatile boolean finish;
    private static final String TAG = "RunnableSort";

    public RunnableSort(SpotifyApi api, List<Post> posts) {
        Log.i(TAG, "making posts with size " + posts.size());
        this.posts = posts;
        this.spotifyApi = api;
    }

    @Override
    public void run() {

        Log.i(TAG, "posts size: " + posts.size());

        List<String> ids = new ArrayList<>();
        for (Post p : posts) {
            ids.add(p.getId());
        }

        // Limit number of top items to 5.
        Map<String, String> options = new HashMap<>();
        options.put("limit", "5");

        // Get top Tracks
        List<TrackFull> topTracks = spotifyApi.getTopTracks(options).getItems();

        List<String> topIds = new ArrayList<>();
        for (TrackFull t : topTracks) {
            topIds.add(t.getId());
        }

        Log.i(TAG, "top IDs: " + topIds.toString());
        Log.i(TAG, "IDs: " + ids.toString());

        // Gets features of respective tracks.
        List<AudioFeatures> postsFeatures = spotifyApi.getTracksAudioFeatures(ids).getAudioFeatures();
        List<AudioFeatures> topFeatures = spotifyApi.getTracksAudioFeatures(topIds).getAudioFeatures();

        Log.i(TAG, "postsFeatures: " + postsFeatures.toString());
        Log.i(TAG, "topFeatures: " + topFeatures.toString());

        // Returns average audio features of user's top tracks.
        AudioFeatures average = getAverage(topFeatures);

        // Stores the score for each post track.
        Map<Float, Post> scores = new TreeMap<Float, Post>();

        Log.i(TAG, "posts size: " + posts.size());
        Log.i(TAG, "postFeatures size: " + postsFeatures.size());

        for (int i = 0; i < postsFeatures.size(); i++) {
            if (postsFeatures.get(i) != null) {
                float score = getScore(postsFeatures.get(i), average);
                Log.i(TAG, "Post " + posts.get(i).getCaption() + " score: " + score);
                scores.put(getScore(postsFeatures.get(i), average), posts.get(i));
            }
        }

        for (Map.Entry<Float, Post> e : scores.entrySet()) {
            Log.i(TAG, "" + e.getKey() + " " + e.getValue().getCaption());
        }

        sorted = new ArrayList<>(scores.values());
        Log.i(TAG, sorted.toString());

        for (Post p : sorted) {
            Log.i(TAG, p.getCaption());
        }

        finish = true;

        synchronized (this) {
            this.notify();
        }
    }

    public List<Post> getSorted() throws InterruptedException {

        synchronized (this) {
            if (!finish)
                this.wait();
        }

        return sorted;
    }

    // Averages features of top tracks
    private AudioFeatures getAverage(List<AudioFeatures> topFeatures) {
        AudioFeatures ret = new AudioFeatures();

        // Traits that Spotify analyzes.
        ret.setAcousticness(0);
        ret.setDanceability(0);
        ret.setEnergy(0);
        ret.setInstrumentalness(0);
        ret.setLiveness(0);
        ret.setLoudness(0);
        ret.setSpeechiness(0);
        ret.setValence(0);

        for (AudioFeatures f : topFeatures) {
            ret.setAcousticness(ret.getAcousticness() + f.getAcousticness());
            ret.setDanceability(ret.getDanceability() + f.getDanceability());
            ret.setEnergy(ret.getEnergy() + f.getEnergy());
            ret.setInstrumentalness(ret.getInstrumentalness() + f.getInstrumentalness());
            ret.setLiveness(ret.getLiveness() + f.getLiveness());
            ret.setLoudness(ret.getLoudness() + f.getLoudness());
            ret.setValence(ret.getValence() + f.getValence());
        }

        int numFeatures = topFeatures.size();
        ret.setAcousticness(ret.getAcousticness() / numFeatures );
        ret.setDanceability(ret.getDanceability() / numFeatures );
        ret.setEnergy(ret.getEnergy() / numFeatures );
        ret.setInstrumentalness(ret.getInstrumentalness() / numFeatures );
        ret.setLiveness(ret.getLiveness() / numFeatures );
        ret.setSpeechiness(ret.getSpeechiness() / numFeatures );
        ret.setValence(ret.getValence() / numFeatures );

        return ret;
    }

    private Float getScore(AudioFeatures trackAnalysis, AudioFeatures average) {
        float score = 0;

        // Weights of features in score in order of: acousticness, danceability, energy,
        // instrumentalness, liveness, speechiness, valence
        float [] weights = new float []{1, 1, (float) 1.2, 1, (float) 0.8, (float) 0.7, (float) 0.6};

        score += (weights[0]) * (Math.abs(trackAnalysis.getAcousticness() - average.getAcousticness()));
        score += (weights[1]) * (Math.abs(trackAnalysis.getDanceability() - average.getDanceability()));
        score += (weights[2]) * (Math.abs(trackAnalysis.getEnergy() - average.getEnergy()));
        score += (weights[3]) * (Math.abs(trackAnalysis.getInstrumentalness() - average.getInstrumentalness()));
        score += (weights[4]) * (Math.abs(trackAnalysis.getLiveness() - average.getLiveness()));
        score += (weights[5]) * (Math.abs(trackAnalysis.getSpeechiness() - average.getSpeechiness()));
        score += (weights[6]) * (Math.abs(trackAnalysis.getValence() - average.getValence()));

        // The lower the score, the closer the track is to user's taste.
        return score;
    }
}
