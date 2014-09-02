/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.content;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class JsonLoader extends AsyncTaskLoader<JSONObject> {

    private final HttpClient mHttpClient;
    private final String mUri;
    private JSONObject mData;

    public JsonLoader(Context context, HttpClient httpClient, String uri) {
        super(context);
        mHttpClient = httpClient;
        mUri = uri;
    }

    public HttpClient getHttpClient() {
        return mHttpClient;
    }

    @Override
    public JSONObject loadInBackground() {
        HttpResponse response;
        try {
            response = mHttpClient.execute(new HttpGet(mUri));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            return null;
        }

        try {
            String a = EntityUtils.toString(response.getEntity(), "UTF-8");
            return new JSONObject(a);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deliverResult(JSONObject data) {
        if (isReset()) {
            return;
        }
        mData = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
    }
}
