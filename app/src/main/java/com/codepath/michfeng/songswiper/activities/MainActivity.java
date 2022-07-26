package com.codepath.michfeng.songswiper.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.fragments.FeedFragment;
import com.codepath.michfeng.songswiper.fragments.ProfileFragment;
import com.codepath.michfeng.songswiper.fragments.ProfileFragment1;
import com.codepath.michfeng.songswiper.fragments.SwipeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private String accessToken;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (ParseUser.getCurrentUser() == null) {
            Log.i("Main activity", "Current user is null");
            // No user logged in, redirect to login page.
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Log.i("MainActivity", "" + ParseUser.getCurrentUser());
        }*/

        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final FragmentManager fragmentManager = getSupportFragmentManager();

                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    accessToken = extras.getString("accessToken");
                } else {
                    accessToken = "";
                }

                // Defining Fragments (each represents a tab).
                final Fragment fragmentSwipe = new SwipeFragment();
                final Fragment fragmentFeed = new FeedFragment();
                final Fragment fragmentProfile = new ProfileFragment1();
                //final Fragment fragmentSearch = new SearchFragment();

                // Handles navigation selection.
                String finalAccessToken = accessToken;
                bottomNavigationView.setOnItemSelectedListener(
                        new NavigationBarView.OnItemSelectedListener() {
                            @Override
                            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                Fragment fragment;
                                switch (item.getItemId()) {
                                    case R.id.action_feed:
                                        fragment = fragmentFeed;
                                        break;
                                    case R.id.action_profile:
                                        fragment = fragmentProfile;
                                        break;
                                    case R.id.action_swipe:
                                    default:
                                        fragment = fragmentSwipe;
                                        break;
                                }

                                Bundle bundle = new Bundle();
                                bundle.putString("accessToken",finalAccessToken);
                                fragment.setArguments(bundle);


                                fragmentManager.beginTransaction().replace(R.id.flSubContainer, fragment).commit();
                                return true;
                            }
                        }
                );

                // Defaults to swipe view.
                bottomNavigationView.setSelectedItemId(R.id.action_feed);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Handle search view queries.
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();

        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", search);

                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        Intent i = new Intent(MainActivity.this, FriendProfileActivity.class);
                        i.putExtra("accessToken", accessToken);
                        if (e == null) {
                            // Object was found. Display user profile.
                            // There should only be one user here because users are unique.
                            ParseUser otherUser = objects.get(0);
                            i.putExtra("userId", otherUser.getObjectId());
                            i.putExtra("spotifyId", otherUser.getString("username"));
                            startActivity(i);
                        }
                        else if (e.equals(ParseException.OBJECT_NOT_FOUND)) {
                            // No object found. Switch to activity with no user.
                            startActivity(i);
                        } else {
                            Log.e(TAG, "Error in retrieving results: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle presses on action bar items.
        if (item.getItemId() == R.id.logout) {
            ParseUser.logOutInBackground();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}