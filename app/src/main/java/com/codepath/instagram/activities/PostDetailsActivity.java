package com.codepath.instagram.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.codepath.instagram.R;
import com.codepath.instagram.adapters.PostsAdapter;
import com.codepath.instagram.databinding.ActivityPostDetailsBinding;
import com.codepath.instagram.models.Post;
import com.parse.ParseFile;

import org.parceler.Parcels;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailsActivity";

    ActivityPostDetailsBinding binding;
    Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPostDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        post = Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));

        fillInViews();
    }

    private void fillInViews() {
        Log.i(TAG, "Is post null: " + String.valueOf(post == null));
        binding.tvDescription.setText(post.getDescription(true));
        binding.tvUsername.setText(post.getUser().getUsername());
        ParseFile image = post.getImage();
        if (image != null) {
            Log.i(TAG,"about to glide image");
            Glide.with(this).load(image.getUrl()).into(binding.ivPostImage);
        }
        String timeAgo = Post.calculateTimeAgo(post.getCreatedAt());
        binding.tvTimestamp.setText(timeAgo);
    }
}