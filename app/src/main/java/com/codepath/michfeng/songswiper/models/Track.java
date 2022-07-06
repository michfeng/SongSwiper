package com.codepath.michfeng.songswiper.models;

public class Track {
    private String id;
    private String name;
    private Artist [] artist;

    public Track (String id, String name, Artist [] artist) {
        this.id = id;
        this.name = name;
        this.artist = artist;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public Artist [] getArtist() {
        return artist;
    }

    public void setArtist(Artist [] artist) {
        this.artist = artist;
    }
}
