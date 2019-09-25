package com.ducktapedapps.updoot.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LinkData implements Data, Serializable {
    private static final String TAG = "LinkData";
    private final String title;
    private final boolean archived;
    @SerializedName("selftext_html")
    private final String selftext;
    private final String author;
    private final boolean locked;
    private final int ups;
    private final Boolean likes;
    private final String subreddit_name_prefixed;
    private final String name;
    private final String thumbnail;
    private final boolean saved;
    private final long created_utc;
    private final int num_comments;
    private final Gildings gildings;
    private final Preview preview;
    private final String post_hint;
    private final String id;
    private final String url;
    private final boolean isSelfTextExpanded;

    public String getThumbnail() {
        return thumbnail;
    }

    private LinkData(
            String title,
            String author,
            int ups,
            Boolean likes,
            String subreddit_name_prefixed,
            String name, String thumbnail,
            boolean saved,
            long created,
            int num_comments,
            Gildings gildings,
            String selfText,
            Preview p,
            String post_hint,
            String id,
            boolean archived,
            boolean locked,
            String url,
            boolean isSelfTextExpanded
    ) {
        this.isSelfTextExpanded = isSelfTextExpanded;
        this.title = title;
        this.author = author;
        this.ups = ups;
        this.likes = likes;
        this.subreddit_name_prefixed = subreddit_name_prefixed;
        this.name = name;
        this.thumbnail = thumbnail;
        this.saved = saved;
        this.created_utc = created;
        this.num_comments = num_comments;
        this.gildings = gildings;
        this.selftext = selfText;
        this.preview = p;
        this.post_hint = post_hint;
        this.id = id;
        this.archived = archived;
        this.locked = locked;
        this.url = url;
    }

    public boolean isArchived() {
        return archived;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getSelftext() {
        return selftext;
    }

    public String getSubreddit_name_prefixed() {
        return subreddit_name_prefixed;
    }

    public int getCommentsCount() {
        return num_comments;
    }

    public long getCreated() {
        return this.created_utc;
    }

    public Boolean getLikes() {
        return likes;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return "u/" + author;
    }

    public String getName() {
        return name;
    }

    public int getUps() {
        return this.ups;
    }

    public boolean getSaved() {
        return this.saved;
    }

    public Gildings getGildings() {
        return gildings;
    }

    public Preview getPreview() {
        return preview;
    }

    public String getPost_hint() {
        return post_hint;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSelfTextExpanded() {
        return isSelfTextExpanded;
    }

    //DiffUtils mutability hackaround :: https://stackoverflow.com/questions/54493764/pagedlistadapter-does-not-update-list-if-just-the-content-of-an-item-changes
    public LinkData vote(int direction) {
        Boolean updatedLikes = this.likes;
        int updatedUps = this.ups;
        switch (direction) {
            case 1:
                if (this.likes == null) {
                    updatedLikes = true;
                    updatedUps++;
                } else if (!this.likes) {
                    updatedLikes = true;
                    updatedUps += 2;
                } else {
                    updatedLikes = null;
                    updatedUps--;
                }
                break;
            case -1:
                if (this.likes == null) {
                    updatedUps--;
                    updatedLikes = false;
                } else if (this.likes) {
                    updatedUps -= 2;
                    updatedLikes = false;
                } else {
                    updatedUps++;
                    updatedLikes = null;
                }
        }
        return new LinkData(
                this.title,
                this.author,
                updatedUps,
                updatedLikes,
                this.subreddit_name_prefixed,
                this.name,
                this.thumbnail,
                this.saved,
                this.created_utc,
                num_comments,
                this.gildings,
                this.selftext,
                this.preview,
                this.post_hint,
                this.id,
                this.archived,
                this.locked,
                this.url,
                this.isSelfTextExpanded
        );
    }

    public LinkData save() {
        return new LinkData(
                this.title,
                this.author,
                this.ups,
                this.likes,
                this.subreddit_name_prefixed,
                this.name,
                this.thumbnail,
                !this.saved,
                this.created_utc,
                this.num_comments,
                this.gildings,
                this.selftext,
                this.preview,
                this.post_hint,
                this.id,
                this.archived,
                this.locked,
                this.url,
                this.isSelfTextExpanded
        );
    }

    public LinkData toggleSelfTextExpansion() {
        return new LinkData(
                this.title,
                this.author,
                this.ups,
                this.likes,
                this.subreddit_name_prefixed,
                this.name,
                this.thumbnail,
                this.saved,
                this.created_utc,
                this.num_comments,
                this.gildings,
                this.selftext,
                this.preview,
                this.post_hint,
                this.id,
                this.archived,
                this.locked,
                this.url,
                !this.isSelfTextExpanded
        );
    }


    @Override
    public String toString() {
        return "LinkData{" +
                "title='" + title + '\'' +
                ", archived=" + archived +
                ", selftext='" + selftext + '\'' +
                ", author='" + author + '\'' +
                ", locked=" + locked +
                ", ups=" + ups +
                ", likes=" + likes +
                ", subreddit_name_prefixed='" + subreddit_name_prefixed + '\'' +
                ", name='" + name + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", saved=" + saved +
                ", created_utc=" + created_utc +
                ", num_comments=" + num_comments +
                ", Gildings=" + gildings +
                ", Preview=" + preview +
                ", post_hint='" + post_hint + '\'' +
                ", id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", isSelfTextExpanded=" + isSelfTextExpanded +
                '}';
    }
}
