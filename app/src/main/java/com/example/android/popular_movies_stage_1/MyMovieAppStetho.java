package com.example.android.popular_movies_stage_1;

import android.app.Application;

import com.facebook.stetho.Stetho;

// Got help from @Aaron Quaday From Slack!

public class MyMovieAppStetho extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
