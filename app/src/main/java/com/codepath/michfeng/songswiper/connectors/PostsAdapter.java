package com.codepath.michfeng.songswiper.connectors;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

import java.util.List;

// Binds posts to recycler view on feed.
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private Context context;
    private List<Post> posts;

    private static final String TAG = "PostsAdapter";

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
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

    // clear all elements of recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView itemAlbum;
        private ImageView itemProfile;
        private TextView itemDescription;
        private TextView itemUsername;
        private LikeButton btnLike;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemAlbum = itemView.findViewById(R.id.itemAlbum);
            itemProfile = itemView.findViewById(R.id.itemProfile);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemUsername = itemView.findViewById(R.id.itemUsername);
            btnLike = itemView.findViewById(R.id.btnLike);
        }


        public void bind(Post post) {
            // bind data to view elements
            String username = post.getUser().getUsername();
            String caption = post.getCaption();

            itemUsername.setText(username);

            String text = username + " " + caption;
            SpannableString ss = new SpannableString(text);
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            ss.setSpan(boldSpan, 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            itemDescription.setText(ss);


            String image = post.getImage();

            if (image != null) {
                Glide.with(context).load(image).into(itemAlbum);
            }

            ParseFile prof = post.getUser().getParseFile("profilePicture");

            if (prof != null) {
                Log.i(TAG, "image not null");
                Glide.with(context).load(prof.getUrl()).circleCrop().into(itemProfile);
            }

            // Set like button initial state.
            ParseRelation<ParseUser> relation = post.getRelation("likes");
            ParseQuery<ParseUser> relationQuery = relation.getQuery();

            relationQuery.whereEqualTo(ParseUser.KEY_OBJECT_ID, ParseUser.getCurrentUser());
            relationQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        // The current user is contained in the like relation, so the heart should be filled.
                        btnLike.setLiked(true);
                    } else {
                        if (e.equals(ParseException.OBJECT_NOT_FOUND))
                            btnLike.setLiked(false);
                        else
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
        }

        // add list of items, change to type used
        public void addAll(List<Post> list) {
            posts.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            // Make sure that position is valid.
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                Log.i(TAG, "post: " + post.getCaption());

                // Create intent to redirect to details page.
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("post", post);
                context.startActivity(intent);
            }
        }
    }
}
