package com.ducktapedapps.updoot.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Gildings implements Serializable {
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

    @Override
    public String toString() {
        return "Gildings{" +
                "silver=" + silver +
                ", gold=" + gold +
                ", platinum=" + platinum +
                '}';
    }
}
