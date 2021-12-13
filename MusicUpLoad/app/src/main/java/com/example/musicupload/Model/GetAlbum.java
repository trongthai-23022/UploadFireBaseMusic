package com.example.musicupload.Model;

public class GetAlbum {
    String name;
    String url;
    String songsCategory;

    public GetAlbum(String name, String songCategory, String url) {
        this.name = name;
        this.url = url;
        this.songsCategory = songCategory;
    }
    public GetAlbum(){}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSongsCategory() {
        return songsCategory;
    }

    public void setSongsCategory(String songsCategory) {
        this.songsCategory = songsCategory;
    }
}