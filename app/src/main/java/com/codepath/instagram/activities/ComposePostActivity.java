package com.codepath.instagram.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.codepath.instagram.R;

public class ComposePostActivity extends AppCompatActivity {

    public static final String TAG = "ComposePostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_post);
    }
}