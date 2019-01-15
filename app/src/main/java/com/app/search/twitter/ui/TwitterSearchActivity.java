package com.app.search.twitter.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.search.twitter.R;
import com.app.search.twitter.adapter.TwitterSearchAdapter;
import com.app.search.twitter.app.TwitterApp;
import com.app.search.twitter.network.NetworkApi;
import com.app.search.twitter.twitter.TwitterApi;
import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Set up Twitter search UI with recycle view, search on toolbar
 */
public final class TwitterSearchActivity extends AppCompatActivity {
    @BindView(R.id.recycler_view_tweets)
    public RecyclerView recyclerViewTweets;
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @BindView(R.id.search_view)
    public MaterialSearchView searchView;
    @BindView(R.id.message_container)
    public LinearLayout messageContainerLayout;
    @BindView(R.id.iv_message_container_image)
    public ImageView imageViewMessage;
    @BindView(R.id.tv_message_container_text)
    public TextView textViewMessage;
    @BindView(R.id.pb_loading_more_tweets)
    public ProgressBar progressLoadingMoreTweets;
    @BindString(R.string.no_internet_connection)
    public String msgNoInternetConnection;
    @BindString(R.string.cannot_load_more_tweets)
    public String msgCannotLoadMoreTweets;
    @BindString(R.string.no_tweets)
    public String msgNoTweets;
    @BindString(R.string.no_tweets_formatted)
    public String msgNoTweetsFormatted;
    @BindString(R.string.searched_formatted)
    public String msgSearchedFormatted;
    @BindString(R.string.api_rate_limit_exceeded)
    public String msgApiRateLimitExceeded;
    @BindString(R.string.error_during_search)
    public String msgErrorDuringSearch;
    /**
     * Twitter api
     */
    @Inject
    protected TwitterApi twitterApi;
    /**
     * Network api
     */
    @Inject
    protected NetworkApi networkApi;
    private String lastKeyword = "";
    private LinearLayoutManager layoutManager;
    /**
     * Search delay
     */
    private Subscription subDelayedSearch;
    /**
     * Search twitter
     */
    private Subscription subSearchTweets;
    /**
     * Search for load more
     */
    private Subscription subLoadMoreTweets;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInjections();
        initRecyclerView();
        setSupportActionBar(toolbar);
        initSearchView();
        setErrorMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    /**
     * Init App Component of TwitterSearchActivity
     */
    private void initInjections() {
        ButterKnife.bind(this);
        ((TwitterApp) getApplication()).getComponent().inject(this);
    }

    /**
     * Init recycle view and setup load more listner
     */
    private void initRecyclerView() {
        recyclerViewTweets.setHasFixedSize(true);
        recyclerViewTweets.setAdapter(new TwitterSearchAdapter(this, new LinkedList<Status>()));
        layoutManager = new LinearLayoutManager(this);
        recyclerViewTweets.setLayoutManager(layoutManager);
        recyclerViewTweets.addOnScrollListener(createInfiniteScrollListener());
    }

