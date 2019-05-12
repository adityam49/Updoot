package com.ducktapedapps.updoot.model;

public class Link {
    String kind;
    LinkData data;

    @Override
    public String toString() {
        return "Link{" +
                "kind='" + kind + '\'' +
                ", data=" + data +
                '}';
    }
}
