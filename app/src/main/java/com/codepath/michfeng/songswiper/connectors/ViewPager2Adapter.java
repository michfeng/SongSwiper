package com.codepath.michfeng.songswiper.connectors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.models.Card;
import com.codepath.michfeng.songswiper.models.Post;
import com.parse.ParseFile;

import java.util.List;

public class ViewPager2Adapter  extends RecyclerView.Adapter<ViewPager2Adapter.ViewHolder> {

    private Context context;
    private List<Card> cards;

    public ViewPager2Adapter(Context context, List<Card> cards) {
        this.context = context;
        this.cards = cards;
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
        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView ivCardCover;
        private ImageView ivCardArtist;
        private TextView tvCardName;
        private TextView tvCardArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCardCover =  itemView.findViewById(R.id.ivCardCover);
            ivCardArtist = itemView.findViewById(R.id.ivCardArtist);
            tvCardArtist = itemView.findViewById(R.id.tvCardArtist);
            tvCardName = itemView.findViewById(R.id.tvCardName);
        }

        public void bind(Card card) {
            tvCardName.setText(card.getTrackName());
            tvCardArtist.setText(card.getArtistName());

            String coverIm = card.getCoverImagePath();
            if (coverIm != null) {
                Glide.with(context).load(coverIm).into(ivCardCover);
            }

            String artistIm = card.getArtistImagePath();
            if (artistIm != null) {
                Glide.with(context).load(artistIm).into(ivCardCover);
            }
        }
    }
}
