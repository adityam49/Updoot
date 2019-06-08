package com.ducktapedapps.updoot.model;

public class LinkData implements data {
    private String title;
    private String author;
    private int ups;
    private String id;
    private String permalink;
    private long created;
    private String thumbnail;
    private Integer thumbnail_height;
    private Integer thumbnail_width;

    public String getThumbnail() {
        return thumbnail;
    }

    public Integer getThumbnail_height() {
        return thumbnail_height;
    }

    public Integer getThumbnail_width() {
        return thumbnail_width;
    }

    public String getId() {
        return id;
    }

    public long getCreated() {
        return created;
    }

    public String getPermalink() {
        return permalink;
    }

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
                ", id='" + id + '\'' +
                ", permalink='" + permalink + '\'' +
                ", created=" + created +
                ", thumbnail=" + thumbnail +
                ", thumbnail_height=" + thumbnail_height +
                ", thumbnail_width=" + thumbnail_width +
                '}';
    }
}
