package com.research.discord.gateway;

import android.util.Log;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HeartbeatManager {
    private static final String TAG = "HeartbeatManager";

    private final GatewayClient client;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> heartbeatTask;
    
    private long heartbeatInterval;
    private boolean ackReceived = true; // Initially true to allow the first beat
    private final Random random = new Random();

    public HeartbeatManager(GatewayClient client) {
        this.client = client;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start(long interval) {
        this.heartbeatInterval = interval;
        this.ackReceived = true; // reset
        
        long firstDelay = (long) (interval * random.nextDouble());
        Log.d(TAG, "Starting heartbeat. Interval=" + interval + "ms, Jitter delay=" + firstDelay + "ms");

        heartbeatTask = executor.scheduleAtFixedRate(this::doHeartbeat, firstDelay, interval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(true);
            heartbeatTask = null;
        }
        Log.d(TAG, "Stopped heartbeat");
    }
    
    public void onHeartbeatAck() {
        ackReceived = true;
        Log.d(TAG, "Heartbeat ACK received");
    }
    
    public void acknowledgeManually() {
        ackReceived = true;
    }

    private void doHeartbeat() {
        if (!ackReceived) {
            Log.w(TAG, "Zombie connection detected! No ACK received since last heartbeat. Reconnecting...");
            client.reconnect(true);
            return;
        }

        ackReceived = false;
        Log.d(TAG, "Sending Heartbeat...");
        client.sendHeartbeat();
    }
}
