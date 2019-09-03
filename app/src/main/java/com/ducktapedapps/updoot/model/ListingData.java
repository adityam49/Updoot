package com.ducktapedapps.updoot.model;

import java.io.Serializable;
import java.util.List;

public class ListingData implements data, Serializable {
    private String after;
    private List<thing> children;

    public String getAfter() {
        return after;
    }

    public List<thing> getChildren() {
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
