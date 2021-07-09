package com.codepath.instagram.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.instagram.R;
import com.codepath.instagram.models.Comment;
import com.codepath.instagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";
    public static final String USER_PIC_KEY = "userPic";

    private Context context;
    private List<Post> posts;
    private PostsAdapterListener mListener;

    public interface PostsAdapterListener {
        void postClicked(Post post);
        void userClicked(ParseUser user);
        void postLiked(boolean isLiked, Post post) throws ParseException;
        void seeCommentsClicked(Post post);
        void createNewComment(String commentDescription, Post containedPost);
    }

    public void setListener(PostsAdapterListener mListener) {
        this.mListener = mListener;
    }

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.mListener = mListener;
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of posts -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PostsAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsername;
        private ImageView ivPostImage;
        private TextView tvDescription;
        private Post containedPost;
        private ImageView ivUserPhoto;
        private TextView tvTimestamp;
        private TextView tvLikesCount;
        private ImageView ivLikeBtn;
        private TextView tvSeeComments;
        private ImageView sendCompose;
        private EditText etNewCommentTxt;
        private ImageView ivCommentBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLikesCount = itemView.findViewById(R.id.tvLikesCount);
            ivLikeBtn = itemView.findViewById(R.id.ivLikeBtn);
            tvSeeComments = itemView.findViewById(R.id.tvSeeComments);
            sendCompose = itemView.findViewById(R.id.sendCompose);
            etNewCommentTxt = itemView.findViewById(R.id.etNewCommentTxt);
            ivCommentBtn = itemView.findViewById(R.id.ivCommentBtn);

            setViewClickListeners();
        }

        private void setViewClickListeners() {
            tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.userClicked(containedPost.getUser());
                }
            });

            ivUserPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.userClicked(containedPost.getUser());
                }
            });

            tvSeeComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.seeCommentsClicked(containedPost);
                }
            });

            ivCommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etNewCommentTxt.requestFocus();
                }
            });

            sendCompose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String commentDescription = etNewCommentTxt.getText().toString();
                    if (commentDescription.isEmpty()) {
                        Toast.makeText(context, "Comment box cannot be empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mListener.createNewComment(commentDescription, containedPost);
                }
            });

            ivLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (containedPost.getCurrentUserLiked()) {
                        // user has liked. make user unlike
                        Log.i(TAG,"user has liked, make the user unlike");
                        ivLikeBtn.setImageResource(R.drawable.ufi_heart);
                        ivLikeBtn.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                        // tell the listener that user liked post before
                        try {
                            mListener.postLiked(true,containedPost);
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
                            mListener.postLiked(false,containedPost);
                        } catch (ParseException e) {
                            Log.e(TAG,"error posting like " + e.getMessage(),e);
                        }
                    }
                    tvLikesCount.setText(containedPost.getStringLikeCount());
                }
            });
        }

        public void bind(Post post) {
            containedPost = post;
            String username = post.getUser().getUsername();
            tvUsername.setText(username);
            String boldUser = "<B>" + username + " </B>";
            String description = post.getDescription(false);
            tvDescription.setText(Html.fromHtml(boldUser + description));
            String timeAgo = Post.calculateTimeAgo(post.getCreatedAt());
            tvTimestamp.setText(timeAgo);
            tvLikesCount.setText(post.getStringLikeCount());
            if (containedPost.getCurrentUserLiked()) {
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
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG,"View holder clicked.");
                    mListener.postClicked(containedPost);
                }
            });
        }
    }

}
