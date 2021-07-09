package com.codepath.instagram.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.instagram.EndlessRecyclerViewScrollListener;
import com.codepath.instagram.FeedFragment;
import com.codepath.instagram.activities.LoginActivity;
import com.codepath.instagram.adapters.PostsAdapter;
import com.codepath.instagram.databinding.FragmentPostsBinding;
import com.codepath.instagram.models.Comment;
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

public class PostsFragment extends FeedFragment {

    public static final String TAG = "PostsFragment";
    public static final int MAX_POST_NUM = 20;
    public static final int FRAGMENT_SPAN_COUNT = 1;

    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    FragmentPostsBinding binding;
    GridLayoutManager glManager;
    protected EndlessRecyclerViewScrollListener scrollListener;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPostsBinding.inflate(getLayoutInflater(), container, false);

        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        glManager = new GridLayoutManager(getContext(),FRAGMENT_SPAN_COUNT, GridLayoutManager.VERTICAL, false);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        binding.rvPosts.setAdapter(adapter);
        binding.rvPosts.setLayoutManager(glManager);

        setButtonClickListeners();
        // TODO: Change this to be called only if posts are reloaded
        queryPosts();
    }

    public void setButtonClickListeners() {
        adapter.setListener(new PostsAdapter.PostsAdapterListener() {
            // TODO: Use and interface instead
            @Override
            public void postClicked(Post post) {
                listener.goToFragment(new PostDetailsFragment(), Parcels.wrap(post));
            }

            @Override
            public void userClicked(ParseUser user) {
                Log.i(TAG,"user clicked: going to profile");
                listener.goToFragment(new ProfileFragment(), Parcels.wrap(user));
            }

            @Override
            public void postLiked(boolean isLiked, Post post) throws ParseException {
                Log.i(TAG,"user liked: going to POST to server");
                post.updateUsersLikingThisInDB(isLiked,ParseUser.getCurrentUser().getObjectId());
                if (isLiked) {
                    Log.i(TAG,"setting like count");
                    post.setLikeCount(post.getLikeCount() - 1);
                }
                else {
                    Log.i(TAG,"setting like count");
                    post.setLikeCount(post.getLikeCount() + 1);
                }
            }

            @Override
            public void seeCommentsClicked(Post post) {
                listener.goToFragment(new CommentsFragment(), Parcels.wrap(post));
            }

            @Override
            public void createNewComment(String commentDescription, Post post) throws ParseException {
                Comment newComment = Comment.createNewComment(commentDescription,ParseUser.getCurrentUser(),post);
                listener.goToFragment(new CommentsFragment(), Parcels.wrap(post));
            }
        });

        binding.ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                LoginActivity.currentUser = ParseUser.getCurrentUser();
                goLoginActivity();
            }
        });

//        binding.ivNewPost.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                goComposePostActivity();
//            }
//        });

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

        scrollListener = new EndlessRecyclerViewScrollListener(glManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                fetchOlderTweets();
            }
        };
        binding.rvPosts.addOnScrollListener(scrollListener);
    }

    protected void fetchOlderTweets() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.include(Post.KEY_COMMENTS);
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

//    private void goComposePostActivity() {
//
//    }

    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.include(Post.KEY_COMMENTS);
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
}