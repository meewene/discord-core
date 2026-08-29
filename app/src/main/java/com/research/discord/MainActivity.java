package com.research.discord;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private DiscordClient discordClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting minimal Discord Android client...");
        
        String token = BuildConfig.DISCORD_TOKEN;
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "DISCORD_TOKEN is missing! Set it in local.properties");
            return;
        }

        discordClient = new DiscordClient(token);
        
        new Thread(() -> {
            discordClient.connect();
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (discordClient != null) {
            discordClient.disconnect();
        }
    }
}
