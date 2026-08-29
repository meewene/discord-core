package com.research.discord.rest.model;

import com.google.gson.annotations.SerializedName;

public class Guild {
    public String id;
    public String name;
    public String icon;
    
    @SerializedName("owner_id")
    public String ownerId;
    
    public String permissions;
    
    @SerializedName("member_count")
    public int memberCount;
    
    @Override
    public String toString() {
        return "Guild{id='" + id + "', name='" + name + "'}";
    }
}
