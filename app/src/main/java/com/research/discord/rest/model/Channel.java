package com.research.discord.rest.model;

import com.google.gson.annotations.SerializedName;

public class Channel {
    public String id;
    public int type;
    
    @SerializedName("guild_id")
    public String guildId;
    
    public String name;
    public String topic;
    public int position;
    
    @SerializedName("parent_id")
    public String parentId;
    
    @Override
    public String toString() {
        return "Channel{id='" + id + "', name='" + name + "', type=" + type + "}";
    }
}
