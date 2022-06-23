package com.codepath.michfeng.songswiper;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.net.Authenticator;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private static final String CLIENT_ID = "7d2e57d7cf85444da0db0412f4116c80";
    private static final String REDIRECT_URI = "com.spotify.michfeng.songswiper://callback";
    private static final int REQUEST_CODE = 1337; // what does this mean? ***
    private static final String SCOPES = "user-read-recently-played,playlist-read-private,user-modify-playback-state" +
            "app-remote-control,user-top-read,user-library-modify,user-follow-read, user-read-private";
    //  may need to adjust scopes ^ ***

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        authenticateSpotify();

        // initialize shared preferences
        msharedPreferences = this.getSharedPreferences("SPOTIFY",0);
        queue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // check if result is from correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // response successful, has auth token
                case TOKEN:
                    return;

                // errors
                case ERROR:
                    // handle error response
                    Log.e(TAG, "Error logging in");

                // cancelled
                default:
                    Log.e(TAG, "Cancelled during authentication");
            }
        }
    }


    private void authenticateSpotify() {
        // open AuthorizationRequest using CLIENT_ID, note request type is an authentication token
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        // set requested scopes (permissions to request from user)
        builder.setScopes(new String[] {SCOPES});

        // send request
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }
}