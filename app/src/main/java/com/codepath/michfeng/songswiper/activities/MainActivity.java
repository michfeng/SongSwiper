package com.codepath.michfeng.songswiper.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.fragments.FeedFragment;
import com.codepath.michfeng.songswiper.fragments.ProfileFragment;
import com.codepath.michfeng.songswiper.fragments.SwipeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        Bundle extras = getIntent().getExtras();
        String accessToken = "";
        if (extras != null) {
            accessToken = extras.getString("accessToken");
        }

        // Defining Fragments (each represents a tab).
        final Fragment fragmentSwipe = new SwipeFragment();
        final Fragment fragmentFeed = new FeedFragment();
        final Fragment fragmentProfile = new ProfileFragment();

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

                        fragmentManager.beginTransaction().replace(R.id.rlContainer, fragment).commit();
                        return true;
                    }
                }
        );

        // defaults to swipe view
        bottomNavigationView.setSelectedItemId(R.id.action_swipe);
    }
}