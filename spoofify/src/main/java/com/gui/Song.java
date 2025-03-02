package com.gui;

public class Song {
    private final String filePath;
    private final String title;
    private final String artist;
    private final String album;
    private final String coverPath;
    /**
     * Constructs a Song instance with all the needed metadata.
     *
     * @param filePath  the local file path or URI to the media file
     * @param title     the title of the song
     * @param artist    the artist performing the song
     * @param album     the album name
     * @param coverPath the local file path or URI for the cover art image
     */
    public Song(String filePath, String title, String artist, String album, String coverPath) {
        this.filePath = filePath;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverPath = coverPath;
    }

    public String getFilePath() {
        return filePath;
    }
    public String getTitle() {
        return title;
    }
    
    public String getArtist() {
        return artist;
    }
    public String getAlbum() {
        return album;
    }
    public String getCoverPath() {
        return coverPath;
    }
    @Override
    public String toString() {
        return String.format("Song[%s by %s from '%s']", title, artist, album);
    }
}
