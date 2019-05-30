package com.ducktapedapps.updoot.model;

public class LinkData {
    private String title;
    private String author;
    private int ups;

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getUps() {
        return ups;
    }

    @Override
    public String toString() {
        return "LinkData{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", ups=" + ups +
                '}';
    }
}
