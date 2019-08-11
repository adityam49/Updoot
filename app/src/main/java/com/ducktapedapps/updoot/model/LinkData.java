package com.ducktapedapps.updoot.model;

public class LinkData implements data {
    private static final String TAG = "LinkData";
    private final String title;
    private final String author;
    private final int ups;
    private final Boolean likes;
    private final String subreddit_name_prefixed;
    private final String name;
    private final String thumbnail;

    public String getSubreddit() {
        return this.subreddit_name_prefixed;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    LinkData(String title, String author, int ups, Boolean likes, String subreddit_name_prefixed, String name, String thumbnail) {
        this.title = title;
        this.author = author;
        this.ups = ups;
        this.likes = likes;
        this.subreddit_name_prefixed = subreddit_name_prefixed;
        this.name = name;
        this.thumbnail = thumbnail;
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
                this.thumbnail
        );
    }


    @Override
    public String toString() {
        return "LinkData{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", ups=" + ups +
                ", likes=" + likes +
                ", subreddit_name_prefixed='" + subreddit_name_prefixed + '\'' +
                ", name='" + name + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}
