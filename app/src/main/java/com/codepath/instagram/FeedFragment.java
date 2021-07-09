package com.codepath.instagram;

import android.content.Intent;
import android.os.Parcelable;

import androidx.fragment.app.Fragment;

import com.codepath.instagram.activities.FeedActivity;
import com.codepath.instagram.activities.LoginActivity;

public abstract class FeedFragment extends Fragment {

    protected FeedFragmentInterface listener;

    public interface FeedFragmentInterface {
        void goToActivity(Class activityClass, Parcelable extraInfo);
        void goToFragment(FeedFragment toFragment, Parcelable extraInfo);

        void reloadPage();
    }

    public void setListener(FeedFragmentInterface listener) {
        this.listener = listener;
    }

    protected void goLoginActivity() {
        // TODO: Change this to use an interface
        Intent i = new Intent(getActivity(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}
