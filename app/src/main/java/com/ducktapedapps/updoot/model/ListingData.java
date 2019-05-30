package com.ducktapedapps.updoot.model;

import java.util.List;

public class ListingData {
    String after;
    String before;
    String modhash;
    List<thing> children;

    public String getAfter() {
        return after;
    }

    public String getBefore() {
        return before;
    }

    public String getModhash() {
        return modhash;
    }

    public List<thing> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "ListingData{" +
                "after='" + after + '\'' +
                ", before='" + before + '\'' +
                ", modhash='" + modhash + '\'' +
                ", children=" + children +
                '}';
    }
}
