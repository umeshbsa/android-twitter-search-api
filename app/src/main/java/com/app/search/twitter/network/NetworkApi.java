package com.app.search.twitter.network;

import android.content.Context;

/**
 * Network Api : Check the network implementation
 */
public interface NetworkApi {
    boolean isConnectedToInternet(Context context);
}
