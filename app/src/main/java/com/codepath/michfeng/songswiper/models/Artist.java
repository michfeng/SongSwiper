package com.codepath.michfeng.songswiper.models;

public class Artist {
    private String name;
    private String id;
    private String [] genres;

    public Artist(String name, String id, String[] genres) {
        this.name = name;
        this.id = id;
        this.genres = genres;
    }

    public String getId() {
        return id;
    }

    public String[] getGenres() {
        return genres;
    }

    public String getName() {
        return name;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
