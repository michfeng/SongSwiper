package com.codepath.michfeng.songswiper.connectors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codepath.michfeng.songswiper.models.Track;
import com.codepath.michfeng.songswiper.models.VolleyCallBack;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TopTrackService {
    private ArrayList<Track> topTracks;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;
    public static final String TAG = "TrackService";

    public TopTrackService(Context context) {
        sharedPreferences = context.getSharedPreferences("SPOTIFY",0);
        requestQueue = Volley.newRequestQueue(context);
    }

    public ArrayList<Track> getTracks() {
        return topTracks;
    }


    // Returns the SpotifyUser's top Tracks.
    public ArrayList<Track> getTopTracks(final VolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/me/top/tracks";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,endpoint,null, response -> {
            Gson gson = new Gson();
            JSONArray jsonArray = response.optJSONArray("items");

            // Take each track from the array returned and add it to local topTracks.
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject object = jsonArray.getJSONObject(i);
                    object = object.optJSONObject("track");
                    Track song = gson.fromJson(object.toString(), Track.class);
                    topTracks.add(song);
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
        return topTracks;
    }


}
