package com.ducktapedapps.updoot.model;

public class LinkData {
    String title;
    String author;
    int ups;

    @Override
    public String toString() {
        return "LinkData{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", ups=" + ups +
                '}';
    }
}
