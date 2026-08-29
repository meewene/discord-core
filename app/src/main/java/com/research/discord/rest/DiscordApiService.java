package com.research.discord.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.research.discord.rest.model.Channel;
import com.research.discord.rest.model.Guild;
import com.research.discord.rest.model.Message;
import com.research.discord.rest.model.User;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DiscordApiService {
    private static final String TAG = "DiscordApiService";
    private final RestClient restClient;
    private final Gson gson;

    public DiscordApiService(RestClient restClient) {
        this.restClient = restClient;
        this.gson = new Gson();
    }

    public void getCurrentUser(Consumer<User> onSuccess) {
        restClient.get("/users/@me", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    User user = gson.fromJson(response.body().charStream(), User.class);
                    onSuccess.accept(user);
                }
            }
        });
    }

    public void getGuilds(Consumer<List<Guild>> onSuccess) {
        restClient.get("/users/@me/guilds", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    List<Guild> guilds = gson.fromJson(response.body().charStream(), 
                            new TypeToken<List<Guild>>(){}.getType());
                    onSuccess.accept(guilds);
                }
            }
        });
    }

    public void getChannels(String guildId, Consumer<List<Channel>> onSuccess) {
        restClient.get("/guilds/" + guildId + "/channels", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    List<Channel> channels = gson.fromJson(response.body().charStream(), 
                            new TypeToken<List<Channel>>(){}.getType());
                    onSuccess.accept(channels);
                }
            }
        });
    }
    
    public void sendMessage(String channelId, String content) {
        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        
        restClient.post("/channels/" + channelId + "/messages", gson.toJson(body), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Send message response: " + response.code());
            }
        });
    }
}
