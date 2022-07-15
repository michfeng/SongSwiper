package com.codepath.michfeng.songswiper.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.activities.LikedActivity;
import com.codepath.michfeng.songswiper.runnables.RunnableImage;
import com.codepath.michfeng.songswiper.runnables.RunnableRecs;
import com.codepath.michfeng.songswiper.connectors.ViewPager2Adapter;
import com.codepath.michfeng.songswiper.models.Card;
import com.parse.Parse;
import com.parse.ParseUser;
import com.saksham.customloadingdialog.LoaderKt;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.tracks.TrackSimplified;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SwipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SwipeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecommendationCollection recommendations;
    private ViewPager2 viewpager;
    private ViewPager2Adapter adapter;
    private List<Card> cards;

    private int index;

    private static final String TAG = "SwipeFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SwipeFragment() {
        // Required empty public constructor.
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param access_token Parameter 1.
     * @return A new instance of fragment SwipeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SwipeFragment newInstance(String access_token) {
        SwipeFragment fragment = new SwipeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, access_token);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String accessToken = getArguments().getString("accessToken");
        Log.i(TAG,"access token: " + accessToken);

        // Initialize fields.
        viewpager = view.findViewById(R.id.viewpager);
        cards = new ArrayList<>();
        adapter = new ViewPager2Adapter(getContext(), cards, accessToken);
        index = 0;

        // Set the adapter of ViewPager (swiping view) to our created adapter.
        viewpager.setAdapter(adapter);

        // Start loading screen.

        SpotifyApi spotifyApi = new SpotifyApi(accessToken);

        Log.i(TAG, "" + ParseUser.getCurrentUser().get("likedTracks"));

        RunnableRecs r = new RunnableRecs(spotifyApi, getContext(), ParseUser.getCurrentUser());
        Log.i(TAG, "checkpoint 1");

        // Using Thread to make network calls on because Android doesn't allow for calls on main thread.
        Thread thread = new Thread(r);
        thread.setName("r");
        thread.start();
        try {
            Log.i(TAG, "checkpoint 2");
            recommendations = r.getRecs();
            Log.i(TAG,"recommended tracks: " + recommendations);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Log.i(TAG,"recommended tracks: " + recommendations.getTracks().toString());

        for (TrackSimplified rec : recommendations.getTracks()) {
            // Check whether this song is playable in user's market.
            //if (rec.isPlayable()) {
                Log.i(TAG, "card: " + rec.getName() + ", artist: " + rec.getArtists().get(0).getName());
                if (rec.getArtists().get(0).getImages() == null)
                    Log.i(TAG, "null images");

                Card c = new Card();
                c.setId(rec.getId());
                c.setTrackName(rec.getName());
                c.setArtistName(rec.getArtists().get(0).getName());
                c.setUri(rec.getUri());
                c.setPreview(rec.getPreviewUrl());

                RunnableImage runImage = new RunnableImage(spotifyApi, rec.getId());
                Thread threadImage = new Thread(runImage);
                threadImage.setName("runImage");
                threadImage.start();
                try {
                    if (runImage.getImage() != null)
                        c.setCoverImagePath(runImage.getImage().getUrl());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cards.add(c);
                /* cards.add(0, new Card()); */
           //}
        }

        viewpager.post(new Runnable() {
            @Override
            public void run() {
                viewpager.setCurrentItem(1, true);
            }
        });

        adapter.notifyDataSetChanged();

        // To get swipe event of viewpager2.
        viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            // This method is triggered when there is any scrolling activity for the current page.
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            // Triggered when you select a new page.
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Record that current user has swiped for user statistics.
                ParseUser currentUser = ParseUser.getCurrentUser();
                currentUser.put("numSwiped", currentUser.getInt("numSwiped") + 1);
                currentUser.saveInBackground();

                // If the new position is to the left of old position.
                // Therefore swipe right action (like) has taken place.
                if (position == index - 1) {
                    Card c = cards.get(index);
                    Intent intent = new Intent(getContext(), LikedActivity.class);
                    intent.putExtra("card", Parcels.wrap(c));
                    intent.putExtra("accesstoken", accessToken);
                    startActivity(intent);
                }

                index = position;
            }

            // Triggered when scroll state will be changed.
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Log.i(TAG, "starting loading screen");
        LoaderKt.showDialog(getContext(), true, R.raw.lottie);*/

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_swipe, container, false);
    }
}