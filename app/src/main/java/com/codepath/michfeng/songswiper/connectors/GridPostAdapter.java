package com.codepath.michfeng.songswiper.connectors;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.activities.PostDetailsActivity;
import com.codepath.michfeng.songswiper.models.Post;

import java.util.List;

// Binds posts to grid view on profiles.
public class GridPostAdapter extends RecyclerView.Adapter<GridPostAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;
    private String accessToken;

    private static final String TAG = "GridPostAdapter";

    public GridPostAdapter(Context context, List<Post> posts, String accessToken) {
        this.context = context;
        this.posts = posts;
        this.accessToken = accessToken;
        Log.i(TAG, "Constructor called");
    }

    @NonNull
    @Override
    public GridPostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridPostAdapter.ViewHolder holder, int position) {
        Log.i(TAG, "Binding viewholder");
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivSong;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSong = itemView.findViewById(R.id.ivPost);
            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            Log.i(TAG, "Binding post: " + post.getCaption());
            if (post.getImage() != null)
                Glide.with(context).load(post.getImage()).into(ivSong);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "Registered click on post");
            int position = getAdapterPosition();
            Post post = posts.get(position);
            Intent i = new Intent(context, PostDetailsActivity.class);
            i.putExtra("post", post);
            i.putExtra("accessToken", accessToken);
            context.startActivity(i);
        }
    }
}
