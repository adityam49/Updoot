package com.ducktapedapps.updoot.model;

import androidx.annotation.NonNull;

public class LinkData implements data {
    private String title;
    private String author;
    private int ups;
    private String id;
    private String permalink;
    private long created;
    private String thumbnail;

    public String getThumbnail() {
        return thumbnail;
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
        return "u/" + author;
    }

    public void upVote(){
        this.ups++;
    }
    public String getUps() {
        if (this.ups > 999) {
            return this.ups / 1000 + "k";
        } else {
            return String.valueOf(ups);
        }
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
                '}';
    }
}
