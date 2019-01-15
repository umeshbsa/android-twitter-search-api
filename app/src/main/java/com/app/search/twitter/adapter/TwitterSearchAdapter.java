package com.app.search.twitter.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.search.twitter.R;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import twitter4j.Status;

public final class TwitterSearchAdapter extends RecyclerView.Adapter<TwitterSearchAdapter.ViewHolder> {
    private static final String LOGIN_FORMAT = "@%s";
    private final Context context;
    private final List<Status> tweets;

    public TwitterSearchAdapter(final Context context, final List<Status> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final Context context = parent.getContext();
        final View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Status tweet = tweets.get(position);
        Picasso.with(context).load(tweet.getUser().getProfileImageURL()).into(holder.ivAvatar);
        holder.tvName.setText(tweet.getUser().getName());
        final String formattedLogin = String.format(LOGIN_FORMAT, tweet.getUser().getScreenName());

        holder.tvLogin.setText(formattedLogin);

        holder.tvDate.setText(tweet.getCreatedAt().toString());

        holder.tvMessage.setText(tweet.getText());
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public long getLastTweetId() {
        final Status tweet = tweets.get(getItemCount() - 1);
        return tweet.getId();
    }

    public List<Status> getTweets() {
        return Collections.unmodifiableList(tweets);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView ivAvatar;
        protected TextView tvName;
        protected TextView tvLogin;
        protected TextView tvDate;
        protected TextView tvMessage;

        public ViewHolder(final View itemView) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvLogin = (TextView) itemView.findViewById(R.id.tv_login);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
        }
    }
}
