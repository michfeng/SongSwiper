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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.activities.LikedActivity;
import com.codepath.michfeng.songswiper.runnables.RunnableImage;
import com.codepath.michfeng.songswiper.runnables.RunnableRecs;
import com.codepath.michfeng.songswiper.connectors.ViewPager2Adapter;
import com.codepath.michfeng.songswiper.models.Card;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.RangeSlider;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.saksham.customloadingdialog.LoaderKt;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ImageView options;
    private String accessToken;

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

        accessToken = getArguments().getString("accessToken");
        Log.i(TAG,"access token: " + accessToken);

        // Initialize fields.
        viewpager = view.findViewById(R.id.viewpager);
        cards = new ArrayList<>();
        adapter = new ViewPager2Adapter(getContext(), cards, accessToken, getActivity());
        options = view.findViewById(R.id.ivOptions);
        index = 0;

        // Set up bottom sheet for options button press.
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Logged click");
                showBottomDialog();
            }
        });


        // Set the adapter of ViewPager (swiping view) to our created adapter.
        viewpager.setAdapter(adapter);

        // Get recommendations.
        getRecommendations(new HashMap<>(), true);

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

    private void getRecommendations(Map<String, String> options, boolean explicit) {
        // Start loading screen.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoaderKt.showDialog(getContext(), true, R.raw.lottie);
            }
        });

        ParseUser user = ParseUser.getCurrentUser();
        Log.i(TAG, "Current user: " + user);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run() called from thread.");
                ParseQuery<ParseObject> query = ParseQuery.getQuery("LikedObjects");
                String likedObjectsId = user.getString("likedObjectsId");
                Log.i(TAG, "likedObjectsId:" + likedObjectsId);
                query.getInBackground(user.getString("likedObjectsId"), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject obj, ParseException e) {
                        if (e == null) {
                            Log.i(TAG, "Successful getting likedObjects");
                            // No exception, so we have successfully found corresponding LikedObjects for user.
                            // Note that there should only be one row corresponding to each user, so we only
                            // need to retrieve the first out of this returned list.

                            SpotifyApi spotifyApi = new SpotifyApi(accessToken);

                            // Get liked objects from retrieved ParseObject.
                            List<String> likedTracks = (ArrayList<String>) obj.get("likedTracks");
                            List<String> likedArtists = (ArrayList<String>) obj.get("likedArtists");
                            List<String> likedGenres = (ArrayList<String>) obj.get("likedGenres");

                            RunnableRecs r = new RunnableRecs(spotifyApi, getContext(), ParseUser.getCurrentUser(),
                                    likedTracks, likedArtists, likedGenres, options, explicit);

                            // Using Thread to make network calls on because Android doesn't allow for calls on main thread.
                            Thread thread = new Thread(r);
                            thread.setName("r");
                            thread.start();
                            try {
                                recommendations = r.getRecs();
                                Log.i(TAG,"recommended tracks: " + recommendations);
                            } catch (InterruptedException ex) {
                                Log.e(TAG, "Error retrieving recommendations: " + ex.getMessage());
                                ex.printStackTrace();
                            }

                            for (TrackSimplified rec : recommendations.getTracks()) {
                                Log.i(TAG, "card: " + rec.getName() + ", artist: " + rec.getArtists().get(0).getName());

                                // Check whether song is explicit (if we user wants to filter).
                                if ((!explicit && !rec.isExplicit())  || explicit) {
                                    Card c = new Card();
                                    c.setId(rec.getId());
                                    c.setTrackName(rec.getName());
                                    c.setArtistName(rec.getArtists().get(0).getName());
                                    c.setUri(rec.getUri());
                                    c.setPreview(rec.getPreviewUrl());
                                    c.setDuration(rec.getDurationMs());
                                    c.setExplicit(rec.isExplicit());

                                    RunnableImage runImage = new RunnableImage(spotifyApi, rec.getId());
                                    Thread threadImage = new Thread(runImage);
                                    threadImage.setName("runImage");
                                    threadImage.start();
                                    try {
                                        if (runImage.getImage() != null)
                                            c.setCoverImagePath(runImage.getImage().getUrl());
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }

                                    cards.add(c);
                                }
                            }

                            adapter.notifyDataSetChanged();

                            LoaderKt.hideDialog();

                            viewpager.post(new Runnable() {
                                @Override
                                public void run() {
                                    viewpager.setCurrentItem(1, true);
                                }
                            });
                        } else {
                            Log.e(TAG, "Error getting liked objects: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        t.start();
        Log.i(TAG, "Recommendation thread started");
    }

    private void showBottomDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);

        RangeSlider sliderAcoustic = bottomSheetDialog.findViewById(R.id.sliderAcoustic);
        RangeSlider sliderDanceability = bottomSheetDialog.findViewById(R.id.sliderDanceability);
        RangeSlider sliderEnergy = bottomSheetDialog.findViewById(R.id.sliderEnergy);
        RangeSlider sliderPopularity = bottomSheetDialog.findViewById(R.id.sliderPopularity);
        CheckBox checkExplicit = bottomSheetDialog.findViewById(R.id.checkExplicit);
        Button addFilters = bottomSheetDialog.findViewById(R.id.btnFilter);



        addFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            // Get new recommendations with new filters.
            public void onClick(View v) {
                List<Float> acoustic = sliderAcoustic.getValues();
                List<Float> danceability = sliderDanceability.getValues();
                List<Float> energy = sliderEnergy.getValues();
                List<Float> popularity = sliderPopularity.getValues();
                boolean isChecked = checkExplicit.isChecked();

                // Map containing optional parameters for recommendations.
                Map<String, String> options = new HashMap<>();
                options.put("max_acousticness", "" + (acoustic.get(1)/100.0));
                options.put("min_acousticness", "" + (acoustic.get(0)/100.0));
                options.put("max_danceability", "" + (danceability.get(1)/100.0));
                options.put("min_danceability", "" + (danceability.get(1)/100.0));
                options.put("max_energy", "" + (energy.get(1)/100.0));
                options.put("min_energy", "" + (energy.get(1)/100.0));
                options.put("max_popularity", "" + Math.round(popularity.get(1)));
                options.put("min_popularity", "" + Math.round(popularity.get(0)));

                Log.i(TAG, "Map keys: " + options.keySet());
                Log.i(TAG, "Map values: " + options.values());

                getRecommendations(options, !isChecked);
                bottomSheetDialog.hide();
            }
        });

        bottomSheetDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_swipe, container, false);
    }
}