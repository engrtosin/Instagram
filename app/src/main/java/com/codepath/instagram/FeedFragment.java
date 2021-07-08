package com.codepath.instagram;

import android.os.Parcelable;

import androidx.fragment.app.Fragment;

public abstract class FeedFragment extends Fragment {

    protected FeedFragmentInterface listener;

    public interface FeedFragmentInterface {
        public void goToActivity(Class activityClass, Parcelable extraInfo);
        public void goToFragment(FeedFragment toFragment, Parcelable extraInfo);
    }

    public void setListener(FeedFragmentInterface listener) {
        this.listener = listener;
    }
}
