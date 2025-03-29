package com.enesuzun.javainstagramclon.model;

import java.util.ArrayList;

public class Post {
    public String email;
    public String comment;
    public String downloadUrl;
    private String id;
    private int likeCount;
    private ArrayList<String> likedBy;
    private ArrayList<String> savedBy;
    private int commentCount;

    public Post(String email, String comment, String downloadUrl) {
        this.email = email;
        this.comment = comment;
        this.downloadUrl = downloadUrl;
        this.likeCount = 0;
        this.likedBy = new ArrayList<>();
        this.savedBy = new ArrayList<>();
        this.commentCount = 0;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLikedBy(String userEmail) {
        return likedBy.contains(userEmail);
    }

    public void addLike(String userEmail) {
        if (!isLikedBy(userEmail)) {
            likedBy.add(userEmail);
            likeCount++;
        }
    }

    public void removeLike(String userEmail) {
        if (isLikedBy(userEmail)) {
            likedBy.remove(userEmail);
            likeCount--;
        }
    }

    public boolean isSavedBy(String userEmail) {
        return savedBy.contains(userEmail);
    }

    public void addSave(String userEmail) {
        if (!isSavedBy(userEmail)) {
            savedBy.add(userEmail);
        }
    }

    public void removeSave(String userEmail) {
        savedBy.remove(userEmail);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
