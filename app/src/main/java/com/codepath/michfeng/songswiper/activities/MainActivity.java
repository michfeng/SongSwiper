package com.codepath.michfeng.songswiper.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.fragments.FeedFragment;
import com.codepath.michfeng.songswiper.fragments.ProfileFragment;
import com.codepath.michfeng.songswiper.fragments.ProfileFragment1;
import com.codepath.michfeng.songswiper.fragments.SwipeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.parse.Parse;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ParseUser.getCurrentUser() == null) {
            // No user logged in, redirect to login page.
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

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
                bottomNavigationView.setSelectedItemId(R.id.action_profile);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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