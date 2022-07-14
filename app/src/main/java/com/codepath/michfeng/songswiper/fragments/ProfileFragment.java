package com.codepath.michfeng.songswiper.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.PlaylistAdapter;
import com.codepath.michfeng.songswiper.connectors.PostsAdapter;
import com.codepath.michfeng.songswiper.connectors.RunnableImage;
import com.codepath.michfeng.songswiper.connectors.RunnablePlaylist;
import com.codepath.michfeng.songswiper.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.generic.AbstractPlayableObject;
import spotify.models.playlists.PlaylistTrack;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private TextView tvName;
    private ImageView ivProfile;
    private ParseUser currentUser;
    private RecyclerView playlistViewer;
    protected List<PlaylistTrack> songs;
    protected PlaylistAdapter adapter;
    private BottomNavigationView bottomNavigationView;
    private TextView userSince;

    private static final String TAG = "ProfileFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accessToken Parameter 1.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String accessToken) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, accessToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String accessToken = getArguments().getString("accessToken");

        tvName = view.findViewById(R.id.tvName);
        ivProfile = view.findViewById(R.id.ivProfile);
        playlistViewer = view.findViewById(R.id.playlistViewer);
        userSince = view.findViewById(R.id.userSince);
        currentUser = ParseUser.getCurrentUser();

        tvName.setText(currentUser.getUsername());

        Date date = currentUser.getCreatedAt();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("LLLL dd, yyyy");
        userSince.setText("User since " + simpleDateFormat.format(date).toString());

        ParseFile image = currentUser.getParseFile("profilePicture");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).circleCrop().into(ivProfile);
        }

        songs = new ArrayList<>();
        adapter = new PlaylistAdapter(getContext(), songs, accessToken);
        playlistViewer.setAdapter(adapter);
        playlistViewer.setLayoutManager(new LinearLayoutManager(getContext()));

        try {
            querySongs();
        } catch (InterruptedException e) {
            Log.i(TAG, "exception here!");
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    private void querySongs() throws InterruptedException {
        Log.i(TAG, "here");
        String accessToken = getArguments().getString("accessToken");

        RunnablePlaylist runPlaylist = new RunnablePlaylist(new SpotifyApi(accessToken));
        Thread thread = new Thread(runPlaylist);
        thread.setName("runPlaylist");
        thread.start();

        songs.addAll(runPlaylist.getPlaylist());
        Log.i(TAG, songs.toString());
        adapter.notifyDataSetChanged();
    }
}