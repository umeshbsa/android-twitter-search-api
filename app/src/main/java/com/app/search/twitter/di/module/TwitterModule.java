package com.app.search.twitter.di.module;

import com.app.search.twitter.twitter.TwitterApi;
import com.app.search.twitter.twitter.TwitterApiProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Twitter Module : Used for twitter search api
 */
@Module
public final class TwitterModule {
    @Provides
    @Singleton
    public TwitterApi provideTwitterApi() {
        return new TwitterApiProvider();
    }
}
