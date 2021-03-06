package com.codepath.michfeng.songswiper.connectors;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.fragments.FeedFragment;
import com.codepath.michfeng.songswiper.fragments.SwipeFragment;
import com.codepath.michfeng.songswiper.models.Card;
import com.codepath.michfeng.songswiper.models.Post;
import com.parse.ParseFile;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.exceptions.SpotifyActionFailedException;
import spotify.models.players.Offset;
import spotify.models.players.requests.ChangePlaybackStateRequestBody;

// This class binds each element of the recommendation card stack to their respective values in the layout file.
public class ViewPager2Adapter extends RecyclerView.Adapter<ViewPager2Adapter.ViewHolder> {

    private Context context;
    private List<Card> cards;
    private String accessToken;
    private FragmentActivity activity;

    private static final String TAG = "ViewPager2Adapter";

    public ViewPager2Adapter(Context context, List<Card> cards, String accessToken, FragmentActivity a) {
        this.context = context;
        this.cards = cards;
        this.accessToken = accessToken;
        this.activity = a;
    }

    @NonNull
    @Override
    public ViewPager2Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cards_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPager2Adapter.ViewHolder holder, int position) {
        Card card = cards.get(position);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SpotifyApi api = new SpotifyApi(accessToken);

                // Context to play in (can be playlist/album/artist). Here we want the album of the track.
                ArrayList <String> uris = new ArrayList<>();
                Log.i(TAG, "uri: " + card.getUri());
                Log.i(TAG, "uri size" + uris.size());
                uris.add(card.getUri());

                ChangePlaybackStateRequestBody body = new ChangePlaybackStateRequestBody();
                body.setUris(uris);

                try {
                    api.changePlaybackState(body);
                } catch (SpotifyActionFailedException e) {
                    Log.e(TAG, "Error playing body: " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Error playing: " + e.getMessage(), Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });

        thread.start();
        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivCardCover;
        private TextView tvCardName;
        private TextView tvCardArtist;
        private RelativeLayout relativeLayoutCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCardCover =  itemView.findViewById(R.id.ivCardCover);
            tvCardArtist = itemView.findViewById(R.id.tvCardArtist);
            tvCardName = itemView.findViewById(R.id.tvCardName);
            relativeLayoutCard = itemView.findViewById(R.id.relativeLayoutCard);
            relativeLayoutCard.setClipToOutline(true);
        }

        public void bind(Card card) {
            tvCardName.setText(card.getTrackName());
            tvCardArtist.setText(card.getArtistName());

            String coverIm = card.getCoverImagePath();
            if (coverIm != null) {
                Glide.with(context).load(coverIm).into(ivCardCover);
            }
        }
    }
}
