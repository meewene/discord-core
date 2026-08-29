package com.research.discord.gateway.event;

import com.google.gson.JsonElement;

public interface EventListener {
    void onEvent(String eventName, JsonElement data);
}
