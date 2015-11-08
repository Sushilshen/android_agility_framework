package org.pinwheel.agility.net;

import android.util.Log;

import org.pinwheel.agility.net.parser.IDataParser;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (C), 2015 <br>
 * <br>
 * All rights reserved <br>
 * <br>
 *
 * @author dnwang
 */
public class HttpConnectionAgent implements HttpClientAgent {
    private static final String TAG = OkHttpAgent.class.getSimpleName();

    private ExecutorService executor;

    public HttpConnectionAgent(){
        executor = Executors.newCachedThreadPool();
    }

    public HttpConnectionAgent(int parallelSize) {
        executor = Executors.newFixedThreadPool(parallelSize);
    }

    protected void convert(Request request, HttpURLConnection connection) throws Exception {
        // method
        connection.setRequestMethod(request.getMethod().toUpperCase());
        // header
        Set<Map.Entry<String, String>> set = request.getHeaders().entrySet();
        for (Map.Entry<String, String> entry : set) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        // time out
        connection.setConnectTimeout(request.getTimeout() * 1000);

//        connection.setDoInput(true);
//        connection.setDoOutput(true);
    }

    @Override
    public void enqueue(final Request request) {
        if (request == null) {
            if (debug) {
                Log.e(TAG, "Request must not null !");
            }
            return;
        }

        final OnRequestAdapter callback = request.getRequestListener();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    if (callback.onRequestPrepare(request)) {
                        // no need handle continue
                        return;
                    }
                }
                // get response
                HttpURLConnection connection = null;
                try {
                    // set url
                    URL url = new URL(request.getUrlByMethod());
                    connection = (HttpURLConnection) url.openConnection();
                    convert(request, connection);
                    connection.connect();
                    // set body
                    if (request.getBody() != null) {
                        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                        dataOutputStream.write(request.getBody());
                        dataOutputStream.flush();
                        dataOutputStream.close();
                    }
                    int code = connection.getResponseCode();
                    String message = connection.getResponseMessage();
                    if (callback != null) {
                        if (callback.onRequestResponse(connection)) {
                            // no need handle continue
                            connection.disconnect();
                            return;
                        }
                    }
                    if (code != HttpURLConnection.HTTP_OK) {
                        throw new IllegalStateException("Response code: " + code + "; message: " + message);
                    }
                } catch (Exception e) {
                    if (debug) {
                        e.printStackTrace();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (callback != null) {
                        callback.dispatchOnError(e);
                    }
                    // break; request error
                    return;
                }
                // parse
                IDataParser parser = request.getResponseParser();
                if (parser == null) {
                    connection.disconnect();
                    if (callback != null) {
                        callback.dispatchOnSuccess(null);
                    }
                    // break; no need parse
                    return;
                }
                try {
                    parser.parse(connection.getInputStream());
                    if (callback != null) {
                        callback.dispatchOnSuccess(parser.getResult());
                    }
                } catch (Exception e) {
                    if (debug) {
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        callback.dispatchOnError(e);
                    }
                } finally {
                    connection.disconnect();
                }
            }
        });
    }

    @Override
    public void parallelExecute(Request... requests) {
        if (requests == null || requests.length == 0) {
            return;
        }
        for (Request request : requests) {
            enqueue(request);
        }
    }

    @Override
    public void cancel(Object... tags) {
        if (debug) {
            Log.e(TAG, HttpConnectionAgent.class.getSimpleName() + " not support cancel.");
        }
    }

    @Override
    public void release() {
        executor.shutdown();
        executor = null;
    }

}
