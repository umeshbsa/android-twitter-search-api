package com.app.search.twitter.di.module;

import com.app.search.twitter.network.NetworkApi;
import com.app.search.twitter.network.NetworkApiProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Network module : Used for check network
 */
@Module
public final class NetworkModule {
    @Provides
    @Singleton
    public NetworkApi provideNetworkApi() {
        return new NetworkApiProvider();
    }
}
