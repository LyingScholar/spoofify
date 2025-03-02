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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * A Spotify via the Web API.
 */
public class Spoofify extends Application {
    private final SpotifyService spotifyService = new SpotifyService();
    private PlaybackBar playbackBar;  
    private ImageView albumCoverView;

    private String currentTrackId;
    private String currentAlbumId;


    @Override
    public void start(Stage stage) {
        stage.setTitle("Spoofify - Spotify Web API Demo");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPrefSize(900, 600);
        mainLayout.setPadding(new Insets(20));
        
        
        // Top Bar
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #1DB954;"); // Typical Spotify green
        Label logoLabel = new Label("Spoofify");
        logoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        topBar.getChildren().add(logoLabel);
        mainLayout.setTop(topBar);

        // // Track info + album cover
        // titleLabel = new Label("Track title");
        // artistLabel = new Label("Artist");
        // albumCoverView = new ImageView();
        // albumCoverView.setFitHeight(200);
        // albumCoverView.setFitWidth(200);
        // albumCoverView.setPreserveRatio(true);

        // VBox centerBox = new VBox(10, albumCoverView, titleLabel, artistLabel);
        // centerBox.setAlignment(Pos.CENTER);
        // root.getChildren().add(centerBox);

        // // Playback controls
        // HBox bottomBar = new HBox(10);
        // bottomBar.setAlignment(Pos.CENTER);

        // prevButton = new Button("Prev");
        // prevButton.setOnAction(e -> handlePrevious());

        // playButton = new Button("Play");
        // playButton.setOnAction(e -> handlePlay());

        // pauseButton = new Button("Pause");
        // pauseButton.setOnAction(e -> handlePause());

        // nextButton = new Button("Next");
        // nextButton.setOnAction(e -> handleNext());

        // Label volLabel = new Label("Volume");
        // volumeSlider = new Slider(0, 100, 50);
        // volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
        //     handleVolumeChange(newVal.intValue())
        // );

        // titleLabel = new Label("Title");
        // titleLabel.setOnMouseClicked(e -> showTrackDetails());
        // artistLabel = new Label("Artist");
        // artistLabel.setOnMouseClicked(e -> showAlbumDetails());

        // bottomBar.getChildren().addAll(prevButton, playButton, pauseButton, nextButton, volLabel, volumeSlider);
        // root.getChildren().add(bottomBar);



        // Left Sidebar
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setStyle("-fx-background-color: #121212;"); // Spotify-like dark
        Label homeLabel = new Label("Home");
        homeLabel.setStyle("-fx-text-fill: white;");
        Label searchLabel = new Label("Search");
        searchLabel.setStyle("-fx-text-fill: white;");
        Label libraryLabel = new Label("Your Library");
        libraryLabel.setStyle("-fx-text-fill: white;");
        sidebar.getChildren().addAll(homeLabel, searchLabel, libraryLabel);
        mainLayout.setLeft(sidebar);


        // Center content (just an album cover for now)
        albumCoverView = new ImageView();
        albumCoverView.setFitHeight(300);
        albumCoverView.setFitWidth(300);
        albumCoverView.setPreserveRatio(true);

        VBox centerBox = new VBox(albumCoverView);
        centerBox.setAlignment(Pos.CENTER);
        mainLayout.setCenter(centerBox);

        // Bottom Playback Bar
        playbackBar = new PlaybackBar();
        // Wire up Spotify actions
        playbackBar.getPlayButton().setOnAction(e -> handlePlay());
        playbackBar.getPauseButton().setOnAction(e -> handlePause());
        playbackBar.getNextButton().setOnAction(e -> handleNext());
        playbackBar.getPrevButton().setOnAction(e -> handlePrevious());
        playbackBar.getShuffleButton().setOnAction(e -> handleShuffle());
        playbackBar.getRepeatButton().setOnAction(e -> handleRepeat());
        // Volume
        playbackBar.getVolumeSlider().valueProperty().addListener((obs, oldV, newV) -> {
            handleVolumeChange(newV.intValue());
        });
        // Clickable track/artist for details
        playbackBar.getTrackTitleLabel().setOnMouseClicked(e -> showTrackDetails());
        playbackBar.getArtistLabel().setOnMouseClicked(e -> showAlbumDetails());
        mainLayout.setBottom(playbackBar);

        Scene scene = new Scene(mainLayout);
        handleLogin();
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
            showAlert("Not Logged In", "Please log in first (in your OAuth flow).");
            return;
        }
        try {
            spotifyService.play();
        } catch (Exception e) {
            showAlert("Playback Error", e.getMessage());
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
            showAlert("Playback Error", e.getMessage());
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
            showAlert("Playback Error", e.getMessage());
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
            showAlert("Playback Error", e.getMessage());
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

    private void handleShuffle() {
        // stub, call spotifyService to toggle shuffle
    }

    private void handleRepeat() {
        // stub, call spotifyService to toggle repeat
    }




    
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
    
                            String albumName = (album != null) ? album.optString("name", "Unknown Album") : "Unknown Album";
                            
                            String imageUrl = null;
                            if (album != null && album.has("images")) {
                                imageUrl = album.getJSONArray("images").getJSONObject(0).getString("url");
                            }

                            final boolean x = Boolean.TRUE.equals(imageUrl != null);
                            final Image y = new Image(imageUrl);

                            Platform.runLater(() -> {
                                playbackBar.getTrackTitleLabel().setText(trackName);
                                playbackBar.getArtistLabel().setText(artistName);
                                playbackBar.getAlbumLabel().setText(albumName);
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
            showAlert("No Track Selected", "No track ID available.");
            return;
        }
        try {
            JSONObject trackJson = spotifyService.getTrackDetails(currentTrackId);
            if (trackJson == null) {
                showAlert("Error", "No track details found.");
                return;
            }
            String name        = trackJson.optString("name", "N/A");
            int durationMs     = trackJson.optInt("duration_ms", 0);
            int popularity     = trackJson.optInt("popularity", 0);
            JSONObject album   = trackJson.optJSONObject("album");
            String albumName   = (album != null) ? album.optString("name", "N/A") : "N/A";
            String releaseDate = (album != null) ? album.optString("release_date", "N/A") : "N/A";

            int seconds        = durationMs / 1000;
            String durationStr = String.format("%d:%02d", seconds / 60, seconds % 60);

            Label trackNameLabel   = new Label("Track: " + name);
            Label albumNameLabel   = new Label("Album: " + albumName);
            Label releaseDateLabel = new Label("Released: " + releaseDate);
            Label popLabel         = new Label("Popularity: " + popularity);
            Label durationLabel    = new Label("Duration: " + durationStr);

            VBox detailBox = new VBox(10, trackNameLabel, albumNameLabel, releaseDateLabel, popLabel, durationLabel);
            detailBox.setPadding(new Insets(10));
            detailBox.setAlignment(Pos.CENTER_LEFT);

            Stage detailStage = new Stage();
            detailStage.setTitle("Track Details");
            detailStage.setScene(new Scene(detailBox, 300, 200));
            detailStage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to load track details: " + e.getMessage());
        }
    }

    private void showAlbumDetails() {
        if (currentAlbumId == null) {
            showAlert("No Album Selected", "No album ID available.");
            return;
        }
        try {
            JSONObject albumJson = spotifyService.getAlbumDetails(currentAlbumId);
            if (albumJson == null) {
                showAlert("Error", "No album details found.");
                return;
            }
            String albumName   = albumJson.optString("name", "N/A");
            String releaseDate = albumJson.optString("release_date", "N/A");
            int totalTracks    = albumJson.optInt("total_tracks", 0);
            String label       = albumJson.optString("label", "N/A");

            Label nameLabel        = new Label("Album: " + albumName);
            Label releaseDateLabel = new Label("Released: " + releaseDate);
            Label tracksLabel      = new Label("Tracks: " + totalTracks);
            Label recordLabel      = new Label("Label: " + label);

            VBox detailBox = new VBox(10, nameLabel, releaseDateLabel, tracksLabel, recordLabel);
            detailBox.setPadding(new Insets(10));
            detailBox.setAlignment(Pos.CENTER_LEFT);

            Stage detailStage = new Stage();
            detailStage.setTitle("Album Details");
            detailStage.setScene(new Scene(detailBox, 300, 200));
            detailStage.show();
        } catch (Exception e) {
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
        // dispose if here.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
