package com.research.discord.rest.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    public String id;
    
    @SerializedName("channel_id")
    public String channelId;
    
    public User author;
    public String content;
    public String timestamp;
    public int type;
    
    @Override
    public String toString() {
        return "Message{id='" + id + "', author=" + (author != null ? author.username : "null") + ", content='" + content + "'}";
    }
}
