package com.codepath.instagram.models;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Parcel
public class Comment {

    public static final String KEY_DESCRIPTION = "commentBody";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_POST_ID = "postId";
    public static final String KEY_USER = "user";
    public static final String KEY_POST = "post";
    public static final String KEY_USERS_LIKING = "usersLikingThis";
    private static final String TAG = "CommentModel";
    private static final int MAX_DESC_LENGTH = 100;
    public static final String KEY_CLASS_COMMENT = "Comment";

    public ParseObject parseComment;
    public List<String> allUsersLikingThis = new ArrayList<>();
    public int likeCount;
    public String description;
    public ParseUser user;
    public Post post;
    public Comment newComment;

    public static Comment createNewComment(String description, ParseUser user, Post post) throws ParseException {
        Comment comment = new Comment();
        comment.parseComment = new ParseObject("Comment");
        comment.parseComment.put(KEY_DESCRIPTION,description);
        comment.parseComment.put(KEY_USER,user);
        comment.post = post;
        comment.user = user;
        comment.parseComment.put(KEY_POST,post);
        comment.parseComment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error saving comment " + e.getMessage(),e);
                }
            }
        });
        post.addComment(0,comment);
        comment.allUsersLikingThis = new ArrayList<>();
        return comment;
    }

    public void initializeCommentFields() throws JSONException {

        getUsersLikingThisFromDB();
        setInitialLikeCount();
    }

    public static Comment getCommentFromId(String id) {
        final Comment[] newComment = new Comment[1];
        ParseQuery query = ParseQuery.getQuery(KEY_CLASS_COMMENT);
        query.include(KEY_POST);
        query.include(KEY_USER);

        query.getInBackground(id, new GetCallback() {
            @Override
            public void done(ParseObject object, ParseException e) {
                try {
                    newComment[0] = getCommentFromParseObject(object);
                    if (e != null) {
                        Log.e(TAG,"Error saving comment " + e.getMessage(),e);
                    }
                } catch (JSONException jsonException) {
                    Log.e(TAG,"Error getting comment from parseobject " + e.getMessage(), e);
                }
            }

            @Override
            public void done(Object o, Throwable throwable) {

            }
        });
        return newComment[0];
    }

    public static Comment getCommentFromParseObject(ParseObject parseObject) throws JSONException {
        Comment comment = new Comment();
        comment.description = parseObject.getString(KEY_DESCRIPTION);
        comment.parseComment = parseObject;
        ParseQuery query = new ParseQuery(Post.class);
        query.include(KEY_USER);
        comment.getUsersLikingThisFromDB();
        return comment;
    }

    public void getUsersLikingThisFromDB() throws JSONException {
        allUsersLikingThis = new ArrayList<>();
        if (parseComment.has(KEY_USERS_LIKING)) {
            JSONArray userJSONArray = parseComment.getJSONArray(KEY_USERS_LIKING);
            Log.i(TAG,userJSONArray.toString());
            for (int i = 0; i < userJSONArray.length(); i++) {
                Log.i(TAG,"jsonObject to string: " + userJSONArray.getString(i));
                allUsersLikingThis.add(userJSONArray.getString(i));
            }
        }
    }

    public void updateUsersLikingThisInDB(boolean isRemove, String id) throws ParseException {
        if (isRemove) {
            allUsersLikingThis.remove(id);
            parseComment.removeAll(KEY_USERS_LIKING, Arrays.asList(id));
        }
        else {
            if (!allUsersLikingThis.contains(id)) {
                allUsersLikingThis.add(id);
            }
            parseComment.addUnique(KEY_USERS_LIKING,id);
        }
        parseComment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
    }

    public String getDescription(boolean isFull) {

        String fullDescription = parseComment.getString(KEY_DESCRIPTION);
        if (!isFull) {
            if (fullDescription.length() > MAX_DESC_LENGTH) {
                return fullDescription.substring(0,MAX_DESC_LENGTH);
            }
        }
        return fullDescription;
    }

    public void setDescription(String description) {
        parseComment.put(KEY_DESCRIPTION, description);
    }

    public ParseUser getUser() {
        return user;
    }

    public Post getPost() {
        return post;
    }

    public String getStringLikeCount() {
        Log.i(TAG,"like count in getStringLikeCount: " + likeCount);
        if (likeCount == 0) {
            return "No like";
        }
        if (likeCount == 1) {
            return "1 like";
        }
        return likeCount + " likes";
    }

    public void setLikeCount(int likeCount) {
        Log.i(TAG,"set like count in setLikeCount: " + likeCount);
        this.likeCount = likeCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean getCurrentUserLiked() {
        Log.i(TAG,"did user like: " + String.valueOf(allUsersLikingThis.contains(ParseUser.getCurrentUser().getObjectId())));
        return allUsersLikingThis.contains(ParseUser.getCurrentUser().getObjectId());
    }

    public String calculateTimeAgo() {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            Date createdAt = parseComment.getCreatedAt();
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m ago";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h ago";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d ago";
            }
        } catch (Exception e) {
            Log.e("Error:", "getRelativeTimeAgo failed", e);
        }

        return "";
    }

    public void setInitialLikeCount() throws JSONException {
        likeCount = allUsersLikingThis.size();
    }
}
