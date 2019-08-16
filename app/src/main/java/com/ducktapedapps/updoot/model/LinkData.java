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
    private final boolean saved;
    private final long created_utc;
    private final int num_comments;
    private final gildings gildings;

    public String getSubreddit() {
        return this.subreddit_name_prefixed;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    private LinkData(String title, String author, int ups, Boolean likes, String subreddit_name_prefixed, String name, String thumbnail, boolean saved, long created, int num_comments, gildings gildings) {
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

    public gildings getGildings() {
        return gildings;
    }

    public String getCustomRelativeTime() {
        long currentTime = System.currentTimeMillis();
        long created = this.created_utc * 1000;
        if (currentTime - created >= 31556926000L) {
            return (currentTime - created) / 31556926000L + "Y";
        } else {
            if (currentTime - created >= 2629743000L) {
                return (currentTime - created) / 2629743000L + "M";
            } else {
                if (currentTime - created >= 604800000L) {
                    return (currentTime - created) / 604800000L + "W";
                } else {
                    if (currentTime - created >= 86400000L) {
                        return (currentTime - created) / 86400000L + "D";
                    } else {
                        if (currentTime - created >= 3600000L) {
                            return (currentTime - created) / 3600000L + "H";
                        } else {
                            return (currentTime - created) / 60000L + "M";
                        }
                    }
                }
            }
        }
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
                this.gildings
        );
    }

    public LinkData save() {
        if (this.saved) {
            return new LinkData(
                    this.title,
                    this.author,
                    this.ups,
                    this.likes,
                    this.subreddit_name_prefixed,
                    this.name,
                    this.thumbnail,
                    false,
                    this.created_utc,
                    this.num_comments,
                    this.gildings
            );
        }
        return new LinkData(
                this.title,
                this.author,
                this.ups,
                this.likes,
                this.subreddit_name_prefixed,
                this.name,
                this.thumbnail,
                true,
                this.created_utc,
                this.num_comments,
                this.gildings
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
                ", saved=" + saved +
                ", created=" + created_utc +
                '}';
    }
}
