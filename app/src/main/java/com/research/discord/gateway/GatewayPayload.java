package com.research.discord.gateway;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class GatewayPayload {
    @SerializedName("op")
    public int op;

    @SerializedName("d")
    public JsonElement d;

    @SerializedName("s")
    public Integer s;

    @SerializedName("t")
    public String t;
}
