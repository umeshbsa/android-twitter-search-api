package com.app.search.twitter.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.app.search.twitter.R;
import com.app.search.twitter.app.TwitterApp;

import butterknife.ButterKnife;

/**
 * Set up Twitter search UI with recycle view, search on toolbar
 */
public final class TwitterSearchDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Init App Component of TwitterSearchActivity
     */
    private void initInjections() {
        ButterKnife.bind(this);
        ((TwitterApp) getApplication()).getComponent().inject(this);
    }
}