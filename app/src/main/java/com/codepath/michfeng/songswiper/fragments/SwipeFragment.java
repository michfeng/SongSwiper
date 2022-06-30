package com.codepath.michfeng.songswiper.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.RecommendationService;
import com.codepath.michfeng.songswiper.connectors.ViewPager2Adapter;
import com.codepath.michfeng.songswiper.models.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.tracks.TrackFull;
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

    private static final String TAG = "SwipeFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SwipeFragment() {
        // Required empty public constructor
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

        // Initialize fields.
        viewpager = view.findViewById(R.id.viewpager);
        cards = new ArrayList<>();
        adapter = new ViewPager2Adapter(getContext(),cards);

        // Set the adapter of ViewPager (swiping view) to our created adapter.
        viewpager.setAdapter(adapter);

        String accessToken = getArguments().getString("accessToken");
        Log.i(TAG,"access token: "+accessToken);

        SpotifyApi spotifyApi = new SpotifyApi(accessToken);

        // Using Thread to make network calls on because Android doesn't allow for calls on main thread.
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    List<TrackFull> topTrackFull = spotifyApi.getTopTracks(new HashMap<>()).getItems();
                    List<ArtistFull> topArtistFull = spotifyApi.getTopArtists(new HashMap<>()).getItems();

                    List<String> topGenres = new ArrayList<>();
                    List<String> topTracks = new ArrayList<>();
                    List<String> topArtists = new ArrayList<>();

                    // At the moment, having all top seeds is not necessary since we are only using
                    // one of each (recommendation API takes 5 total seeds maximum), but I am storing
                    // them in case I would like to make more recommendations in the future.
                    for (TrackFull t : topTrackFull) topTracks.add(t.getId());
                    for (ArtistFull a : topArtistFull) {
                        topGenres.addAll(a.getGenres());
                        topArtists.add(a.getId());
                    }


                    // Making getting specific seeds
                    List<String> seed_artists = (new ArrayList<>());
                    seed_artists.add(topArtists.get(0));
                    seed_artists.add(topArtists.get(1));

                    List<String> seed_genres = (new ArrayList<>());
                    seed_genres.add(topGenres.get(0));

                    List<String> seed_tracks = (new ArrayList<>());
                    seed_tracks.add(topTracks.get(0));

                    Log.i(TAG,"seed tracks: "+seed_tracks.toString());
                    Log.i(TAG,"seed genres: "+seed_genres.toString());
                    Log.i(TAG,"seed artists: "+seed_artists.toString());


                    recommendations = spotifyApi.getRecommendations(seed_artists,seed_genres,seed_tracks,new HashMap<String, String>());
                    Log.i(TAG,"recommended tracks: "+recommendations.getTracks().toString());

                    for (TrackSimplified r : recommendations.getTracks()) {
                        Card c = new Card();
                        c.setTrackName(r.getName());
                        c.setArtistName(r.getArtists().get(0).getName());
                        c.setArtistImagePath(r.getArtists().get(0).getImages().get(0).getUrl());
                        c.setPreview(r.getPreviewUrl());
                        cards.add(c);
                    }

                    adapter.notifyDataSetChanged();

                    // To get swipe event of viewpager2
                    viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        // This method is triggered when there is any scrolling activity for the current page
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                        }

                        // triggered when you select a new page
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                        }

                        // triggered when there is
                        // scroll state will be changed
                        @Override
                        public void onPageScrollStateChanged(int state) {
                            super.onPageScrollStateChanged(state);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_swipe, container, false);
    }
}