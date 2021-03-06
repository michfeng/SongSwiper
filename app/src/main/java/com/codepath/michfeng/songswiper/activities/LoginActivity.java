package com.codepath.michfeng.songswiper.activities;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.UserService;
import com.codepath.michfeng.songswiper.models.SpotifyUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.saksham.customloadingdialog.LoaderKt;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.ArrayList;
import java.util.LinkedList;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistSimplified;
import spotify.models.tracks.TrackFull;
import spotify.models.users.User;


public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private static final String CLIENT_ID = "7d2e57d7cf85444da0db0412f4116c80";
    private static final String REDIRECT_URI = "com.codepath.michfeng.songswiper://callback";
    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played,playlist-modify-public,playlist-read-private,user-modify-playback-state,app-remote-control,user-top-read,user-library-modify,user-follow-read,user-read-private";
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

        ParseUser.getCurrentUser().logOut();

        btnAuthenticate = (Button) findViewById(R.id.btnAuthenticate);
        btnAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // LoaderKt.showDialog(LoginActivity.this, true, R.raw.lottie);
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
            Log.i(TAG,"Request code approved");

            switch (response.getType()) {
                // The response is successful, and has produced an authorization token.
                case TOKEN:
                    Log.i(TAG, "Successful response!");
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    editor.apply();
                    waitForUserInfo(response);
                    break;

                // There is an error in log in attempt.
                case ERROR:
                    Log.e(TAG, "Error logging in");
                    break;

                // Most likely authentication has been cancelled.
                default:
                    Log.e(TAG, "Cancelled during authentication: "+ response.getType().toString());
                    authenticateSpotify();
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

        Log.i(TAG,"openLoginActivity finished");
    }

    private void waitForUserInfo(AuthorizationResponse response) {

        String accessToken = response.getAccessToken();

        UserService userService = new UserService(queue, msharedPreferences);
        try {
            Log.i(TAG, "Getting user");
            userService.get(() -> {
                try {
                    SpotifyUser user = userService.getUser();

                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("userid", user.id);

                    Log.d("STARTING", "Retrieved user information");
                    editor.commit();
                    startMainActivity(user.id, accessToken);
                } catch (Exception e){
                    Log.e(TAG, "Exception getting user1: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception getting user: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void startMainActivity(String id, String accessToken) {
        // Attempts to sign user up based on Spotify user id.
        // If user already exists in database, the method will throw an exception
        try {
            Log.i(TAG, "Attempting to sign up user " + id);

            ParseUser newUser = new ParseUser();
            newUser.setUsername(id);
            newUser.setPassword("password");

            newUser.signUp();
        } catch (ParseException e) {
        // Signing up threw an exception, so they may already exist in database, so we try logging in.
            Log.i(TAG, "Exception with signup: " + e.getMessage());
            logInUser(id);
        }

        // Goes to SwipeActivity once authentication is done.
        Log.i(TAG, "starting loading screen");
        LoaderKt.showDialog(this, true, R.raw.lottie);
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("accessToken", accessToken);
        Log.i(TAG,"Access token: " + accessToken);
        startActivity(i);
    }

    private void logInUser(String id) {
        Log.i(TAG, "Attempting to log in user " + id);
        ParseUser.logInInBackground(id, "password", new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                Log.i(TAG, "done with log in");
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this, "Invalid login", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }


    private void signUp(String id) {
        Log.i(TAG, "Attempting to sign up user " + id);

        ParseUser newUser = new ParseUser();
        newUser.setUsername(id);
        newUser.setPassword("password");

        newUser.signUpInBackground();

        // Make new LikedObjects object for new user.
        ParseObject likedObjects = new ParseObject("LikedObjects");

        likedObjects.put("likedTracks", new ArrayList<>());
        likedObjects.put("likedArtists", new ArrayList<>());
        likedObjects.put("likedGenres", new ArrayList<>());
        likedObjects.put("user", newUser);

        likedObjects.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Error saving: " + e.getStackTrace());
                    e.printStackTrace();
                } else {
                    Log.i(TAG, "Successful save");
                    Log.i(TAG, "Object id: " + likedObjects.getObjectId());
                    newUser.put("likedObjectsId", likedObjects.getObjectId());
                    newUser.saveInBackground();
                }
            }
        });

        newUser.put("likedObjectsId", likedObjects.getObjectId());
        newUser.saveInBackground();

        ParseObject followers = new ParseObject("Followers");
        followers.put("user", newUser);
        followers.saveInBackground();
    }


}