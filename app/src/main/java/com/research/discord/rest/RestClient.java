package com.research.discord.rest;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestClient {
    private static final String TAG = "RestClient";
    public static final String BASE_URL = "https://discord.com/api/v10";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final String token;
    private final String superPropertiesBase64;

    public RestClient(String token) {
        this.token = token;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        this.superPropertiesBase64 = SuperProperties.getBase64Header();
    }

    private Request.Builder newRequestBuilder(String path) {
        return new Request.Builder()
                .url(BASE_URL + path)
                .header("Authorization", token)
                .header("User-Agent", "Discord-Android/126021")
                .header("X-Super-Properties", superPropertiesBase64)
                .header("X-Discord-Locale", "en-US")
                .header("Accept-Language", "en-US");
    }

    public void get(String path, Callback callback) {
        Request request = newRequestBuilder(path).get().build();
        executeRequest(request, callback);
    }

    public void post(String path, String jsonBody, Callback callback) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = newRequestBuilder(path).post(body).build();
        executeRequest(request, callback);
    }
    
    public void patch(String path, String jsonBody, Callback callback) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = newRequestBuilder(path).patch(body).build();
        executeRequest(request, callback);
    }
    
    public void delete(String path, Callback callback) {
        Request request = newRequestBuilder(path).delete().build();
        executeRequest(request, callback);
    }

    private void executeRequest(Request request, Callback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed: " + request.url(), e);
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 429) {
                    Log.w(TAG, "Rate limited on " + request.url() + 
                            " Retry-After: " + response.header("Retry-After"));
                } else if (!response.isSuccessful()) {
                    Log.e(TAG, "Request unsuccessful: " + response.code() + " " + request.url());
                }
                callback.onResponse(call, response);
            }
        });
    }
}
