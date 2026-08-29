package com.research.discord.gateway;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.research.discord.gateway.event.EventDispatcher;
import com.research.discord.gateway.event.GatewayEvent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class GatewayClient extends WebSocketListener {
    private static final String TAG = "GatewayClient";
    private static final String GATEWAY_URL = "wss://gateway.discord.gg/?encoding=json&v=10&compress=zlib-stream";
    
    // Capabilities Bitfield flags (mimicking Android client capabilities)
    private static final int CAPABILITIES = 1 | 2 | 4 | 8 | 16 | 32 | 64 | 128 | 256 | 512 | 1024 | 2048;

    private final String token;
    private final EventDispatcher eventDispatcher;
    private final Gson gson;
    private final OkHttpClient httpClient;

    private WebSocket webSocket;
    private ZlibDecompressor decompressor;
    private HeartbeatManager heartbeatManager;

    private Integer sequenceNumber = null;
    private String sessionId = null;
    private boolean isReconnecting = false;

    public GatewayClient(String token, EventDispatcher eventDispatcher) {
        this.token = token;
        this.eventDispatcher = eventDispatcher;
        this.gson = new Gson();
        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for websockets
                .build();
        this.heartbeatManager = new HeartbeatManager(this);
    }

    public void connect() {
        Log.d(TAG, "Connecting to Gateway...");
        decompressor = new ZlibDecompressor(); // Fresh inflator for each connection
        Request request = new Request.Builder().url(GATEWAY_URL).build();
        webSocket = httpClient.newWebSocket(request, this);
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from Gateway...");
        heartbeatManager.stop();
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnecting");
            webSocket = null;
        }
    }

    public void reconnect(boolean resume) {
        Log.d(TAG, "Reconnecting... Resume=" + resume);
        disconnect();
        
        if (!resume) {
            sessionId = null;
            sequenceNumber = null;
        }
        
        isReconnecting = true;
        // Simple backoff could be added here
        connect();
    }

    void sendHeartbeat() {
        GatewayPayload payload = new GatewayPayload();
        payload.op = OpCode.HEARTBEAT;
        payload.d = sequenceNumber == null ? null : gson.toJsonTree(sequenceNumber);
        sendPayload(payload);
    }

    private void sendIdentify() {
        Log.d(TAG, "Sending IDENTIFY");
        
        JsonObject properties = new JsonObject();
        properties.addProperty("os", "Android");
        properties.addProperty("browser", "Discord Android");
        properties.addProperty("device", "Pixel, oriole");

        JsonObject clientState = new JsonObject();
        clientState.add("guild_hashes", new JsonObject());
        clientState.addProperty("highest_last_message_id", "0");
        clientState.addProperty("read_state_version", 0);
        clientState.addProperty("user_guild_settings_version", -1);
        clientState.addProperty("user_settings_version", -1);

        JsonObject d = new JsonObject();
        d.addProperty("token", token);
        d.addProperty("capabilities", CAPABILITIES);
        d.addProperty("compress", false);
        d.add("properties", properties);
        d.add("client_state", clientState);

        GatewayPayload payload = new GatewayPayload();
        payload.op = OpCode.IDENTIFY;
        payload.d = d;
        sendPayload(payload);
    }

    private void sendResume() {
        Log.d(TAG, "Sending RESUME");
        JsonObject d = new JsonObject();
        d.addProperty("token", token);
        d.addProperty("session_id", sessionId);
        d.addProperty("seq", sequenceNumber);

        GatewayPayload payload = new GatewayPayload();
        payload.op = OpCode.RESUME;
        payload.d = d;
        sendPayload(payload);
    }

    private void sendPayload(GatewayPayload payload) {
        if (webSocket != null) {
            String json = gson.toJson(payload);
            // Log.d(TAG, "Sending: " + json);
            webSocket.send(json);
        }
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        Log.d(TAG, "WebSocket opened");
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        handleJsonMessage(text);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        try {
            String decompressed = decompressor.decompress(bytes);
            if (decompressed != null) {
                handleJsonMessage(decompressed);
            }
        } catch (Exception e) {
            Log.e(TAG, "Decompression error", e);
            reconnect(true);
        }
    }

    private void handleJsonMessage(String json) {
        // Log.d(TAG, "Received: " + json);
        GatewayPayload payload = gson.fromJson(json, GatewayPayload.class);
        
        if (payload.s != null) {
            sequenceNumber = payload.s;
        }

        switch (payload.op) {
            case OpCode.HELLO:
                long interval = payload.d.getAsJsonObject().get("heartbeat_interval").getAsLong();
                heartbeatManager.start(interval);
                
                if (isReconnecting && sessionId != null) {
                    sendResume();
                } else {
                    sendIdentify();
                }
                isReconnecting = false;
                break;
                
            case OpCode.HEARTBEAT_ACK:
                heartbeatManager.onHeartbeatAck();
                break;
                
            case OpCode.HEARTBEAT:
                sendHeartbeat();
                break;
                
            case OpCode.INVALID_SESSION:
                boolean resumable = payload.d.getAsBoolean();
                Log.w(TAG, "Invalid Session! Resumable: " + resumable);
                // Give a short delay as recommended by docs, though omitting for pure minimal implementation is okay, 
                // just reconnect directly
                try { Thread.sleep(2000); } catch (Exception ignored) {}
                if (resumable) {
                    reconnect(true);
                } else {
                    reconnect(false);
                }
                break;
                
            case OpCode.RECONNECT:
                Log.w(TAG, "Gateway requested reconnect");
                reconnect(true);
                break;
                
            case OpCode.DISPATCH:
                handleDispatch(payload.t, payload.d);
                break;
                
            default:
                Log.w(TAG, "Unhandled OpCode: " + payload.op);
                break;
        }
    }

    private void handleDispatch(String eventName, JsonElement data) {
        if (GatewayEvent.READY.equals(eventName)) {
            sessionId = data.getAsJsonObject().get("session_id").getAsString();
            Log.i(TAG, "READY! Session ID: " + sessionId);
        }
        
        eventDispatcher.dispatch(eventName, data);
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        Log.i(TAG, "WebSocket closed: Code=" + code + " Reason=" + reason);
        heartbeatManager.stop();
        if (CloseCode.canReconnect(code)) {
            reconnect(true);
        } else {
            Log.e(TAG, "Fatal close code, cannot reconnect.");
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
        Log.e(TAG, "WebSocket failure", t);
        heartbeatManager.stop();
        reconnect(true);
    }
}
