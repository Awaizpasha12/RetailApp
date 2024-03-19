package com.example.balaji;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

import java.security.KeyStore;
import org.json.JSONObject;

public class AsyncRequestHandler {
    private static AsyncRequestHandler instance;

    private AsyncHttpClient client = new AsyncHttpClient();

    public static AsyncRequestHandler getInstance() {
        if (instance == null)
            instance = new AsyncRequestHandler();
        return instance;
    }

    public void makeRequest(JSONObject params, String apilink, Context context, final RequestListener listener) {
        StringEntity entity = null;
        MySSLSocketFactory sf=null;
        try {
            entity = new StringEntity(params.toString(),"utf-8");

            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sf = new MySSLSocketFactory(trustStore);
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.setSSLSocketFactory(sf);
        client.setTimeout(60000);
        client.post(context,apilink, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started
                //Some debugging code here
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                listener.onSuccess(statusCode, headers, response);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {

                listener.onFailure(statusCode, headers, response);


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                listener.onFailureThrowable(statusCode, headers, responseString,throwable);

            }



            @Override
            public void onRetry(int retryNo) {
            }
        });
    }
}
