package com.ducktapedapps.updoot.model;

import org.jetbrains.annotations.NotNull;

public class LinkData implements data {
    private String title;
    private String author;
    private int ups;
    private String subreddit_name_prefixed;
    private String id;
    private String permalink;
    private long created;
    private String thumbnail;

    public String getSubreddit() {
        return this.subreddit_name_prefixed;
    }

    public String getThumbnail() {
        return thumbnail;
    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return "u/" + author;
    }

    public String getUps() {
        if (this.ups > 999) {
            return this.ups / 1000 + "k";
        } else {
            return String.valueOf(ups);
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "LinkData{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", ups=" + ups +
                ", subreddit='" + subreddit_name_prefixed + '\'' +
                ", id='" + id + '\'' +
                ", permalink='" + permalink + '\'' +
                ", created=" + created +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}
