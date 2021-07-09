package com.codepath.instagram.models;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
    public static final String KEY_USER_PROFILE = "userPic";
    public static final int MAX_DESC_LENGTH = 100;
    private static final String TAG = "PostModel";
    public static final String KEY_COMMENTS = "comments";
    public static final int MAX_POST_NUM = 20;

    public boolean currentUserLikedThis;
    public static List<String> allPostsCurrentUserLiked;
    public List<String> allUsersLikingThis = new ArrayList<>();
    public ParseUser currentUser;
    public int likeCount;
    public List<Comment> allComments;
    public List<String> allCommentIds;
    public ParseObject thisParseObject;

    public ParseObject getThisParseObject() {
        return thisParseObject;
    }

    public void initializePostFields() throws JSONException {
        getUsersLikingThisFromDB();
        setInitialLikeCount();
        queryComments();
    }

    public void queryComments() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Comment.KEY_CLASS_COMMENT);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Comment.KEY_POST,this);
        query.orderByDescending(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    try {
                        allComments = commentsFromParseObjectList(objects);
                    } catch (JSONException jsonException) {
                        Log.e(TAG,"error while finding in background");
                    }
                }
                else {
                    Log.e(TAG,"error while finding in background " + e.getMessage(),e);
                }
            }
        });
    }

    private List<Comment> commentsFromParseObjectList(List<ParseObject> objects) throws JSONException {
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            comments.add(getCommentFromParseObject(objects.get(i)));
        }
        return comments;
    }

    private List<Comment> commentsFromObjectArray(List objects) throws JSONException {
        List<Comment> comments = new ArrayList<>();
        for (Object object : objects) {
            comments.add(getCommentFromParseObject((ParseObject) object));
        }
        return comments;
    }

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
        Log.i(TAG,"Like count for " + getDescription(false) + ": " + allUsersLikingThis.size());
    }

    public void getAllCommentsFromDB() throws JSONException {
        if (has(KEY_COMMENTS)) {
            Log.i(TAG,get(KEY_COMMENTS).getClass().toString());
        }
        allComments = commentsFromJSONArray(getJSONArray(KEY_COMMENTS));
        allCommentIds = commentIdsFromComments(allComments);
    }

//    public Comment createNewComment(String description, ParseUser user) throws ParseException {
//        Comment comment = new Comment();
//        comment.parseComment = new ParseObject("Comment");
//        comment.parseComment.put(KEY_DESCRIPTION,description);
//        comment.parseComment.put(KEY_USER,user);
//        comment.post = this;
//        comment.user = user;
//        comment.parseComment.put(Comment.KEY_POST,this);
//        comment.parseComment.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e != null) {
//                    Log.e(TAG,"Error saving comment " + e.getMessage(),e);
//                }
//            }
//        });
//        this.addComment(0,comment);
//        comment.allUsersLikingThis = new ArrayList<>();
//        return comment;
//    }

    private List<String> commentIdsFromComments(List<Comment> allComments) {
        List<String> commentIds = new ArrayList<>();
        for (int i = 0; i < allComments.size(); i++) {
            commentIds.add(i,allComments.get(i).parseComment.getObjectId());
        }
        return commentIds;
    }

    public static List<Comment> commentsFromArray(JSONArray commentObjects) throws JSONException {
        List<Comment> comments = new ArrayList<>();
        Log.i(TAG,commentObjects.toString());
        for (int i = 0; i < commentObjects.length(); i++) {
            Log.i(TAG,"comment Id to string: " + commentObjects.get(i));
            // TODO: change this
            comments.add(Comment.getCommentFromParseObject((ParseObject) commentObjects.get(i)));
        }
        return comments;
    }

    public List<Comment> commentsFromJSONArray(JSONArray commentObjects) throws JSONException {
        List<Comment> comments = new ArrayList<>();
        Log.i(TAG,commentObjects.toString());
        for (int i = 0; i < commentObjects.length(); i++) {
            Log.i(TAG,"comment Id to string: " + commentObjects.get(i));
            // TODO: change this
            comments.add(getCommentFromParseObject((ParseObject) commentObjects.get(i)));
        }
        return comments;
    }

    public Comment getCommentFromParseObject(ParseObject parseObject) throws JSONException {
        Comment comment = new Comment();
        comment.parseComment = parseObject;
        comment.description = parseObject.getString(Comment.KEY_DESCRIPTION);
        comment.user = parseObject.getParseUser(Comment.KEY_USER);
        comment.post = this;
        comment.getUsersLikingThisFromDB();
        comment.setInitialLikeCount();
        return comment;
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
        Log.i(TAG,"Post is " + getDescription(false));
        Log.i(TAG,"did user like: " + String.valueOf(allUsersLikingThis.contains(ParseUser.getCurrentUser().getObjectId())));
        return allUsersLikingThis.contains(ParseUser.getCurrentUser().getObjectId());
    }

    public String calculateTimeAgo() {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            Date createdAt = getCreatedAt();
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
        likeCount = allUsersLikingThis.size();
    }

    public void setComments(JSONArray commentObjects) throws ParseException {
        Log.i(TAG,"setComments");
        put(KEY_COMMENTS,commentObjects);
        saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG,"error while saving after setting comments: " + e.getMessage(),e);
                }
            }
        });
    }

    public void addComment(int position, Comment comment) throws ParseException {
        allComments.add(position,comment);
        add(KEY_COMMENTS,comment.parseComment);
        save();
    }
}
