package com.codepath.michfeng.songswiper.fragments;

import static com.parse.Parse.getApplicationContext;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.RecommendationService;
import com.codepath.michfeng.songswiper.connectors.TopTrackService;
import com.codepath.michfeng.songswiper.models.Artist;
import com.codepath.michfeng.songswiper.models.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.paging.Paging;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.tracks.TrackFull;

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
    private RecommendationService recommendationService;

    private static final String TAG = "SwipeFragment";

    TextView tv;

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
        tv = (TextView) view.findViewById(R.id.textView);

        recommendationService = new RecommendationService(getApplicationContext());

        String accessToken = getArguments().getString("accessToken");
        Log.i(TAG,"access token: "+accessToken);
        /*recommendationService.get(() -> {
            recommendations = recommendationService.getRecommendations();
            Log.i("SwipeFragment","recommended tracks: "+recommendations.toString());
        });*/


        SpotifyApi spotifyApi = new SpotifyApi(accessToken);

        List<TrackFull> topTrackFull = spotifyApi.getTopTracks(new HashMap<>()).getItems();
        List<ArtistFull> topArtistFull = spotifyApi.getTopArtists(new HashMap<>()).getItems();
        List<String> topGenres = new ArrayList<>();
        List<String> topTracks = new ArrayList<>();
        List<String> topArtists = new ArrayList<>();

        for (TrackFull t : topTrackFull) topTracks.add(t.getId());
        for (ArtistFull a : topArtistFull) {
            topGenres.addAll(a.getGenres());
            topArtists.add(a.getId());
        }

        recommendations = spotifyApi.getRecommendations(topArtists,topGenres,topTracks,new HashMap<String, String>());
        Log.i("SwipeFragment","recommended tracks: "+recommendations.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_swipe, container, false);
    }
}