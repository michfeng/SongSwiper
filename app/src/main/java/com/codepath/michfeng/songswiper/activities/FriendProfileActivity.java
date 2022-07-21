package com.codepath.michfeng.songswiper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.GridPostAdapter;
import com.codepath.michfeng.songswiper.models.Post;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import spotify.models.tracks.TrackFull;

public class FriendProfileActivity extends AppCompatActivity {

    RelativeLayout profileHolder;
    ImageView ivProfile;
    RecyclerView rvFriendSongs;
    RecyclerView gridViewFriend;
    TextView tvNoneFound;
    TextView tvFriendName;
    Button btnFollow;
    GridPostAdapter gridAdapter;

    List<Post> posts;
    List<String> trackIds;

    private static final String TAG = "FriendProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        profileHolder = findViewById(R.id.friendProfileHolder);
        ivProfile = findViewById(R.id.rvFriendSongs);
        rvFriendSongs = findViewById(R.id.rvFriendSongs);
        gridViewFriend = findViewById(R.id.gridViewFriend);
        tvNoneFound = findViewById(R.id.tvNoneFound);
        tvFriendName = findViewById(R.id.tvFriendName);
        btnFollow = findViewById(R.id.btnFollow);

        String id = getIntent().getStringExtra("userId");

        if (id == null) {
            // No profile was found.
            profileHolder.setVisibility(View.INVISIBLE);
        } else {
            // Profile found.
            tvNoneFound.setText(View.INVISIBLE);

            // Set button initial state, store whether following in isFollowing (0 for not following, 1 for following, -1 for error).
            int isFollowing = setButtonState(id);
            if (isFollowing == -1) {
                Log.e(TAG, "Error checking whether user is following");
            }


            // Handle button press.
            btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFollowing == 1) {
                        follow(id, 0);
                        btnFollow.setText("Follow");
                    }
                    else if (isFollowing == 0) {
                        follow(id, 1);
                        btnFollow.setText("Unfollow");
                    }
                }
            });

            // Load friend information.
            ParseQuery<ParseUser> query = ParseQuery.getQuery("ParseUser");
            query.whereEqualTo("objectId", id);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        Log.i(TAG, "Friend found");
                        ParseUser friend = objects.get(0);

                        tvFriendName.setText(friend.getUsername());

                        ParseFile profilePic = friend.getParseFile("profilePicture");
                        if (profilePic != null) {
                            Glide.with(FriendProfileActivity.this).load(profilePic.getUrl()).circleCrop().into(ivProfile);
                        }
                    }
                }
            });


            // Populate posts.
            posts = new ArrayList<>();
            gridAdapter = new GridPostAdapter(this, posts, "accessToken");
            gridViewFriend.setAdapter(gridAdapter);
            queryPosts(id);
        }
    }

    private void queryPosts(String id) {
        // Specify which class to query.
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        // Include data referred by user key.
        query.include(Post.KEY_USER);

        // Limit query to latest 20 items.
        query.setLimit(20);

        // Order posts by creation date.
        query.addDescendingOrder("createdAt");
        query.whereEqualTo("user", id);

        // Start asynchronous call for posts.
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Issue with getting posts",e);
                    return;
                }
                for (Post post : posts) {
                    Log.i(TAG,"Post: "+post.getCaption()+", username: " + post.getUser().getUsername());
                }

                // Save received posts.
                posts.clear();
                posts.addAll(posts);
                gridAdapter.notifyDataSetChanged();
            }
        });
    }

    // Follow or unfollow other user with given id.
    private void follow(String id, int num) {
        // num represents whether we want to follow (1) or unfollow (0).

        // Query for current user's following relation.
        ParseQuery<ParseObject> followersQ = new ParseQuery<ParseObject>("Followers");
        followersQ.whereEqualTo("user", ParseUser.getCurrentUser().getObjectId());
        followersQ.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // Object found, get Friend's user object.
                    ParseObject followersObj = objects.get(0);

                    ParseQuery<ParseUser> friendUserQ = ParseUser.getQuery();
                    friendUserQ.whereEqualTo("objectId", id);
                    friendUserQ.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objectsFriend, ParseException e) {
                            if (e == null) {
                                // Friend object found.
                                ParseUser friendUser = objectsFriend.get(0);
                                ParseRelation<ParseUser> followingRel = followersObj.getRelation("following");
                                if (num == 0)
                                    followingRel.remove(friendUser);
                                else
                                    followingRel.add(friendUser);

                                try {
                                    followersObj.save();
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                Log.e(TAG, "Error retrieving Friend object");
                                e.printStackTrace();
                            }
;                        }
                    });
                }
            }
        });

        // Query for other user's followers relation.
        ParseQuery<ParseObject> friendFollowersQ = new ParseQuery<ParseObject>("Followers");
        followersQ.whereEqualTo("user", id);
        followersQ.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    ParseObject friendsFollowers = objects.get(0);
                    ParseRelation followingRel = friendsFollowers.getRelation("followers");
                    if (num == 0)
                        followingRel.remove(ParseUser.getCurrentUser());
                    else
                        followingRel.add(ParseUser.getCurrentUser());

                    try {
                        friendsFollowers.save();
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Error retrieving friend's object");
                    e.printStackTrace();
                }
            }
        });
    }

    private int setButtonState(String friendId) {
        final int[] ret = {-1};

        // Find Followers object for user.
        ParseQuery<ParseObject> followersQ = new ParseQuery<ParseObject>("Followers");
        followersQ.whereEqualTo("user", ParseUser.getCurrentUser().getObjectId());
        followersQ.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // Object found.
                    ParseObject followersObj = objects.get(0);
                    ParseRelation<ParseUser> followingRel = followersObj.getRelation("following");
                    ParseQuery<ParseUser> relationQuery = followingRel.getQuery();
                    relationQuery.whereEqualTo("objectId", friendId);
                    relationQuery.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> object, ParseException e) {
                            if (e == null) {
                                // Friend found in relation, so user is following.
                                btnFollow.setText("Unfollow");
                                ret[0] = 1;
                            } else {
                                if (e.equals(ParseException.OBJECT_NOT_FOUND)) {
                                    // Friend not found in relation, so user is not following.
                                    btnFollow.setText("Follow");
                                    ret[0] = -1;
                                } else {
                                    Log.e(TAG, "Error checking followers relation");
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                } else {
                    Log.e(TAG, "Error retrieving Followers object: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        return ret[0];
    }
}