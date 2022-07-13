package com.codepath.michfeng.songswiper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.codepath.michfeng.songswiper.fragments.FeedFragment;
import com.codepath.michfeng.songswiper.fragments.SwipeFragment;

public class SwipeFragmentExtended extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new FeedFragment()).commit();}
    }
}