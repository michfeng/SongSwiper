package com.codepath.michfeng.songswiper.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.codepath.michfeng.songswiper.connectors.PlaylistAdapter;
import com.codepath.michfeng.songswiper.connectors.PlaylistAdapter1;
import com.codepath.michfeng.songswiper.models.Post;
import com.codepath.michfeng.songswiper.runnables.RunnableFriendPlaylist;
import com.codepath.michfeng.songswiper.runnables.RunnablePlaylist;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.playlists.PlaylistTrack;
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
    TextView userSince;
    String accessToken;
    PlaylistAdapter1 adapter;
    String friendSpotifyId;

    List<Post> posts;
    List<PlaylistTrack> songs;

    private static final String TAG = "FriendProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        profileHolder = findViewById(R.id.friendProfileHolder);
        ivProfile = findViewById(R.id.ivFriendProfile);
        rvFriendSongs = findViewById(R.id.rvFriendSongs);
        gridViewFriend = findViewById(R.id.gridViewFriend);
        tvNoneFound = findViewById(R.id.tvNoneFound);
        tvFriendName = findViewById(R.id.tvFriendName);
        btnFollow = findViewById(R.id.btnFollow);
        userSince = findViewById(R.id.tvFriendUserSince);

        accessToken = getIntent().getStringExtra("accessToken");
        friendSpotifyId = getIntent().getStringExtra("spotifyId");
        String id = getIntent().getStringExtra("userId");

        if (id == null) {
            // No profile was found.
            profileHolder.setVisibility(View.INVISIBLE);
        } else {
            // Profile found.
            tvNoneFound.setVisibility(View.INVISIBLE);

            // Set button initial state, store whether following in isFollowing (0 for not following, 1 for following, -1 for error).
            int isFollowing = setButtonState(id);
            Log.i(TAG, "Initial following state: " + isFollowing);
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
            ParseQuery<ParseUser> query = ParseUser.getQuery();
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

                        Date date = friend.getCreatedAt();
                        Log.i(TAG, "Date: " + date);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("LLLL dd, yyyy");
                        userSince.setText("User since " + simpleDateFormat.format(date).toString());
                    }
                }
            });


            // Populate posts.
            posts = new ArrayList<>();
            gridAdapter = new GridPostAdapter(this, posts, accessToken);
            gridViewFriend.setAdapter(gridAdapter);
            gridViewFriend.setLayoutManager(new GridLayoutManager(this, 3));
            queryPosts(id);

            // Populate songs.
            LinearLayoutManager HorizontalLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            songs = new ArrayList<>();
            adapter = new PlaylistAdapter1(this, songs, accessToken);
            rvFriendSongs.setAdapter(adapter);
            rvFriendSongs.setLayoutManager(HorizontalLayout);

            try {
                querySongs(friendSpotifyId);
            } catch (InterruptedException e) {
                Log.i(TAG, "exception here!");
                e.printStackTrace();
            }
        }
    }

    private void querySongs(String id) throws InterruptedException {
        Log.i(TAG, "here");

        RunnableFriendPlaylist runPlaylist = new RunnableFriendPlaylist(new SpotifyApi(accessToken), id);
        Thread thread = new Thread(runPlaylist);
        thread.setName("runPlaylist");
        thread.start();

        songs.addAll(runPlaylist.getPlaylist());
        Log.i(TAG, songs.toString());
        adapter.notifyDataSetChanged();
    }

    private void queryPosts(String id) {
        Log.i(TAG, "Querying posts from user with id: " + id);

        // Specify which class to query.
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        // Include data referred by user key.
         query.include(Post.KEY_USER);

        // Limit query to latest 20 items.
        query.setLimit(20);

        // Order posts by creation date.
        query.addDescendingOrder("createdAt");
        // query.whereEqualTo("user", id);

        // Start asynchronous call for posts.
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> postList, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Issue with getting posts",e);
                    return;
                }

                for (Post post : postList) {
                    if (post.getUser().getObjectId().equals(id)) {
                        posts.add(post);
                    }
                }
                Log.i(TAG, "Posts size: " + posts.size());

                for (Post post : posts) {
                    Log.i(TAG,"Post: "+post.getCaption()+", username: " + post.getUser().getUsername());
                }

                // Save received posts.
                gridAdapter.notifyDataSetChanged();
            }
        });
    }

    // Follow or unfollow other user with given id.
    private void follow(String id, int num) {
        // num represents whether we want to follow (1) or unfollow (0).

        // Query for current user's following relation.
        ParseQuery<ParseObject> followersQ = new ParseQuery<ParseObject>("Followers");
        followersQ.whereEqualTo("objectId", ParseUser.getCurrentUser().getString("followersObjectId"));
        followersQ.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    ParseObject followersObj = objects.get(0);

                    ParseRelation<ParseUser> followingRel = followersObj.getRelation("following");

                    // Get friend object to remove/add from previously found relation.
                    ParseQuery<ParseUser> friendUserQ = ParseUser.getQuery();
                    friendUserQ.whereEqualTo("objectId", id);
                    friendUserQ.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objectsFriend, ParseException e) {
                            if (e == null) {
                                // Friend object found.
                                ParseUser friendUser = objectsFriend.get(0);
                                if (num == 0)
                                    followingRel.remove(friendUser);
                                else
                                    followingRel.add(friendUser);

                                followersObj.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Log.i(TAG, "Updated user object successfully");
                                        } else {
                                            Log.i(TAG, "Error updating user followers object: " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    }
                                });
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
        ParseQuery<ParseUser> getUserQuery = ParseUser.getQuery();;
        getUserQuery.whereEqualTo("objectId", id);
        getUserQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    // Retrieved user.
                    ParseUser object = objects.get(0);
                    ParseQuery<ParseObject> friendFollowersQ = new ParseQuery<ParseObject>("Followers");
                    friendFollowersQ.whereEqualTo("objectId", object.get("followersObjectId"));

                    Log.i(TAG, "Getting followers object with id: " + object.get("followersObjectId"));
                    friendFollowersQ.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                ParseObject friendsFollowers = objects.get(0);

                                ParseRelation followersRelation = friendsFollowers.getRelation("followers");
                                if (num == 0)
                                    followersRelation.remove(ParseUser.getCurrentUser());
                                else
                                    followersRelation.add(ParseUser.getCurrentUser());

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
            }
        });
    }

    private int setButtonState(String friendId) {
        final int[] returnedVal = {0};

        // Find Followers object for user.
        ParseQuery<ParseObject> followersQ = new ParseQuery<ParseObject>("Followers");
        followersQ.whereEqualTo("objectId", ParseUser.getCurrentUser().get("followersObjectId"));
        followersQ.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                final int[] ret = {-1};
                if (e == null) {
                    // Object found.
                    Log.i(TAG, "Objects size: " + objects.size());
                    ParseObject followersObj = objects.get(0);
                    Log.i(TAG, "Followers object id: " + followersObj.getObjectId());
                    ParseRelation<ParseUser> followingRel = followersObj.getRelation("following");
                    ParseQuery<ParseUser> relationQuery = followingRel.getQuery();
                    relationQuery.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> object, ParseException e) {
                            if (e == null) {
                                for (ParseUser user : object) {
                                    if (user.getObjectId().equals(friendId)) {
                                        // Friend found in relation, so user is following.
                                        Log.i(TAG, "Friend found in relation, so user is following.");
                                        btnFollow.setText("Unfollow");
                                        ret[0] = 1;
                                    }
                                }
                                if (ret[0] != 1) {
                                    // Friend not found in relation, so user is not following.
                                    Log.i(TAG, "Friend no found in relation, so user is not following.");
                                    btnFollow.setText("Follow");
                                    ret[0] = 0;
                                    Log.i(TAG, "ret[0] here: " + ret[0]);
                                }
                            } else {
                                Log.e(TAG, "Error checking followers relation");
                                e.printStackTrace();
                                ret[0] = -1;
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Error retrieving Followers object: " + e.getMessage());
                    e.printStackTrace();
                }
                returnedVal[0] = ret[0];
            }
        });

        Log.i(TAG, "Returning initial state: " + returnedVal[0]);
        return returnedVal[0];
    }
}