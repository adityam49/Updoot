package com.ducktapedapps.updoot.model;

import java.io.Serializable;
import java.util.List;

public class ListingData implements Data, Serializable {
    private String after;
    private List<Thing> children;

    public String getAfter() {
        return after;
    }

    public List<Thing> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "ListingData{" +
                "after='" + after + '\'' +
                ", children=" + children +
                '}';
    }
}
