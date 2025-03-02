package com.spoof;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * A simplified JavaFX "Spoofify" player that controls Spotify via the Web API.
 */
public class Spoofify extends Application {

    private final SpotifyService spotifyService = new SpotifyService();

    private Button loginButton;
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Button prevButton;

    private Label titleLabel;
    private Label artistLabel;
    private ImageView albumCoverView;

    private Slider volumeSlider;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Spoofify - Spotify Web API Demo");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Login button
        loginButton = new Button("Login with Spotify");
        loginButton.setOnAction(e -> handleLogin());
        root.getChildren().add(loginButton);

        // Track info + album cover
        titleLabel = new Label("Track title");
        artistLabel = new Label("Artist");
        albumCoverView = new ImageView();
        albumCoverView.setFitHeight(200);
        albumCoverView.setFitWidth(200);
        albumCoverView.setPreserveRatio(true);

        VBox centerBox = new VBox(10, albumCoverView, titleLabel, artistLabel);
        centerBox.setAlignment(Pos.CENTER);
        root.getChildren().add(centerBox);

        // Playback controls
        HBox bottomBar = new HBox(10);
        bottomBar.setAlignment(Pos.CENTER);

        prevButton = new Button("Prev");
        prevButton.setOnAction(e -> handlePrevious());

        playButton = new Button("Play");
        playButton.setOnAction(e -> handlePlay());

        pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> handlePause());

        nextButton = new Button("Next");
        nextButton.setOnAction(e -> handleNext());

        Label volLabel = new Label("Volume");
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
            handleVolumeChange(newVal.intValue())
        );

        titleLabel = new Label("Title");
        titleLabel.setOnMouseClicked(e -> showTrackDetails());
        artistLabel = new Label("Artist");
        artistLabel.setOnMouseClicked(e -> showAlbumDetails());

        bottomBar.getChildren().addAll(prevButton, playButton, pauseButton, nextButton, volLabel, volumeSlider);
        root.getChildren().add(bottomBar);

        Scene scene = new Scene(root, 600, 450);
        stage.setScene(scene);
        stage.show();

        startPlaybackPolling();
    }

    private void handleLogin() {
        try {
            spotifyService.authenticate();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to start Spotify login: " + ex.getMessage());
        }
    }

    private void handlePlay() {
        if (!spotifyService.isAuthorized()) {
            showAlert("Not Logged In", "Please log in first.");
            return;
        }
        try {
            spotifyService.play();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Playback Error", "Failed to play: " + e.getMessage());
        }
    }

    private void handlePause() {
        if (!spotifyService.isAuthorized()) {
            showAlert("Not Logged In", "Please log in first.");
            return;
        }
        try {
            spotifyService.pause();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Playback Error", "Failed to pause: " + e.getMessage());
        }
    }

    private void handleNext() {
        if (!spotifyService.isAuthorized()) {
            showAlert("Not Logged In", "Please log in first.");
            return;
        }
        try {
            spotifyService.nextTrack();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Playback Error", "Failed to skip: " + e.getMessage());
        }
    }

    private void handlePrevious() {
        if (!spotifyService.isAuthorized()) {
            showAlert("Not Logged In", "Please log in first.");
            return;
        }
        try {
            spotifyService.previousTrack();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Playback Error", "Failed to go previous: " + e.getMessage());
        }
    }

    private void handleVolumeChange(int volumePercent) {
        if (!spotifyService.isAuthorized()) {
            return;
        }
        try {
            spotifyService.setVolume(volumePercent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String currentTrackId;
    private String currentAlbumId;

    private void startPlaybackPolling() {
        Timer timer = new Timer(true);
        TimerTask fetchTask = new TimerTask() {
            @Override
            public void run() {
                if (spotifyService.isAuthorized()) {
                    try {
                        JSONObject playback = spotifyService.getCurrentPlayback();
                        if (playback != null && playback.has("item")) {
                            JSONObject track = playback.getJSONObject("item");
                            // track ID and album ID
                            currentTrackId = track.optString("id", null);
                            JSONObject album = track.optJSONObject("album");
                            currentAlbumId = (album != null) ? album.optString("id", null) : null;
    
                            String trackName = track.optString("name", "Unknown Title");
                            JSONObject artists = track.optJSONArray("artists").optJSONObject(0);
                            String artistName = (artists != null) ? artists.optString("name") : "Unknown Artist";
    
                            String imageUrl = null;
                            if (album != null && album.has("images")) {
                                imageUrl = album.getJSONArray("images").getJSONObject(0).getString("url");
                            }

                            final boolean x = Boolean.TRUE.equals(imageUrl != null);
                            final Image y = new Image(imageUrl);

                            Platform.runLater(() -> {
                                titleLabel.setText(trackName);
                                artistLabel.setText(artistName);
                                if (x) {
                                    albumCoverView.setImage(y);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        timer.schedule(fetchTask, 0, 5000);
    }

    private void showTrackDetails() {
        if (currentTrackId == null) {
            showAlert("No Track Selected", "There is no track to display.");
            return;
        }
        try {
            JSONObject trackJson = spotifyService.getTrackDetails(currentTrackId);
            if (trackJson == null) {
                showAlert("Error", "Could not retrieve track details.");
                return;
            }
            // Grab some fields
            String name       = trackJson.optString("name", "N/A");
            int durationMs    = trackJson.optInt("duration_ms", 0);
            int popularity    = trackJson.optInt("popularity", 0);
            JSONObject album  = trackJson.optJSONObject("album");
            String albumName  = (album != null) ? album.optString("name", "N/A") : "N/A";
            String releaseDate= (album != null) ? album.optString("release_date", "N/A") : "N/A";
    
            // Convert duration to M:SS
            int seconds = durationMs / 1000;
            String durationStr = String.format("%d:%02d", seconds/60, seconds%60);
    
            // Build a simple UI for the details
            Label trackNameLabel     = new Label("Track: " + name);
            Label albumNameLabel     = new Label("Album: " + albumName);
            Label releaseDateLabel   = new Label("Released: " + releaseDate);
            Label popularityLabel    = new Label("Popularity: " + popularity);
            Label durationLabel      = new Label("Duration: " + durationStr);
    
            VBox detailBox = new VBox(10, trackNameLabel, albumNameLabel, releaseDateLabel, popularityLabel, durationLabel);
            detailBox.setPadding(new Insets(15));
            detailBox.setAlignment(Pos.CENTER_LEFT);
    
            Stage detailStage = new Stage();
            detailStage.setTitle("Track Details");
            detailStage.setScene(new Scene(detailBox, 300, 200));
            detailStage.show();
    
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load track details: " + e.getMessage());
        }
    }
    
    private void showAlbumDetails() {
        if (currentAlbumId == null) {
            showAlert("No Album Selected", "There is no album to display.");
            return;
        }
        try {
            JSONObject albumJson = spotifyService.getAlbumDetails(currentAlbumId);
            if (albumJson == null) {
                showAlert("Error", "Could not retrieve album details.");
                return;
            }
            String albumName    = albumJson.optString("name", "N/A");
            String releaseDate  = albumJson.optString("release_date", "N/A");
            int totalTracks     = albumJson.optInt("total_tracks", 0);
            String label        = albumJson.optString("label", "N/A");
    
            // Build a simple UI for the details
            Label nameLabel         = new Label("Album: " + albumName);
            Label releaseDateLabel  = new Label("Released: " + releaseDate);
            Label tracksLabel       = new Label("Tracks: " + totalTracks);
            Label recordLabel       = new Label("Label: " + label);
    
            VBox detailBox = new VBox(10, nameLabel, releaseDateLabel, tracksLabel, recordLabel);
            detailBox.setPadding(new Insets(15));
            detailBox.setAlignment(Pos.CENTER_LEFT);
    
            Stage detailStage = new Stage();
            detailStage.setTitle("Album Details");
            detailStage.setScene(new Scene(detailBox, 300, 200));
            detailStage.show();
    
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load album details: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void stop() {
        // If you were using a local MediaPlayer for MP3s, dispose it here.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
