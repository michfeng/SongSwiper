package com.codepath.michfeng.songswiper.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.PlaylistAdapter;
import com.codepath.michfeng.songswiper.runnables.RunnablePlaylist;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
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
    private TextView tvStats;
    private ImageView ivHover;
    private TextView tvChange;
    private File photoFile;

    private String photoFileName = "photo.jpg";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
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
        tvStats = view.findViewById(R.id.tvStats);
        ivHover = view.findViewById(R.id.ivHover);
        tvChange = view.findViewById(R.id.tvChangeProfile);

        Glide.with(getContext()).load("https://htmlcolorcodes.com/assets/images/colors/gray-color-solid-background-1920x1080.png").circleCrop().into(ivHover);
        ivHover.setAlpha(127);
        ivHover.setVisibility(View.INVISIBLE);
        tvChange.setVisibility(View.INVISIBLE);

        tvName.setText(currentUser.getUsername());

        Date date = currentUser.getCreatedAt();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("LLLL dd, yyyy");
        userSince.setText("User since " + simpleDateFormat.format(date).toString());

        String stats = "" + currentUser.getInt("numLiked") + " liked songs out of " + currentUser.getInt("numSwiped") + " swiped tracks";
        tvStats.setText(stats);

        ParseFile image = currentUser.getParseFile("profilePicture");
        if (image != null) {
            Glide.with(getContext()).load(image.getUrl()).circleCrop().into(ivProfile);
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

        // On hover, can add profile picture.
        ivProfile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        ivHover.setVisibility(View.VISIBLE);
                        tvChange.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        ivHover.setVisibility(View.INVISIBLE);
                        tvChange.setVisibility(View.INVISIBLE);
                        launchCamera();
                        break;
                }
                return true;
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });
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

    private void launchCamera() {
        Log.i(TAG, "camera launch method called");

        // Create Intent to take picture and return control to calling app.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create file reference to access to future access.
        photoFile = getPhotoFileUri(photoFileName);
        Log.i(TAG, "photofile: " + photoFile.toString());

        // Wrap File object in content provider.
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            Log.i(TAG, "starting image capture intent");
            // Start image capture intent to take photo.
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns File for photo stored on disk given fileName.
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create storage directory if it does not exist.
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return file target for photo based on filename.
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "took profile picture");

                // Have camera photo on disk, convert to ParseFile.
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                takenImage.compress(Bitmap.CompressFormat.PNG,100,stream);
                byte[] byteArray = stream.toByteArray();
                ParseFile file = new ParseFile("image.png",byteArray);

                // Save profile picture to user.
                ParseUser user = ParseUser.getCurrentUser();
                user.put("profilePicture", file);
                user.saveInBackground();
                Glide.with(getContext()).load(file).circleCrop().into(ivProfile);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken", Toast.LENGTH_SHORT).show();
            }
        }
    }
}