package com.codepath.instagram.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.codepath.instagram.EndlessRecyclerViewScrollListener;
import com.codepath.instagram.FeedFragment;
import com.codepath.instagram.R;
import com.codepath.instagram.adapters.PostsAdapter;
import com.codepath.instagram.databinding.ActivityFeedBinding;
import com.codepath.instagram.fragments.ChangePicFragment;
import com.codepath.instagram.fragments.CommentsFragment;
import com.codepath.instagram.fragments.ComposeFragment;
import com.codepath.instagram.fragments.PostDetailsFragment;
import com.codepath.instagram.fragments.PostsFragment;
import com.codepath.instagram.fragments.ProfileFragment;
import com.codepath.instagram.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    public static final String TAG = "FeedActivity";

    ActivityFeedBinding binding;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    FeedFragment.FeedFragmentInterface fragmentListener;
    FeedFragment currentFragment;
    FeedFragment previousFragment;
    ParseUser profileUser;
    FeedActivityInterface myListener;

    public interface FeedActivityInterface {
        public void refreshPage();
    }

    public void setMyListener(FeedActivityInterface feedActivityListener) {
        this.myListener = feedActivityListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileUser = ParseUser.getCurrentUser();
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        fragmentListener = new FeedFragment.FeedFragmentInterface() {
            @Override
            public void goToActivity(Class activityClass, Parcelable extraInfo) {

            }

            @Override
            public void goToFragment(FeedFragment toFragment, Parcelable extraInfo) {
                // this method should not be used when button navigation can be used
                toFragment.setListener(fragmentListener);
                previousFragment = currentFragment;
                currentFragment = toFragment;
                if (previousFragment != null) {
                    Log.i(TAG,"previous: " + previousFragment.getClass());
                }
                Log.i(TAG,"current: " + currentFragment.getClass());
                if (toFragment instanceof PostDetailsFragment) {
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.post_object_key),extraInfo);
                    toFragment.setArguments(args);
                    fragmentManager.beginTransaction().replace(R.id.flContainer, toFragment).commit();
                }
                else if (toFragment instanceof ProfileFragment) {
                    profileUser = (ParseUser) Parcels.unwrap(extraInfo);
                    binding.bottomNavigation.setSelectedItemId(R.id.action_profile);
                }
                else if (toFragment instanceof PostsFragment) {
                    binding.bottomNavigation.setSelectedItemId(R.id.action_home);
                }
                else if (toFragment instanceof CommentsFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.post_object_key),extraInfo);
                    toFragment.setArguments(args);
                    fragmentManager.beginTransaction().replace(R.id.flContainer, toFragment).commit();
                }
                else if (toFragment instanceof ChangePicFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                    Bundle args = new Bundle();
                    args.putParcelable(Post.KEY_USER,extraInfo);
                    toFragment.setArguments(args);
                    fragmentManager.beginTransaction().replace(R.id.flContainer, toFragment).commit();
                }
            }

            @Override
            public void reloadPage() {
                Log.i(TAG,"reloading called from compose");
                myListener.refreshPage();
            }
        };

        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                FeedFragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        profileUser = ParseUser.getCurrentUser();
                        fragment = new PostsFragment();
                        fragment.setListener(fragmentListener);
                        break;
                    case R.id.action_compose:
                        profileUser = ParseUser.getCurrentUser();
                        fragment = new ComposeFragment();
                        fragment.setListener(fragmentListener);
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        Bundle args = new Bundle();
                        Log.i(TAG,"Going to " + profileUser.getUsername() + "'s profile");
                        args.putParcelable(Post.KEY_USER,Parcels.wrap(profileUser));
                        fragment.setArguments(args);
                        fragment.setListener(fragmentListener);
                        break;
                    default:
                        fragment = new ComposeFragment();
                        break;
                }
                previousFragment = currentFragment;
                currentFragment = fragment;
                if (previousFragment != null) {
                    Log.i(TAG,"previous: " + previousFragment.getClass());
                }
                Log.i(TAG,"current: " + currentFragment.getClass());
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // Set default selection
        binding.bottomNavigation.setSelectedItemId(R.id.action_home);
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof PostDetailsFragment) {
            Log.i(TAG,"Back pressed in post details");
            if (previousFragment != null) {
                Log.i(TAG,"previous: " + previousFragment.getClass());
                if (previousFragment.getClass().equals(PostsFragment.class)) {
                    binding.bottomNavigation.setSelectedItemId(R.id.action_home);
                }
                else if (previousFragment.getClass().equals(ProfileFragment.class)) {
                    Log.i(TAG,"while previously in profile fragment");
                    binding.bottomNavigation.setSelectedItemId(R.id.action_profile);
                }
            }
        }
        else if (currentFragment instanceof CommentsFragment) {
            Log.i(TAG,"Back pressed in comments");
            binding.bottomNavigation.setVisibility(View.VISIBLE);
            if (previousFragment != null) {
                Log.i(TAG,"previous: " + previousFragment.getClass());
                if (previousFragment.getClass().equals(PostsFragment.class)) {
                    binding.bottomNavigation.setSelectedItemId(R.id.action_home);
                }
                else if (previousFragment.getClass().equals(ProfileFragment.class)) {
                    Log.i(TAG,"while previously in profile fragment");
                    binding.bottomNavigation.setSelectedItemId(R.id.action_profile);
                }
                else if (previousFragment.getClass().equals(PostDetailsFragment.class)) {
                    currentFragment = previousFragment;
                    fragmentManager.beginTransaction().replace(R.id.flContainer, previousFragment).commit();
                }
            }
        }
        else if (currentFragment instanceof ChangePicFragment) {
            binding.bottomNavigation.setVisibility(View.VISIBLE);
            binding.bottomNavigation.setSelectedItemId(R.id.action_profile);
        }
        else {
            super.onBackPressed();
        }
    }
}