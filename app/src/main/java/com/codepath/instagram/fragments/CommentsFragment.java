package com.codepath.instagram.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.instagram.EndlessRecyclerViewScrollListener;
import com.codepath.instagram.FeedFragment;
import com.codepath.instagram.R;
import com.codepath.instagram.activities.LoginActivity;
import com.codepath.instagram.adapters.CommentsAdapter;
import com.codepath.instagram.adapters.PostsAdapter;
import com.codepath.instagram.databinding.FragmentCommentsBinding;
import com.codepath.instagram.models.Comment;
import com.codepath.instagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;


public class CommentsFragment extends FeedFragment {

    private static final String TAG = "CommentsFragment";
    private FragmentCommentsBinding binding;
    public Post post;
    public LinearLayoutManager llManager;
    public CommentsAdapter adapter;
    protected EndlessRecyclerViewScrollListener scrollListener;

    public CommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCommentsBinding.inflate(getLayoutInflater(), container, false);

        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        post = (Post) Parcels.unwrap(getArguments().getParcelable(getString(R.string.post_object_key)));
        Log.i(TAG,"" + post.allComments.size());

        llManager = new LinearLayoutManager(getContext());
        adapter = new CommentsAdapter(getContext(), post.allComments);
        binding.rvComments.setAdapter(adapter);
        binding.rvComments.setLayoutManager(llManager);

        bindViewDetails();


        adapter.setListener(new CommentsAdapter.CommentsAdapterListener() {

            @Override
            public void userClicked(ParseUser user) {
                Log.i(TAG,"user clicked: going to profile");
                listener.goToFragment(new ProfileFragment(), Parcels.wrap(user));
            }

            @Override
            public void commentLiked(boolean isLiked, Comment comment) throws ParseException {
                Log.i(TAG,"user liked comment: going to POST to server");
                comment.updateUsersLikingThisInDB(isLiked,ParseUser.getCurrentUser().getObjectId());
                if (isLiked) {
                    Log.i(TAG,"setting like count");
                    comment.setLikeCount(comment.getLikeCount() - 1);
                }
                else {
                    Log.i(TAG,"setting like count");
                    comment.setLikeCount(comment.getLikeCount() + 1);
                }
            }
        });


        setButtonClickListeners();
    }

    private void bindViewDetails() {
        String username = post.getUser().getUsername();
        String boldUser = "<B>" + username + " </B>";
        String description = post.getDescription(false);
        binding.tvDescription.setText(Html.fromHtml(boldUser + description));
        String timeAgo = post.calculateTimeAgo();
        binding.tvTimestamp.setText(timeAgo);
        ParseFile image = post.getImage();
        image = post.getUser().getParseFile(PostDetailsFragment.USER_PIC_KEY);
        if (image != null) {
            Log.i(TAG,"about to glide user pic");
            Glide.with(getContext()).load(image.getUrl()).into(binding.ivUserPhoto);
        }
        image = LoginActivity.currentUser.getParseFile(PostsAdapter.USER_PIC_KEY);
        if (image != null) {
            Log.i(TAG,"about to glide user pic");
            Glide.with(getContext()).load(image.getUrl()).into(binding.ivCurrUserPhoto);
        }
    }

    private void setButtonClickListeners() {

        binding.ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                LoginActivity.currentUser = ParseUser.getCurrentUser();
                goLoginActivity();
            }
        });

        binding.ivBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        binding.cvCurrUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"user clicked: going to profile");
                listener.goToFragment(new ProfileFragment(), Parcels.wrap(ParseUser.getCurrentUser()));
            }
        });
        binding.sendCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentDescription = binding.etNewCommentTxt.getText().toString();
                binding.etNewCommentTxt.setText("");
                if (commentDescription.isEmpty()) {
                    Toast.makeText(getContext(), "Comment box cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Comment newComment = Comment.createNewComment(commentDescription,ParseUser.getCurrentUser(),post);
                } catch (ParseException e) {
                    Log.e(TAG,"error while creating comment after clicked compose: " + e.getMessage(),e);
                }
                adapter.notifyDataSetChanged();
                binding.rvComments.smoothScrollToPosition(0);
            }
        });
    }
}