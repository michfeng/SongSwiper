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
import com.codepath.michfeng.songswiper.models.Post;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.parse.ParseFile;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private Context context;
    private List<Post> posts;

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


    class ViewHolder extends RecyclerView.ViewHolder {
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

            // Handle click for like button.
            btnLike.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    // Record that specific user has liked the post.


                    // Change the appearance of button to reflect (shade).
                    btnLike.setLiked(true);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    // Record that user has unliked the post.

                    // Change the appearance of button to reflect action (unshade).
                    btnLike.setLiked(false);
                }
            });
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

            ParseFile prof = post.getUser().getParseFile("profilePic");

            if (prof != null) {
                Glide.with(context).load(prof.getUrl()).circleCrop().into(itemProfile);
            }
        }

        // add list of items, change to type used
        public void addAll(List<Post> list) {
            posts.addAll(list);
            notifyDataSetChanged();
        }
    }
}
