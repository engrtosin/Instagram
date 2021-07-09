package com.codepath.instagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.instagram.EndlessRecyclerViewScrollListener;
import com.codepath.instagram.R;
import com.codepath.instagram.adapters.PostsAdapter;
import com.codepath.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends PostsFragment {

    public static final String TAG = "ProfileFragment";
    public static final int FRAGMENT_SPAN_COUNT = 1;

    public ParseUser user;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Parcels.unwrap(getArguments().getParcelable(Post.KEY_USER));
        Log.i(TAG,user.getUsername());
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridLayoutManager glManager = new GridLayoutManager(getContext(),FRAGMENT_SPAN_COUNT, GridLayoutManager.VERTICAL, false);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        adapter.clear();
        binding.rvPosts.setAdapter(adapter);
        binding.rvPosts.setLayoutManager(glManager);

        if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            binding.ivCamera.setVisibility(View.VISIBLE);
        }
        else {
            binding.ivCamera.setVisibility(View.GONE);
        }
        binding.ivCamera.setImageResource(R.drawable.ic_baseline_person_add_24);
        binding.ivCamera.setScaleX(1f);
        binding.ivCamera.setScaleY(1f);
        binding.ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.goToFragment(new ChangePicFragment(), Parcels.wrap(user));
            }
        });

        setButtonClickListeners();

        queryPosts();
    }

    private void updateProfilePic() {

    }

    @Override
    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.include(Post.KEY_COMMENTS);
        query.whereEqualTo(Post.KEY_USER,user);
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
                Log.i(TAG,""+posts.size());
                for (Post post: posts) {
                    try {
                        post.initializePostFields();
                    } catch (JSONException jsonException) {
                        Log.e(TAG,"Error setting initial like count" + jsonException.getMessage(), jsonException);
                    }
                }
                adapter.clear();
                adapter.addAll(posts);
                binding.swipeContainer.setRefreshing(false);
            }
        });
    }

    @Override
    protected void fetchOlderTweets() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.include(Post.KEY_COMMENTS);
        query.whereEqualTo(Post.KEY_USER,user);
        // limit query to latest 20 items
        query.setLimit(MAX_POST_NUM);
        query.orderByDescending(Post.KEY_CREATED_AT);
        query.setSkip(allPosts.size());
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Failure in querying posts.",e);
                    return;
                }
                Log.i(TAG,"Done with endless scrolling.");
                Log.i(TAG,"Success in querying posts.");
                for (Post post: posts) {
                    try {
                        post.initializePostFields();
                    } catch (JSONException jsonException) {
                        Log.e(TAG,"Error setting initial like count" + jsonException.getMessage(), jsonException);
                    }
                }
                adapter.addAll(posts);
                binding.swipeContainer.setRefreshing(false);
            }
        });
    }
}
