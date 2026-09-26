package com.example.coupledaysinlove.models;

import java.io.Serializable;

public class SpecialEvent implements Serializable {
    private String id;
    private String title;
    private long dateMillis;
    private String category; // "Cumpleaños", "Aniversario", "Cita", "Otro"
    private boolean repeatYearly;
    private String photoPath;
    private String description;

    public SpecialEvent() {
    }

    public SpecialEvent(String id, String title, long dateMillis, String category, boolean repeatYearly) {
        this(id, title, dateMillis, category, repeatYearly, null, null);
    }

    public SpecialEvent(String id, String title, long dateMillis, String category, boolean repeatYearly, String photoPath, String description) {
        this.id = id;
        this.title = title;
        this.dateMillis = dateMillis;
        this.category = category;
        this.repeatYearly = repeatYearly;
        this.photoPath = photoPath;
        this.description = description;
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

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isRepeatYearly() {
        return repeatYearly;
    }

    public void setRepeatYearly(boolean repeatYearly) {
        this.repeatYearly = repeatYearly;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}