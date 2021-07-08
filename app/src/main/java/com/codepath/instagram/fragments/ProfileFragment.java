package com.codepath.instagram.fragments;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.codepath.instagram.R;
import com.codepath.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

public class ProfileFragment extends PostsFragment {

    public static final String TAG = "ProfileFragment";

    ParseUser user;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Parcels.unwrap(getArguments().getParcelable(Post.KEY_USER));
        Log.i(TAG,user.getUsername());
    }

    @Override
    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
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
                adapter.clear();
                adapter.addAll(posts);
                binding.swipeContainer.setRefreshing(false);
            }
        });
    }
}
