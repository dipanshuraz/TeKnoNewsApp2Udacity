package com.example.android.teknonewsapp;

import android.text.TextUtils;
import android.util.Log;

import com.example.android.teknonewsapp.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving articles from the Guardian API.
 */

public final class QueryUtils {

    // Constant fields for json parsing
    private static final String LOG_TAG = QueryUtils.class.getName();
    private static final String TAG_RESPONSE = "response";
    private static final String TAG_RESULTS = "results";
    private static final String TAG_SECTIONNAME = "sectionName";
    private static final String TAG_WEBPUBLICATIONDATE = "webPublicationDate";
    private static final String TAG_WEBURL = "webUrl";
    private static final String TAG_FIELDS = "fields";
    private static final String TAG_HEADLINE = "headline";
    private static final String TAG_THUMBNAIL = "thumbnail";
    private static final String TAG_TAGS = "tags";
    private static final String TAG_WEBTITLE = "webTitle";


    private QueryUtils() {
    }

    public static List<Article> fetchArticleData(String requestUrl) {

        Log.i(LOG_TAG, "Test FetchArticleData() called");

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        List<Article> articles = extractFeatureFromJSON(jsonResponse);
        return articles;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the article JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    public static List<Article> extractFeatureFromJSON(String articlesJSON) {

        if (TextUtils.isEmpty(articlesJSON)) {
            return null;
        }

        List<Article> articles = new ArrayList<>();

        try {

            JSONObject root = new JSONObject(articlesJSON);
            JSONObject responseObject = root.getJSONObject(TAG_RESPONSE);
            JSONArray resultsArray = responseObject.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject data = resultsArray.getJSONObject(i);
                String sectionName = data.getString(TAG_SECTIONNAME);
                String webPublicationDate = data.getString(TAG_WEBPUBLICATIONDATE);
                String webUrl = data.getString(TAG_WEBURL);
                JSONObject fields = data.getJSONObject(TAG_FIELDS);
                String headline = fields.getString(TAG_HEADLINE);

                String thumbnail = null;

                try {
                    thumbnail = fields.getString(TAG_THUMBNAIL);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "No thumbnail image received", e);
                }

                JSONArray tagsArray = data.getJSONArray(TAG_TAGS);

                String author = null;

                if (tagsArray.length() > 0) {
                    JSONObject authorObject = tagsArray.getJSONObject(0);

                    try {
                        author = authorObject.getString(TAG_WEBTITLE);

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error retrieving authors details", e);
                    }
                }

                Article article = new Article(headline, thumbnail, author, webUrl, sectionName, webPublicationDate);
                articles.add(article);
            }

        } catch (JSONException e) {

            Log.e("QueryUtils", "Problem parsing the article JSON results", e);
        }
        return articles;
    }
}