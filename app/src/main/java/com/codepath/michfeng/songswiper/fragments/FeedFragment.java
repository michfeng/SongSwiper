package com.codepath.michfeng.songswiper.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.codepath.michfeng.songswiper.R;
import com.codepath.michfeng.songswiper.connectors.PostsAdapter;
import com.codepath.michfeng.songswiper.models.Post;
import com.codepath.michfeng.songswiper.runnables.RunnableImage;
import com.codepath.michfeng.songswiper.runnables.RunnableSort;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import spotify.api.spotify.SpotifyApi;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView rvFeed;
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    private Button btnSort;
    private String accessToken;

    private SwipeRefreshLayout swipeContainer;

    private static final String TAG = "FeedFragment";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accessToken Parameter 1.
     * @return A new instance of fragment FeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedFragment newInstance(String accessToken) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, accessToken);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        accessToken = getArguments().getString("accessToken");

        rvFeed = view.findViewById(R.id.rvFeed);
        btnSort = view.findViewById(R.id.btnSort);

        // Initialize list that holds posts.
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts, accessToken);

        // Set adapter on recycler view.
        rvFeed.setAdapter(adapter);

        // Set layout manager on recycler view.
        rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));

        // look up swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        // Setup refresh listener (start loading new data).
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });

        // Configure refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sortPosts();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        queryPosts();
    }

    private void queryPosts() {
        // Specify which class to query.
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        // Include data referred by user key.
        query.include(Post.KEY_USER);

        // Limit query to latest 20 items.
        query.setLimit(20);

        // Order posts by creation date.
        query.addDescendingOrder("createdAt");

        // Start asynchronous call for posts.
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Issue with getting posts",e);
                    return;
                }
                for (Post post : posts) {
                    Log.i(TAG,"Post: "+post.getCaption()+", username: " + post.getUser().getUsername());
                }

                // Save received posts.
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchTimelineAsync(int page) {
        adapter.clear();
        queryPosts();
        swipeContainer.setRefreshing(false);
    }

    private void sortPosts() throws InterruptedException {

        RunnableSort run = new RunnableSort(new SpotifyApi(accessToken), allPosts);
        Thread thread = new Thread(run);
        thread.setName("run");
        thread.start();

        allPosts.clear();
        allPosts.addAll(run.getSorted());
        adapter.notifyDataSetChanged();
    }
}