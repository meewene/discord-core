package com.research.discord.rest;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class SuperProperties {
    
    @SerializedName("os")
    public String os = "Android";
    
    @SerializedName("browser")
    public String browser = "Discord Android";
    
    @SerializedName("device")
    public String device = "Pixel, oriole";
    
    @SerializedName("system_locale")
    public String systemLocale = "en-US";
    
    @SerializedName("client_version")
    public String clientVersion = "126.21 - Stable";
    
    @SerializedName("client_build_number")
    public int clientBuildNumber = 126021;
    
    @SerializedName("os_version")
    public String osVersion = "12";
    
    @SerializedName("os_sdk_version")
    public String osSdkVersion = "32";
    
    @SerializedName("client_performance_cpu")
    public int clientPerformanceCpu = 56;
    
    @SerializedName("client_performance_memory")
    public int clientPerformanceMemory = 400000;
    
    @SerializedName("cpu_core_count")
    public int cpuCoreCount = 8;
    
    @SerializedName("accessibility_support_enabled")
    public boolean accessibilitySupportEnabled = false;
    
    @SerializedName("accessibility_features")
    public int accessibilityFeatures = 128;
    
    public static String getBase64Header() {
        SuperProperties props = new SuperProperties();
        String json = new Gson().toJson(props);
        return Base64.encodeToString(json.getBytes(), Base64.NO_WRAP);
    }
}
