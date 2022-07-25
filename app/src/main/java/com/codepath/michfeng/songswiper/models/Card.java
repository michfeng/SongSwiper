package com.codepath.michfeng.songswiper.models;

import org.parceler.Parcel;

import spotify.models.tracks.TrackSimplified;

@Parcel
// Keeps track of specific recommendations in "cards"
public class Card {
    public String trackName;
    public String artistName;
    public String coverImagePath;
    public String artistImagePath;
    public String preview;
    public String uri;
    public String id;
    public boolean isExplicit;
    public int duration;

    public Card() {}

    public Card (String trackname, String artist, String coverIm, String artistIm, String preview,
                 String uri, String id, boolean isExplicit, int duration) {
        this.trackName = trackname;
        this.artistName = artist;
        this.coverImagePath = coverIm;
        this.artistImagePath = artistIm;
        this.preview = preview;
        this.uri = uri;
        this.id = id;
        this.isExplicit = isExplicit;
        this.duration = duration;
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

    public String getUri() { return uri; }

    public void setUri(String uri) { this.uri = uri; }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isExplicit() { return isExplicit; }

    public int getDuration() { return duration; }

    public void setDuration(int duration) { this.duration = duration; }

    public void setExplicit(boolean explicit) { isExplicit = explicit; }
}
