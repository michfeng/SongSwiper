package com.codepath.michfeng.songswiper.connectors;

import static com.parse.Parse.getApplicationContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codepath.michfeng.songswiper.models.Artist;
import com.codepath.michfeng.songswiper.models.Track;
import com.codepath.michfeng.songswiper.models.VolleyCallBack;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationService {
    private ArrayList<Track> recommendations;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    public static final String TAG = "TrackService";

    private ArrayList<String> topArtists;
    private ArrayList<String> topTracks;
    private ArrayList<String> topGenres;

    private TopTrackService topTrackService;

    public RecommendationService(Context context) {
        sharedPreferences = context.getSharedPreferences("SPOTIFY",0);
        requestQueue = Volley.newRequestQueue(context);
    }

    public ArrayList<Track> getRecommendations() {
        return recommendations;
    }

    // Use TopTrackService to gather necessary seeds for recommendation endpoint.
    public void initSeeds () {
        topTrackService = new TopTrackService(getApplicationContext());

        topTrackService.getTopTracks(() -> {
            ArrayList<Track> tracklist = topTrackService.getTracks();

            this.topTracks = getIDs(tracklist);
            this.topArtists = getArtists(tracklist);
            this.topGenres = getGenres(tracklist);

            Log.i(TAG,"tracks: "+ topTracks.toString());
            Log.i(TAG,"artists: "+ topArtists.toString());
            Log.i(TAG,"genres: "+ topGenres.toString());
        });
    }

    // Get genres for a list of tracks.
    private ArrayList<String> getGenres(ArrayList<Track> tracklist) {
        ArrayList<String> ret = new ArrayList<>();

        for (Track track : tracklist){
            for (Artist a : track.getArtist()) {
                ret.addAll(Arrays.asList(a.getGenres()));
            }
        }

        return ret;
    }

    // Get Artist IDs for a list of tracks.
    private ArrayList<String> getArtists(ArrayList<Track> tracklist) {
        ArrayList<String> ret = new ArrayList<>();

        for (Track track : tracklist){
            for (Artist a : track.getArtist()) {
                ret.add(a.getId());
            }
        }

        return ret;
    }

    // Get Spotify IDs for a list of tracks.
    private ArrayList<String> getIDs(ArrayList<Track> tracks) {
        ArrayList<String> ret = new ArrayList<>();

        for (Track track : tracks)
            ret.add(track.getId());

        return ret;
    }


    // Returns the SpotifyUser's recommendations.
    public ArrayList<Track> get(final VolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/recommendations";

        initSeeds();

        // Manipulate seeds to fit Spotify requirements (size 5, String array).
        String [] seed_artist = topArtists.toArray(new String[topArtists.size()]);
        String [] seed_genres = topGenres.toArray(new String[topGenres.size()]);
        String [] seed_tracks = topTracks.toArray(new String[topTracks.size()]);

        Map<String, String []> params = new HashMap();
        params.put("seed_artists", Arrays.copyOfRange(seed_artist,0,5));
        params.put("seed_genres",  Arrays.copyOfRange(seed_genres,0,5));
        params.put("seed_tracks",  Arrays.copyOfRange(seed_tracks,0,5));

        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,endpoint,parameters, response -> {
            Gson gson = new Gson();
            JSONArray jsonArray = response.optJSONArray("items");

            // Take each track from the array returned and add it to local recommendations.
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject object = jsonArray.getJSONObject(i);
                    object = object.optJSONObject("track");
                    Track song = gson.fromJson(object.toString(), Track.class);
                    recommendations.add(song);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            callBack.onSuccess();
        }, error -> {
            // Handles error in getting data.
            Log.e(TAG,"Error getting top Tracks");
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
        return recommendations;
    }
}