    @NonNull
    private InfiniteScrollListener createInfiniteScrollListener() {
        return new InfiniteScrollListener(twitterApi.getMaxTweetsPerRequest(), layoutManager) {
            @Override
            public void onScrolledToEnd(final int firstVisibleItemPosition) {
                if (subLoadMoreTweets != null && !subLoadMoreTweets.isUnsubscribed()) {
                    return;
                }

                final long lastTweetId = ((TwitterSearchAdapter) recyclerViewTweets.getAdapter()).getLastTweetId();

                subLoadMoreTweets = twitterApi.searchTweets(lastKeyword, lastTweetId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<Status>>() {
                            @Override
                            public void onStart() {
                                progressLoadingMoreTweets.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCompleted() {
                                progressLoadingMoreTweets.setVisibility(View.GONE);
                                unsubscribe();
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (!networkApi.isConnectedToInternet(TwitterSearchActivity.this)) {
                                    showSnackBar(msgNoInternetConnection);
                                } else {
                                    showSnackBar(msgCannotLoadMoreTweets);
                                }
                                progressLoadingMoreTweets.setVisibility(View.GONE);
                            }

                            @Override
                            public void onNext(List<Status> newTweets) {
                                final TwitterSearchAdapter newAdapter = createNewTweetsAdapter(newTweets);
                                refreshView(recyclerViewTweets, newAdapter, firstVisibleItemPosition);
                            }
                        });
            }
        };
    }

    @NonNull
    private TwitterSearchAdapter createNewTweetsAdapter(List<Status> newTweets) {
        final TwitterSearchAdapter adapter = (TwitterSearchAdapter) recyclerViewTweets.getAdapter();
        final List<Status> oldTweets = adapter.getTweets();
        final List<Status> tweets = new LinkedList<>();
        tweets.addAll(oldTweets);
        tweets.addAll(newTweets);
        return new TwitterSearchAdapter(TwitterSearchActivity.this, tweets);
    }

    private void initSearchView() {
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.search_view_cursor);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchTweets(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTweetsWithDelay(newText);
                return false;
            }
        });
    }

    private void setErrorMessage() {
        if (networkApi.isConnectedToInternet(this)) {
            showErrorMessageContainer(msgNoTweets, R.drawable.no_tweets);
        } else {
            showErrorMessageContainer(msgNoInternetConnection, R.drawable.error);
        }
    }

    private void searchTweetsWithDelay(final String keyword) {
        safelyUnsubscribe(subDelayedSearch);

        if (!twitterApi.canSearchTweets(keyword)) {
            return;
        }

        // we are creating this delay to let user provide keyword
        // and omit not necessary requests
        subDelayedSearch = Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long milliseconds) {
                        searchTweets(keyword);
                    }
                });
    }

    private void searchTweets(final String keyword) {
        safelyUnsubscribe(subDelayedSearch, subLoadMoreTweets, subSearchTweets);
        lastKeyword = keyword;

        if (!networkApi.isConnectedToInternet(this)) {
            showSnackBar(msgNoInternetConnection);
            return;
        }

        if (!twitterApi.canSearchTweets(keyword)) {
            return;
        }

        subSearchTweets = twitterApi.searchTweets(keyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Status>>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onCompleted() {
                        // we don't have to implement this method
                    }

                    @Override
                    public void onError(final Throwable e) {
                        final String message = getErrorMessage((TwitterException) e);
                        showSnackBar(message);
                        showErrorMessageContainer(message, R.drawable.no_tweets);
                    }

                    @Override
                    public void onNext(final List<Status> tweets) {
                        handleSearchResults(tweets, keyword);
                    }
                });
    }

    @NonNull
    private String getErrorMessage(final TwitterException e) {
        if (e.getErrorCode() == twitterApi.getApiRateLimitExceededErrorCode()) {
            return msgApiRateLimitExceeded;
        }
        return msgErrorDuringSearch;
    }

    private void handleSearchResults(final List<Status> tweets, final String keyword) {
        if (tweets.isEmpty()) {
            final String message = String.format(msgNoTweetsFormatted, keyword);
            showSnackBar(message);
            showErrorMessageContainer(message, R.drawable.no_tweets);
            return;
        }

        final TwitterSearchAdapter adapter = new TwitterSearchAdapter(TwitterSearchActivity.this, tweets);
        recyclerViewTweets.setAdapter(adapter);
        recyclerViewTweets.invalidate();
        recyclerViewTweets.setVisibility(View.VISIBLE);
        messageContainerLayout.setVisibility(View.GONE);
        final String message = String.format(msgSearchedFormatted, keyword);
        showSnackBar(message);
    }

    private void showSnackBar(final String message) {
        final View containerId = findViewById(R.id.container);
        Snackbar.make(containerId, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        safelyUnsubscribe(subDelayedSearch, subSearchTweets, subLoadMoreTweets);
    }

    private void safelyUnsubscribe(final Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }

    private void showErrorMessageContainer(final String message, final int imageResourceId) {
        recyclerViewTweets.setVisibility(View.GONE);
        messageContainerLayout.setVisibility(View.VISIBLE);
        imageViewMessage.setImageResource(imageResourceId);
        textViewMessage.setText(message);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
}