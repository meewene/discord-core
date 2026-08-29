package com.research.discord;

import android.util.Log;

import com.research.discord.gateway.GatewayClient;
import com.research.discord.gateway.event.EventDispatcher;
import com.research.discord.gateway.event.GatewayEvent;
import com.research.discord.rest.DiscordApiService;
import com.research.discord.rest.RestClient;

public class DiscordClient {
    private static final String TAG = "DiscordClient";

    private final String token;
    private final EventDispatcher eventDispatcher;
    private final GatewayClient gatewayClient;
    private final RestClient restClient;
    private final DiscordApiService apiService;

    public DiscordClient(String token) {
        this.token = token;
        this.eventDispatcher = new EventDispatcher();
        this.gatewayClient = new GatewayClient(token, eventDispatcher);
        this.restClient = new RestClient(token);
        this.apiService = new DiscordApiService(restClient);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        eventDispatcher.addListener(GatewayEvent.READY, (eventName, data) -> {
            Log.i(TAG, "Handling READY event");
            apiService.getCurrentUser(user -> {
                Log.i(TAG, "Fetched Current User from REST: " + user.toString());
            });
            apiService.getGuilds(guilds -> {
                Log.i(TAG, "Fetched Guilds: " + guilds.size());
            });
        });

        eventDispatcher.addListener(GatewayEvent.GUILD_CREATE, (eventName, data) -> {
            String name = data.getAsJsonObject().get("name").getAsString();
            Log.d(TAG, "GUILD_CREATE: " + name);
        });

        eventDispatcher.addListener(GatewayEvent.MESSAGE_CREATE, (eventName, data) -> {
            String content = data.getAsJsonObject().get("content").getAsString();
            String author = "Unknown";
            if (data.getAsJsonObject().has("author")) {
                 author = data.getAsJsonObject().getAsJsonObject("author").get("username").getAsString();
            }
            Log.d(TAG, "MESSAGE_CREATE: [" + author + "] " + content);
        });
    }

    public void connect() {
        Log.i(TAG, "Starting DiscordClient...");
        gatewayClient.connect();
    }

    public void disconnect() {
        Log.i(TAG, "Stopping DiscordClient...");
        gatewayClient.disconnect();
    }
}
