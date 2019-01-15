package com.app.search.twitter.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AppUtils {

    /**
     * Check for network
     *
     * @param context
     * @return true -available
     */
    public static boolean isConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        boolean isnewtwork = (activeNetworkInfo != null && activeNetworkInfo.isConnected());
        if (!isnewtwork) {
            // ValidationManager.showDialog(context, "Your internet connection has been lost.");
        }
        return isnewtwork;
    }
}
