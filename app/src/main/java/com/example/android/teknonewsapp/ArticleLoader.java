package com.example.android.teknonewsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.example.android.teknonewsapp.Article;
import com.example.android.teknonewsapp.QueryUtils;

import java.util.List;

public class ArticleLoader extends AsyncTaskLoader<List<Article>> {


    private String url;
    private static final String LOG_TAG = ArticleLoader.class.getName();

    public ArticleLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    public List<Article> loadInBackground() {

        Log.i(LOG_TAG, "Test loadinBackground() called");

        if (url == null) {
            return null;
        }
        
        List<Article> articles = QueryUtils.fetchArticleData(url);
        return articles;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
