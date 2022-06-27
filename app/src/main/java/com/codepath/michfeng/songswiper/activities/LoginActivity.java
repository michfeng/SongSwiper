package com.codepath.michfeng.songswiper.activities;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.UserService;
import com.codepath.michfeng.songswiper.models.SpotifyUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;


public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private static final String CLIENT_ID = "7d2e57d7cf85444da0db0412f4116c80";
    private static final String REDIRECT_URI = "com.codepath.michfeng.songswiper://callback/";
    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played,playlist-read-private,user-modify-playback-state,app-remote-control,user-top-read,user-library-modify,user-follow-read,user-read-private";
    //  may need to adjust scopes ^ ***

    private Button btnAuthenticate;

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        btnAuthenticate = (Button) findViewById(R.id.btnAuthenticate);
        btnAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateSpotify();
            }
        });

        // initialize shared preferences
        msharedPreferences = this.getSharedPreferences("SPOTIFY",0);
        queue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Checks to see if response is from the correct activity.
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            Log.i(TAG,"request code approved");

            switch (response.getType()) {
                // The response is successful, and has produced an authorization token.
                case TOKEN:
                    Log.i(TAG, "Successful response!");
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    editor.apply();
                    waitForUserInfo();
                    break;

                // There is an error in log in attempt.
                case ERROR:
                    Log.e(TAG, "Error logging in");
                    break;

                // Most likely authentication has been cancelled.
                default:
                    Log.e(TAG, "Cancelled during authentication: "+ response.getType().toString());
            }
        }
    }


    private void authenticateSpotify() {
        // Open AuthorizationRequest using CLIENT_ID, note that request type is an authentication token.
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        // Set requested scopes (permissions to request from user).
        builder.setScopes(new String[] {SCOPES});

        // Send request.
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
        //AuthorizationClient.openLoginInBrowser(this, request);
        Log.i(TAG,"openLoginActivity finished");
        //startMainActivity("0");
    }

    private void waitForUserInfo() {
        UserService userService = new UserService(queue, msharedPreferences);
        userService.get( () -> {
            SpotifyUser user = userService.getUser();
            editor = getSharedPreferences("SPOTIFY",0).edit();
            editor.putString("userid",user.id);

            Log.d("STARTING", "Retrieved user information");
            editor.commit();
            startMainActivity(user.id);
        });

    }

    private void startMainActivity(String id) {
        // Checks Parse server if user is not in system
        //if (ParseUser.)
        /*try {
            Log.i(TAG, "Attempting to sign up user " + id);

            ParseUser newUser = new ParseUser();
            newUser.setUsername(id);
            newUser.setPassword("password");

            newUser.signUp();
        } catch (ParseException e) {
            if (e == ParseException.USERNAME_TAKEN)
        }
        try {
            ParseUser.s
            ParseUser.sign(id,"password", new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == Does)
                }
            });
        }
            // sign user up

        // log in user
*/


        // Goes to SwipeActivity once authentication is done.
        Intent i = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(i);
    }

    private void signUp(String id) {
        Log.i(TAG, "Attempting to sign up user " + id);

        ParseUser newUser = new ParseUser();
        newUser.setUsername(id);
        newUser.setPassword("password");

        newUser.signUpInBackground();
    }
}