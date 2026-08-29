package com.research.discord.rest.model;

import com.google.gson.annotations.SerializedName;

public class User {
    public String id;
    public String username;
    public String discriminator;
    
    @SerializedName("global_name")
    public String globalName;
    
    public String avatar;
    public boolean bot;
    public String email;
    public boolean verified;
    public int flags;
    
    @Override
    public String toString() {
        return "User{id='" + id + "', username='" + username + "', globalName='" + globalName + "'}";
    }
}
