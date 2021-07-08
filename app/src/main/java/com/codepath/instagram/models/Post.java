package com.codepath.instagram.models;

import android.util.Log;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseException;
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
    public static final String KEY_USERS_LIKING = "usersLikingThis";
    public static final int MAX_DESC_LENGTH = 100;
    private static final String TAG = "PostModel";

    public boolean currentUserLikedThis;
    public static List<String> allPostsCurrentUserLiked;
    public List<String> allUsersLikingThis;
    public ParseUser currentUser;
    public int likeCount;

    public static void getCurrUserPostsLikedFromDB() throws JSONException {
        ParseUser currentUser = ParseUser.getCurrentUser();
        allPostsCurrentUserLiked = new ArrayList<>();
        if (currentUser.has(KEY_POSTS_LIKED)) {
            JSONArray postsJson = currentUser.getJSONArray(KEY_POSTS_LIKED);
            Log.i(TAG,postsJson.toString());
            for (int i = 0; i < postsJson.length(); i++) {
                Log.i(TAG,"jsonObject to string: " + postsJson.getString(i));
                allPostsCurrentUserLiked.add(postsJson.getString(i));
            }
        }
    }

    public void getUsersLikingThisFromDB() throws JSONException {
        allUsersLikingThis = new ArrayList<>();
        if (this.has(KEY_USERS_LIKING)) {
            JSONArray userJSONArray = getJSONArray(KEY_USERS_LIKING);
            Log.i(TAG,userJSONArray.toString());
            for (int i = 0; i < userJSONArray.length(); i++) {
                Log.i(TAG,"jsonObject to string: " + userJSONArray.getString(i));
                allUsersLikingThis.add(userJSONArray.getString(i));
            }
        }
    }

    public static void updateCurrUserPostsLikedInDB(boolean isRemove, String id) throws ParseException {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (isRemove) {
            allPostsCurrentUserLiked.remove(id);
            currentUser.removeAll(KEY_POSTS_LIKED, Arrays.asList(id));
        }
        else {
            allPostsCurrentUserLiked.add(id);
            currentUser.add(KEY_POSTS_LIKED,id);
        }
        currentUser.save();
    }

    public void updateUsersLikingThisInDB(boolean isRemove, String id) throws ParseException {
        if (isRemove) {
            allUsersLikingThis.remove(id);
            removeAll(KEY_USERS_LIKING, Arrays.asList(id));
        }
        else {
            if (!allUsersLikingThis.contains(id)) {
                allUsersLikingThis.add(id);
            }
            addUnique(KEY_USERS_LIKING,id);
        }
        save();
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

    public void setInitialLikeCount() throws JSONException {
        getUsersLikingThisFromDB();
        likeCount = allUsersLikingThis.size();
    }
}
