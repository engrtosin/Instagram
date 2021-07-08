package com.codepath.instagram.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.instagram.R;
import com.codepath.instagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    public static final String TAG = "CommentsAdapter";
    public static final String USER_PIC_KEY = "userPic";

    private Context context;
    private List<Post> comments;
    private CommentsAdapterListener mListener;

    public interface CommentsAdapterListener {
        void userClicked(ParseUser user);
        void commentLiked(boolean isLiked, Post post) throws ParseException;
    }

    public void setListener(CommentsAdapterListener mListener) {
        this.mListener = mListener;
    }

    public CommentsAdapter(Context context, List<Post> comments) {
        this.context = context;
        this.comments = comments;
        this.mListener = mListener;
    }

    // Clean all elements of the recycler
    public void clear() {
        comments.clear();
        notifyDataSetChanged();
    }

    // Add a list of comments -- change to type used
    public void addAll(List<Post> list) {
        comments.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommentsAdapter.ViewHolder holder, int position) {
        Post comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername;
        private ImageView ivPostImage;
        private TextView tvDescription;
        private Post containedComment;
        private ImageView ivUserPhoto;
        private TextView tvTimestamp;
        private TextView tvLikesCount;
        private ImageView ivLikeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLikesCount = itemView.findViewById(R.id.tvLikesCount);
            ivLikeBtn = itemView.findViewById(R.id.ivLikeBtn);

            setViewClickListeners();
        }

        private void setViewClickListeners() {
            tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.userClicked(containedComment.getUser());
                }
            });

            ivUserPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.userClicked(containedComment.getUser());
                }
            });

            ivLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (containedComment.getCurrentUserLiked()) {
                        // user has liked. make user unlike
                        Log.i(TAG,"user has liked, make the user unlike");
                        ivLikeBtn.setImageResource(R.drawable.ufi_heart);
                        ivLikeBtn.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                        // tell the listener that user liked post before
                        try {
                            mListener.commentLiked(true,containedComment);
                        } catch (ParseException e) {
                            Log.e(TAG,"error posting like " + e.getMessage(),e);
                        }
                    }
                    else {
                        // user did not like. make user like
                        ivLikeBtn.setImageResource(R.drawable.ufi_heart_active);
                        ivLikeBtn.setColorFilter(ContextCompat.getColor(context, R.color.medium_red), android.graphics.PorterDuff.Mode.SRC_IN);
                        // tell the listener that user did not like post before
                        try {
                            mListener.commentLiked(false,containedComment);
                        } catch (ParseException e) {
                            Log.e(TAG,"error posting like " + e.getMessage(),e);
                        }
                    }
                    tvLikesCount.setText(containedComment.getStringLikeCount());
                }
            });
        }

        public void bind(Post post) {
            containedComment = post;
            String username = post.getUser().getUsername();
            tvUsername.setText(username);
            String boldUser = "<B>" + username + " </B>";
            String description = post.getDescription(false);
            tvDescription.setText(Html.fromHtml(boldUser + description));
            String timeAgo = Post.calculateTimeAgo(post.getCreatedAt());
            tvTimestamp.setText(timeAgo);
            tvLikesCount.setText(post.getStringLikeCount());
            if (containedComment.getCurrentUserLiked()) {
                // user has liked.
                ivLikeBtn.setImageResource(R.drawable.ufi_heart_active);
                ivLikeBtn.setColorFilter(ContextCompat.getColor(context, R.color.medium_red), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            else {
                // user did not like
                ivLikeBtn.setImageResource(R.drawable.ufi_heart);
                ivLikeBtn.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            ParseFile image = post.getImage();
            if (image != null) {
                Log.i(TAG,"about to glide image");
                Glide.with(context).load(image.getUrl()).into(ivPostImage);
            }
            image = post.getUser().getParseFile(USER_PIC_KEY);
            if (image != null) {
                Log.i(TAG,"about to glide user pic");
                Glide.with(context).load(image.getUrl()).into(ivUserPhoto);
            }
        }
    }

}