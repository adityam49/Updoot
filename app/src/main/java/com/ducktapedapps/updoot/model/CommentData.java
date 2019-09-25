package com.ducktapedapps.updoot.model;

import java.io.Serializable;

public class CommentData implements Data, Serializable {
    private String author;
    private int depth;
    private String body;
    private int ups;
    private Boolean likes;
    private boolean saved;
    private String id;
    private Thing replies;
    private Gildings gildings;

    public String getAuthor() {
        return author;
    }

    public int getDepth() {
        return depth;
    }

    public String getBody() {
        return body;
    }

    public int getUps() {
        return ups;
    }

    public Boolean getLikes() {
        return likes;
    }

    public boolean isSaved() {
        return saved;
    }

    public String getId() {
        return id;
    }

    public Thing getReplies() {
        return replies;
    }

    @Override
    public String toString() {
        return "CommentData{" +
                "author='" + author + '\'' +
                ", depth=" + depth +
                ", body='" + body + '\'' +
                ", ups=" + ups +
                ", likes=" + likes +
                ", saved=" + saved +
                ", id='" + id + '\'' +
                ", replies=" + replies +
                ", Gildings=" + gildings +
                '}';
    }

    public Gildings getGildings() {
        return gildings;
    }
}
