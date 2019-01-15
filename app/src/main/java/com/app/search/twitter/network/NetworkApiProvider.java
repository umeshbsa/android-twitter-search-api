package com.app.search.twitter.network;

import android.content.Context;

import com.app.search.twitter.utils.AppUtils;

public final class NetworkApiProvider implements NetworkApi {
    public NetworkApiProvider() {
    }

    /**
     * Check network provider
     *
     * @param context
     * @return is network available. true - available
     */
    @Override
    public boolean isConnectedToInternet(Context context) {
        return AppUtils.isConnection(context);
    }


}
