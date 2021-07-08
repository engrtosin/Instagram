package com.codepath.instagram.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.codepath.instagram.FeedFragment;
import com.codepath.instagram.R;
import com.codepath.instagram.databinding.FragmentPostDetailsBinding;
import com.codepath.instagram.databinding.FragmentPostsBinding;
import com.codepath.instagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

public class PostDetailsFragment extends FeedFragment {

    public static final String TAG = "PostDetailsFragment";
    public static final String USER_PIC_KEY = "userPic";

    FragmentPostDetailsBinding binding;
    Post containedPost;

    public PostDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        containedPost = Parcels.unwrap(getArguments().getParcelable(getString(R.string.post_object_key)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPostDetailsBinding.inflate(getLayoutInflater(), container, false);

        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bind(containedPost);
        setViewListeners();
    }

    private void setViewListeners() {
        binding.ivBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.goToFragment(new PostsFragment(),null);
            }
        });
        binding.ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                goLoginActivity();
            }
        });
        binding.ivLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (containedPost.getCurrentUserLiked()) {
                    // user has liked. make user unlike
                    Log.i(TAG,"user has liked, make the user unlike");
                    binding.ivLikeBtn.setImageResource(R.drawable.ufi_heart);
                    binding.ivLikeBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                    // tell the listener that user liked post before
                    whenContainedPostLiked();
                }
                else {
                    // user did not like. make user like
                    binding.ivLikeBtn.setImageResource(R.drawable.ufi_heart_active);
                    binding.ivLikeBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.medium_red), android.graphics.PorterDuff.Mode.SRC_IN);
                    // tell the listener that user did not like post before
                    whenContainedPostLiked();
                }
                binding.tvLikesCount.setText(containedPost.getStringLikeCount());
            }
        });
    }

    private void whenContainedPostLiked() {
        try {
            Log.i(TAG,"user liked: going to POST to server");
            if (containedPost.getCurrentUserLiked()) {
                Log.i(TAG,"setting like count");
                containedPost.setLikeCount(containedPost.getLikeCount() - 1);
                containedPost.updateUsersLikingThisInDB(true,ParseUser.getCurrentUser().getObjectId());
            }
            else {
                Log.i(TAG,"setting like count");
                containedPost.setLikeCount(containedPost.getLikeCount() + 1);
                containedPost.updateUsersLikingThisInDB(false,ParseUser.getCurrentUser().getObjectId());
            }
        } catch (ParseException e) {
            Log.e(TAG,"error posting like in details" + e.getMessage(),e);
        }
    }

    public void bind(Post post) {
        String username = post.getUser().getUsername();
        binding.tvUsername.setText(username);
        String boldUser = "<B>" + username + " </B>";
        String description = post.getDescription(false);
        binding.tvDescription.setText(Html.fromHtml(boldUser + description));
        String timeAgo = Post.calculateTimeAgo(post.getCreatedAt());
        binding.tvTimestamp.setText(timeAgo);
        binding.tvLikesCount.setText(post.getStringLikeCount());
        if (containedPost.getCurrentUserLiked()) {
            // user has liked.
            binding.ivLikeBtn.setImageResource(R.drawable.ufi_heart_active);
            binding.ivLikeBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.medium_red), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else {
            // user did not like
            binding.ivLikeBtn.setImageResource(R.drawable.ufi_heart);
            binding.ivLikeBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        ParseFile image = post.getImage();
        if (image != null) {
            Log.i(TAG,"about to glide image");
            Glide.with(getContext()).load(image.getUrl()).into(binding.ivPostImage);
        }
        image = post.getUser().getParseFile(USER_PIC_KEY);
        if (image != null) {
            Log.i(TAG,"about to glide user pic");
            Glide.with(getContext()).load(image.getUrl()).into(binding.ivUserPhoto);
        }
    }
}