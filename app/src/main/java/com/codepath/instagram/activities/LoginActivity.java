package com.codepath.instagram.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.instagram.adapters.PostsAdapter;
import com.codepath.instagram.databinding.ActivityLoginBinding;
import com.codepath.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONException;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    ActivityLoginBinding binding;
    public static ParseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if (ParseUser.getCurrentUser() != null) {
            currentUser = ParseUser.getCurrentUser();
            queryCurrentUser();
            goFeedActivity();
        }

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();
                loginUser(username,password);
            }
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();
                signUpUser(username,password);
            }
        });
    }

    private void queryCurrentUser() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.include(PostsAdapter.USER_PIC_KEY);
        Log.i(TAG,"current user id: " + currentUser.getObjectId());
        query.whereEqualTo("objectId",currentUser.getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error getting current user: " + e.getMessage(),e);
                    return;
                }
                currentUser = objects.get(0);
            }
        });
    }

    private void signUpUser(String username,String password) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG,"Successful sign up");
                    Toast.makeText(LoginActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                    loginUser(username,password);
                    // Hooray! Let them use the app now.
                } else {
                    Log.i(TAG,"Failed sign up");
                    Toast.makeText(LoginActivity.this, "Error signing up!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Trying to login in user: " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    // TODO: Notify the user of the error
                    Log.e(TAG,"Login error occured",e);
                    return;
                }
                currentUser = ParseUser.getCurrentUser();
                queryCurrentUser();
                goFeedActivity();
                Toast.makeText(LoginActivity.this,"Successful!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goFeedActivity() {
        Intent i = new Intent(this, FeedActivity.class);
        startActivity(i);
        finish();
    }
}