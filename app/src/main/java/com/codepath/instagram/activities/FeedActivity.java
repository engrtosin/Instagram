package com.codepath.instagram.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.codepath.instagram.adapters.PostsAdapter;
import com.codepath.instagram.databinding.ActivityFeedBinding;
import com.codepath.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    public static final String TAG = "FeedActivity";
    private static final int MAX_POST_NUM = 20;
    public static int COMPOSE_REQUEST_CODE = 12;

    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    ActivityFeedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Log.i(TAG,"Tofunmi");

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allPosts);
        binding.rvPosts.setAdapter(adapter);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(this));

        binding.ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                goLoginActivity();
            }
        });

        binding.ivNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goComposePostActivity();
            }
        });

        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                queryPosts();
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        
        queryPosts();
    }

    private void goComposePostActivity() {
        Intent i = new Intent(FeedActivity.this, ComposePostActivity.class);
        startActivityForResult(i, COMPOSE_REQUEST_CODE);
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        // limit query to latest 20 items
        query.setLimit(MAX_POST_NUM);
        query.orderByDescending(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Failure in querying posts.",e);
                    return;
                }
                Log.i(TAG,"Success in querying posts.");
                adapter.clear();
                adapter.addAll(posts);
                binding.swipeContainer.setRefreshing(false);
            }
        });
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}