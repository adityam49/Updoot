package com.ducktapedapps.updoot.model;

import java.util.List;

public class ListingData implements data {
    private String after;
    private String before;
    private String modhash;
    private List<thing> children;

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
