package org.levimc.launcher.ui.entity;

public class PatchNoteItem {
    private String id;
    private String title;
    private String imageUrl;
    private String url;

    public PatchNoteItem(String id, String title, String imageUrl, String url) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUrl() {
        return url;
    }
}

