package com.example.coupledaysinlove.models;

import java.io.Serializable;

public class BucketItem implements Serializable {
    private String id;
    private String title;
    private String category; // "Citas", "Viajes", "Aventura", "Gastronomía", "Hogar", "Otro"
    private boolean completed;
    private long completedDateMillis;
    private long createdDateMillis;

    public BucketItem() {
    }

    public BucketItem(String id, String title, String category, boolean completed, long completedDateMillis, long createdDateMillis) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.completed = completed;
        this.completedDateMillis = completedDateMillis;
        this.createdDateMillis = createdDateMillis;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCompletedDateMillis() {
        return completedDateMillis;
    }

    public void setCompletedDateMillis(long completedDateMillis) {
        this.completedDateMillis = completedDateMillis;
    }

    public long getCreatedDateMillis() {
        return createdDateMillis;
    }

    public void setCreatedDateMillis(long createdDateMillis) {
        this.createdDateMillis = createdDateMillis;
    }
}