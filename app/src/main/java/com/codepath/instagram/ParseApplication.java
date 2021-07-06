package com.codepath.instagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("gTZXI48xjm2zjDpyIMQJnbPl3Cqqz6sjjuTawMN4")
                .clientKey("w5DE1LjPBzuYKOoPB55t5MmGqGjeaPCyHlIK7AhM")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
