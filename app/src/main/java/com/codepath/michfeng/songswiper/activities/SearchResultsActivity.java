package com.codepath.michfeng.songswiper.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.codepath.michfeng.songswiper.R;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class SearchResultsActivity extends Activity {

    private static final String TAG = "SearchResultsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Here: ");
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.i(TAG, "Search intent found");

            String search = intent.getStringExtra(SearchManager.QUERY);

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", search);

            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    Intent i = new Intent(SearchResultsActivity.this, FriendProfileActivity.class);
                    if (e == null) {
                        // Object was found. Display user profile.
                        // There should only be one user here because users are unique.
                        ParseUser otherUser = objects.get(0);
                        i.putExtra("userId", otherUser.getObjectId());
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
        }
    }
}
