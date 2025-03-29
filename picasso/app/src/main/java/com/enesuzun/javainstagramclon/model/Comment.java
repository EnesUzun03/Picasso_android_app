package com.enesuzun.javainstagramclon.model;

public class Comment {
    private String userEmail;
    private String commentText;

    public Comment(String userEmail, String commentText) {
        this.userEmail = userEmail;
        this.commentText = commentText;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getCommentText() {
        return commentText;
    }
} 