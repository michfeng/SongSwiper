package com.codepath.michfeng.songswiper.models;

public class Card {
    public String trackName;
    public String artistName;
    public String coverImagePath;
    public String artistImagePath;

    public Card (String track, String artist, String coverIm, String artistIm) {
        this.trackName = track;
        this.artistName = artist;
        this.coverImagePath = coverIm;
        this.artistImagePath = artistIm;
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
}
