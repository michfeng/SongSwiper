package com.codepath.michfeng.songswiper.models;

public class Card {
    public String trackName;
    public String artistName;
    public String coverImagePath;
    public String artistImagePath;
    public String preview;

    public Card() {}

    public Card (String track, String artist, String coverIm, String artistIm,String preview) {
        this.trackName = track;
        this.artistName = artist;
        this.coverImagePath = coverIm;
        this.artistImagePath = artistIm;
        this.preview = preview;
    }

    public String getArtistImagePath() {
        return artistImagePath;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setArtistImagePath(String artistImagePath) {
        this.artistImagePath = artistImagePath;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }
}
