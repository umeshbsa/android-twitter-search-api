package com.app.search.twitter.di.component;

import com.app.search.twitter.di.module.NetworkModule;
import com.app.search.twitter.di.module.TwitterModule;
import com.app.search.twitter.ui.TwitterSearchActivity;
import com.app.search.twitter.ui.TwitterSearchDetailActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * App Component : Setup Twitter and Network Module
 */
@Singleton
@Component(modules = {TwitterModule.class, NetworkModule.class})
public interface AppComponent {

    /**
     * Inject TwitterSearchActivity with App Component
     *
     * @param activity current activity instance
     */
    void inject(TwitterSearchActivity activity);

    void inject(TwitterSearchDetailActivity activity);
}
