package com.codepath.instagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codepath.instagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.parceler.Parcels;

public class ChangePicFragment extends ComposeFragment {

    public static final String TAG = "ChangePicFragment";

    ParseUser user;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Parcels.unwrap(getArguments().getParcelable(Post.KEY_USER));
        Log.i(TAG,user.getUsername());
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvFragTitle.setText("Update Profile Pic");
        binding.btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"onCLicked");
                launchCamera();
            }
        });
        binding.etDescription.setVisibility(View.GONE);
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoFile == null || binding.ivPostPhoto.getDrawable() == null) {
                    Toast.makeText(getContext(), "Image cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    savePicChange();
                } catch (ParseException e) {
                    Log.e(TAG,"Error saving pic change: " + e.getMessage(),e);
                }
                getActivity().onBackPressed();
            }
        });
    }

    protected void savePicChange() throws ParseException {
        binding.pbLoading.setVisibility(View.VISIBLE);
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put(Post.KEY_USER_PROFILE,new ParseFile(photoFile));
        currentUser.save();
        binding.pbLoading.setVisibility(View.GONE);
    }
}
