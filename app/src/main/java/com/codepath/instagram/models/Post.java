package com.codepath.instagram.models;

import android.util.Log;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcel;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Parcel(analyze = Post.class)
@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_LIKE_COUNT = "likeCount";
    public static final String KEY_POSTS_LIKED = "postsLiked";
    public static final int MAX_DESC_LENGTH = 100;

    public boolean currentUserLikedThis;
    public static List<String> allPostsCurrentUserLiked;
    public ParseUser currentUser;

    public static void getCurrUserPostsLikedFromDB() throws JSONException {
        ParseUser currentUser = ParseUser.getCurrentUser();
        allPostsCurrentUserLiked = new ArrayList<>();
        if (currentUser.has(KEY_POSTS_LIKED)) {
            JSONArray postsJson = currentUser.getJSONArray(KEY_POSTS_LIKED);
            for (int i = 0; i < postsJson.length(); i++) {
                allPostsCurrentUserLiked.add(postsJson.getJSONObject(i).getString("post"));
            }
        }
    }

    public static void updateCurrUserPostsLikedInDB(boolean isRemove, String id) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (isRemove) {
            allPostsCurrentUserLiked.remove(id);
            currentUser.removeAll(KEY_POSTS_LIKED, Arrays.asList(id));
        }
        else {
            allPostsCurrentUserLiked.add(id);
            currentUser.add(KEY_POSTS_LIKED,id);
        }
    }

    public String getDescription(boolean isFull) {

        String fullDescription = getString(KEY_DESCRIPTION);
        if (!isFull) {
            if (fullDescription.length() > MAX_DESC_LENGTH) {
                return fullDescription.substring(0,MAX_DESC_LENGTH);
            }
        }
        return fullDescription;
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public String getLikeCount() {
        int likeCount = getInt(KEY_LIKE_COUNT);
        if (likeCount == 0) {
            return "No like";
        }
        if (likeCount == 1) {
            return "1 like";
        }
        return likeCount + " likes";
    }

    public void setLikeCount(int likeCount) {
        put(KEY_LIKE_COUNT, likeCount);
    }

    public boolean getCurrentUserLiked() {
        return false;
    }

    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
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
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }
}
