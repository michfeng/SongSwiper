package com.codepath.michfeng.songswiper.connectors;

import android.content.Context;
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
import com.codepath.michfeng.songswiper.runnables.RunnableImage;

import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.playlists.PlaylistTrack;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private Context context;
    private List<PlaylistTrack> tracks;
    private String accessToken;

    private static final String TAG = "PlaylistAdapter";

    public PlaylistAdapter (Context context, List<PlaylistTrack> posts, String accessToken) {
        this.context = context;
        this.tracks = posts;
        this.accessToken = accessToken;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_track, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaylistTrack track = tracks.get(position);
        holder.bind(track);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    // Clear all elements of recycler.
    public void clear() {
        tracks.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivTrackCover;
        private TextView tvSongTitle;
        private TextView tvSongArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTrackCover = itemView.findViewById(R.id.ivTrackCover);
            tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
        }

        public void bind(PlaylistTrack track) {
            Log.i(TAG, tvSongTitle.toString());

            if (track.getTrack() != null) {
                if (track.getTrack().getName() != null)
                    tvSongTitle.setText(track.getTrack().getName());


                SpotifyApi spotifyApi = new SpotifyApi(accessToken);

                RunnableImage runImage = new RunnableImage(spotifyApi, track.getTrack().getId());
                Thread threadImage = new Thread(runImage);
                threadImage.setName("runImage");
                threadImage.start();

                try {
                    String imagePath = runImage.getImage().getUrl();
                    if (imagePath != null) {
                        Glide.with(context).load(imagePath).into(ivTrackCover);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Add list of items, change to type used.
        public void addAll(List<PlaylistTrack> list) {
            tracks.addAll(list);
            notifyDataSetChanged();
        }
    }
}
