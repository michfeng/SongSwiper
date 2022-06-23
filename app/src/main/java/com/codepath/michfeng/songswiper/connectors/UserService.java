package com.codepath.michfeng.songswiper.connectors;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.codepath.michfeng.songswiper.models.User;
import com.codepath.michfeng.songswiper.models.VolleyCallBack;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class UserService {

    private static final String ENDPOINT = "https://api.spotify.com/v1/me";
    private SharedPreferences msharedPreferences;
    private RequestQueue mqueue;
    private User user;
    private static final String TAG = "UserService";

    public UserService(RequestQueue queue, SharedPreferences sharedPreferences) {
        mqueue = queue;
        msharedPreferences = sharedPreferences;
    }

    public User getUser() {
        return user;
    }

    public void get(final VolleyCallBack callBack) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(ENDPOINT, null, response -> {
            //"Gson is an open-source Java library to serialize and deserialize Java objects to JSON" from google
            // use Gson to parse JSON object
            Gson gson = new Gson ();
            user = gson.fromJson(response.toString(), User.class);
            callBack.onSuccess();
        }, error -> get (() -> {}))
        {
           @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
               Map<String, String> headers = new HashMap<>();

               String token = msharedPreferences.getString("token", "");
               String auth = "Bearer " + token;
               headers.put("Authorization", auth);
               return headers;
           }
        };
        mqueue.add(jsonObjectRequest);
    }
}