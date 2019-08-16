package com.ducktapedapps.updoot.model;

import com.google.gson.annotations.SerializedName;

public class gildings {
    @SerializedName("gid_1")
    private int silver;
    @SerializedName("gid_2")
    private int gold;
    @SerializedName("gid_3")
    private int platinum;

    public int getPlatinum() {
        return this.platinum;
    }

    public int getGold() {
        return this.gold;
    }

    public int getSilver() {
        return this.silver;
    }
}
