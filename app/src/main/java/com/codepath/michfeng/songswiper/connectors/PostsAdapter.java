package com.codepath.michfeng.songswiper.connectors;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.activities.PostDetailsActivity;
import com.codepath.michfeng.songswiper.models.Post;
import com.codepath.michfeng.songswiper.models.User;
import com.like.LikeButton;
import com.like.OnLikeListener;
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
import java.util.concurrent.TimeUnit;

import spotify.api.spotify.SpotifyApi;
import spotify.models.players.requests.ChangePlaybackStateRequestBody;

// Binds posts to recycler view on feed.
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private Context context;
    private List<Post> posts;
    private String accessToken;

    private static final String TAG = "PostsAdapter";

    public PostsAdapter(Context context, List<Post> posts, String accessToken) {
        this.context = context;
        this.posts = posts;
        this.accessToken = accessToken;
        Log.i(TAG, "Constructor called");
    }

    @NonNull
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Clear all elements of recycler.
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemAlbum;
        private ImageView itemProfile;
        private TextView itemDescription;
        private TextView itemUsername;
        private LikeButton btnLike;
        private ImageView btnPlay;
        private TextView tvDate;
        private TextView tvLikes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemAlbum = itemView.findViewById(R.id.itemAlbum);
            itemProfile = itemView.findViewById(R.id.itemProfile);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemUsername = itemView.findViewById(R.id.itemUsername);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnPlay = itemView.findViewById(R.id.playButton);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLikes = itemView.findViewById(R.id.tvLikes);

            GestureDetector mDetector = new GestureDetector(new myGestureListener());
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mDetector.onTouchEvent(event);
                }
            });
        }

        public void bind(Post post) {
            Log.i(TAG, "bind is happening");
            // Bind data to view elements.
            String username = post.getUser().getUsername();
            String caption = post.getCaption();

            itemUsername.setText(username);

            String text = username + " " + caption;
            SpannableString ss = new SpannableString(text);
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            ss.setSpan(boldSpan, 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            itemDescription.setText(ss);

            tvDate.setText(Post.calculateTimeAgo(post.getCreatedAt()));

            String image = post.getImage();

            if (image != null) {
                Glide.with(context).load(image).into(itemAlbum);
            }

            ParseFile prof = post.getUser().getParseFile("profilePicture");

            if (prof != null) {
                Glide.with(context).load(prof.getUrl()).circleCrop().into(itemProfile);
            }

            // Set like button initial state.
            ParseRelation<ParseUser> relation = post.getRelation("likes");
            ParseQuery<ParseUser> relationQuery = relation.getQuery();

            relationQuery.whereEqualTo(ParseUser.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());

            Log.i(TAG, relationQuery.getClassName());
            relationQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        // The current user is contained in the like relation, so the heart should be filled.
                        Log.i(TAG, "Like button pressed");
                        btnLike.setLiked(true);
                    } else {
                        if (e.equals(ParseException.OBJECT_NOT_FOUND)){
                            Log.i(TAG, "Like button not pressed");
                            btnLike.setLiked(false);
                        }
                        else
                            e.printStackTrace();
                    }
                }
            });

            // Query for get display number of likes.
            // Get number of likes.
            ParseRelation<ParseUser> rel = post.getRelation("likes");
            ParseQuery<ParseUser> query = rel.getQuery();
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() == 1)
                            tvLikes.setText("" + objects.size() + " like");
                        else
                            tvLikes.setText("" + objects.size() + " likes");
                    } else if (e.equals(ParseException.OBJECT_NOT_FOUND)) {
                        tvLikes.setText("0 likes");
                    } else {
                        Log.e(TAG, "Error getting numLikes: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            // Handle click for like button.
            btnLike.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    // Record that specific user has liked the post.
                    ParseRelation<ParseUser> relation = post.getRelation("likes");
                    relation.add(ParseUser.getCurrentUser());
                    try {
                        post.save();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // Change the appearance of button to reflect (shade).
                    btnLike.setLiked(true);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    // Record that user has unliked the post.
                    ParseRelation<ParseUser> relation = post.getRelation("likes");
                    relation.remove(ParseUser.getCurrentUser());
                    try {
                        post.save();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // Change the appearance of button to reflect action (unshade).
                    btnLike.setLiked(false);
                }
            });

            // Handle click for play button.
            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SpotifyApi api = new SpotifyApi(accessToken);

                            // Context to play in (can be playlist/album/artist). Here we want the album of the track.
                            ArrayList<String> uris = new ArrayList<>();
                            Log.i(TAG, "uri: " + post.getUri());
                            Log.i(TAG, "uri size" + uris.size());
                            uris.add(post.getUri());

                            ChangePlaybackStateRequestBody body = new ChangePlaybackStateRequestBody();
                            body.setUris(uris);

                            api.changePlaybackState(body);
                        }
                    });

                    thread.start();
                }
            });
        }

        // Add list of items, change to type used
        public void addAll(List<Post> list) {
            posts.addAll(list);
            notifyDataSetChanged();
        }

        public void onLike (View v) {
            Post post = posts.get(getAdapterPosition());

            // Record that specific user has liked the post.
            ParseRelation<ParseUser> relation = post.getRelation("likes");
            relation.add(ParseUser.getCurrentUser());
            try {
                post.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Change the appearance of button to reflect (shade).
            btnLike.setLiked(true);
        }

        // Handles types of actions on each post.
         class myGestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            // Double tap to like event.
            public boolean onDoubleTapEvent(MotionEvent e) {
                Post post = posts.get(getAdapterPosition());

                // Record that specific user has liked the post.
                ParseRelation<ParseUser> relation = post.getRelation("likes");
                relation.add(ParseUser.getCurrentUser());
                try {
                    post.save();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                // Change the appearance of button to reflect (shade).
                btnLike.setLiked(true);
                return super.onDoubleTapEvent(e);
            }

            @Override
            // Single tap to redirect to details page.
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int position = getAdapterPosition();

                Log.i(TAG, "Saw click");

                // Make sure that position is valid.
                if (position != RecyclerView.NO_POSITION) {
                    Post post = posts.get(position);
                    Log.i(TAG, "post: " + post.getCaption());

                    // Create intent to redirect to details page.
                    Intent intent = new Intent(context, PostDetailsActivity.class);
                    intent.putExtra("post", post);
                    intent.putExtra("accessToken", accessToken);
                    Log.i(TAG, "accessToken: " + accessToken);
                    context.startActivity(intent);
                }
                return super.onSingleTapUp(e);
            }
        }
    }
}
