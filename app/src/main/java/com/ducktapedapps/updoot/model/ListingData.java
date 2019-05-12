package com.ducktapedapps.updoot.model;

import java.util.List;

public class ListingData {
    String after;
    String before;
    String modhash;
    List<Link> children;

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
