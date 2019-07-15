package com.ducktapedapps.updoot.model;

import com.google.gson.annotations.SerializedName;

public class account {
    @SerializedName("name")
    private String name;

    public account(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "account{" +
                "name='" + name + '\'' +
                '}';
    }
}
