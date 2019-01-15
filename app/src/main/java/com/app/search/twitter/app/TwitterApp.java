package com.app.search.twitter.app;

import android.app.Application;

import com.app.search.twitter.di.component.AppComponent;
import com.app.search.twitter.di.component.DaggerAppComponent;
import com.app.search.twitter.di.module.NetworkModule;
import com.app.search.twitter.di.module.TwitterModule;


public final class TwitterApp extends Application {
    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        buildApplicationComponent();
    }

    /**
     * Integrate Twitter and Network Module
     */
    private void buildApplicationComponent() {
        component = DaggerAppComponent.builder()
                .twitterModule(new TwitterModule())
                .networkModule(new NetworkModule())
                .build();
    }


    /**
     * Find app component
     *
     * @return App component
     */
    public AppComponent getComponent() {
        return component;
    }
}
