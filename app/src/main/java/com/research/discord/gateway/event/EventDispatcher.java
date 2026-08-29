package com.research.discord.gateway.event;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventDispatcher {
    private final Map<String, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private final List<EventListener> catchAllListeners = new CopyOnWriteArrayList<>();

    public void addListener(String eventName, EventListener listener) {
        listeners.computeIfAbsent(eventName, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void removeListener(String eventName, EventListener listener) {
        List<EventListener> list = listeners.get(eventName);
        if (list != null) {
            list.remove(listener);
        }
    }

    public void addCatchAllListener(EventListener listener) {
        catchAllListeners.add(listener);
    }

    public void removeCatchAllListener(EventListener listener) {
        catchAllListeners.remove(listener);
    }

    public void dispatch(String eventName, JsonElement data) {
        List<EventListener> list = listeners.get(eventName);
        if (list != null) {
            for (EventListener listener : list) {
                listener.onEvent(eventName, data);
            }
        }
        for (EventListener listener : catchAllListeners) {
            listener.onEvent(eventName, data);
        }
    }
}
