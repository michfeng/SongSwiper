package com.codepath.michfeng.songswiper.connectors;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.runnables.RunnableImage;

import java.util.ArrayList;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.exceptions.SpotifyActionFailedException;
import spotify.models.players.requests.ChangePlaybackStateRequestBody;
import spotify.models.playlists.PlaylistTrack;

public class PlaylistAdapter1 extends RecyclerView.Adapter<PlaylistAdapter1.ViewHolder> {
    private Context context;
    private List<PlaylistTrack> tracks;
    private String accessToken;
    private FragmentActivity activity;

    private static final String TAG = "PlaylistAdapter";

    public PlaylistAdapter1 (Context context, List<PlaylistTrack> posts, String accessToken) {
        this.context = context;
        this.tracks = posts;
        this.accessToken = accessToken;
    }

    @NonNull
    @Override
    public PlaylistAdapter1.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new PlaylistAdapter1.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter1.ViewHolder holder, int position) {
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
        private ImageView btnPlay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTrackCover = itemView.findViewById(R.id.ivSong);
        }

        public void bind(PlaylistTrack track) {
            SpotifyApi spotifyApi = new SpotifyApi(accessToken);

                RunnableImage runImage = new RunnableImage(spotifyApi, track.getTrack().getId());
                Thread threadImage = new Thread(runImage);
                threadImage.setName("runImage");
                threadImage.start();

                try {
                    String imagePath = runImage.getImage().getUrl();
                    if (imagePath != null) {
                        Glide.with(context).load(imagePath).circleCrop().into(ivTrackCover);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                // Handle click for play button.
                ivTrackCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // Context to play in (can be playlist/album/artist). Here we want the album of the track.
                                ArrayList<String> uris = new ArrayList<>();
                                Log.i(TAG, "uri: " + track.getTrack().getUri());
                                Log.i(TAG, "uri size" + uris.size());
                                uris.add(track.getTrack().getUri());

                                ChangePlaybackStateRequestBody body = new ChangePlaybackStateRequestBody();
                                body.setUris(uris);

                                try {
                                    spotifyApi.changePlaybackState(body);
                                    /*activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, "Playing track: " + track.getTrack().getName(), Toast.LENGTH_SHORT);
                                        }
                                    });*/
                                } catch (SpotifyActionFailedException e) {
                                    Log.e(TAG, "Error playing body: " + e.getMessage());
                                    /*activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, "Error playing: " + e.getMessage(), Toast.LENGTH_SHORT);
                                        }
                                    });*/
                                }
                            }
                        });
                        thread.start();
                    }
                });
            }


        // Add list of items, change to type used.
        public void addAll(List<PlaylistTrack> list) {
            tracks.addAll(list);
            notifyDataSetChanged();
        }
    }
}
