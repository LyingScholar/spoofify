package com.spoof;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
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
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * A "full-screen" style UI with:
 * - Left pane: album art, track info, controls, progress
 * - Right pane: lyrics for the current track
 */
public class SpoofifyFullScreen extends Application {

    private final SpotifyService spotifyService = new SpotifyService();

    // UI controls on the left
    private ImageView albumArtView;
    private Label trackNameLabel;
    private Label albumNameLabel;
    private Label artistNameLabel;
    private Slider progressSlider;
    private Button playPauseButton;
    private Button prevButton;
    private Button nextButton;
    private Button shuffleButton;
    private Button repeatButton;
    private Slider volumeSlider;

    // UI on the right for lyrics
    private TextArea lyricsArea;

    // State tracking
    private String currentTrackId;
    private String currentAlbumId;
    private boolean isPlaying = false;
    private boolean shuffleOn = false;
    private String repeatMode = "off"; // off, context, or track

    @Override
    public void start(Stage stage) {
        stage.setTitle("Spoofify - Full Screen Demo");

        // Main container is an HBox: left for track/controls, right for lyrics
        HBox root = new HBox();
        root.setSpacing(20);
        root.setPadding(new Insets(20));

        // Left Pane
        VBox leftPane = new VBox(15);
        leftPane.setAlignment(Pos.CENTER);

        // 1) Album art
        albumArtView = new ImageView();
        albumArtView.setFitWidth(300);
        albumArtView.setFitHeight(300);
        albumArtView.setPreserveRatio(true);

        // 2) Track info
        trackNameLabel = new Label("Track Title");
        trackNameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        albumNameLabel = new Label("Album");
        artistNameLabel = new Label("Artist");

        // 3) Progress slider
        progressSlider = new Slider(0, 1, 0);
        progressSlider.setPrefWidth(300);

        // 4) Playback controls (image buttons)
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        // Use local helper to create image-based button
        shuffleButton = createImageButton("shuffle.png");
        prevButton    = createImageButton("prev.png");
        playPauseButton = createImageButton("play.png");
        nextButton    = createImageButton("next.png");
        repeatButton  = createImageButton("repeat.png");

        // Volume
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(100);

        controls.getChildren().addAll(shuffleButton, prevButton, playPauseButton, nextButton, repeatButton, new Label("Volume:"), volumeSlider);

        leftPane.getChildren().addAll(
            albumArtView,
            trackNameLabel,
            albumNameLabel,
            artistNameLabel,
            progressSlider,
            controls
        );

        // Right Pane for lyrics
        VBox rightPane = new VBox();
        rightPane.setAlignment(Pos.CENTER_LEFT);
        lyricsArea = new TextArea();
        lyricsArea.setPrefSize(400, 400);
        lyricsArea.setEditable(false);
        // You can style it:
        lyricsArea.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14;");
        rightPane.getChildren().add(lyricsArea);

        // Add leftPane and rightPane to root
        root.getChildren().addAll(leftPane, rightPane);

        // Scene & Stage
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        handleLogin();
        stage.show();

        // Event handlers
        shuffleButton.setOnAction(e -> handleShuffle());
        repeatButton.setOnAction(e -> handleRepeat());
        prevButton.setOnAction(e -> handlePrevious());
        playPauseButton.setOnAction(e -> handlePlayPause());
        nextButton.setOnAction(e -> handleNext());
        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> handleVolumeChange(newV.intValue()));

        // Start polling for track/lyrics
        startPolling();
    }

    /**
     * Helper to create a Button with an Image icon from resource.
     */
    private Button createImageButton(String imageName) {
        URL url = getClass().getResource("/images/" + imageName);
        if (url == null) {
            throw new RuntimeException("Resource not found: " + imageName);
        }
        Image img = new Image(url.toExternalForm());
        ImageView iv = new ImageView(img);
        iv.setFitWidth(24);
        iv.setFitHeight(24);
        iv.setPreserveRatio(true);

        Button button = new Button();
        button.setGraphic(iv);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    /**
     * Polling method to fetch current track info, update progress slider, fetch lyrics, etc.
     */
    private void startPolling() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (spotifyService.isAuthorized()) {
                    try {
                        JSONObject playback = spotifyService.getCurrentPlayback();
                        if (playback != null && playback.has("item")) {
                            JSONObject track = playback.getJSONObject("item");
                            currentTrackId = track.optString("id", null);
                            JSONObject album = track.optJSONObject("album");
                            currentAlbumId = (album != null) ? album.optString("id", null) : null;

                            // Shuffle/Repeat
                            boolean newShuffleState = playback.optBoolean("shuffle_state", false);
                            String newRepeatMode    = playback.optString("repeat_state", "off");
                            boolean playing         = playback.optBoolean("is_playing", false);

                            // For progress
                            int progressMs = playback.optInt("progress_ms", 0);
                            int durationMs = 0;
                            if (track.has("duration_ms")) {
                                durationMs = track.getInt("duration_ms");
                            }
                            // Track name, artist, album
                            String name = track.optString("name", "Unknown Title");
                            JSONObject artists = track.optJSONArray("artists").optJSONObject(0);
                            String artistName = (artists != null) ? artists.optString("name") : "Unknown Artist";
                            String albumName  = (album != null) ? album.optString("name", "Unknown Album") : "Unknown Album";

                            // Artwork
                            String artUrl = null;
                            if (album != null && album.has("images")) {
                                artUrl = album.getJSONArray("images").getJSONObject(0).getString("url");
                            }
                            final boolean x = Boolean.TRUE.equals(artUrl != null);
                            final Image y = new Image(artUrl);

                            final boolean z = Boolean.TRUE.equals(durationMs > 0);
                            double fraction = (double) progressMs / durationMs;


                            // For lyrics
                            // Example: fetch from a method that returns a map of time->lyric
                            Map<Integer, String> timedLyrics = getLyricsForTrack(currentTrackId);

                            // Update UI on JavaFX thread
                            Platform.runLater(() -> {
                                trackNameLabel.setText(name);
                                albumNameLabel.setText(albumName);
                                artistNameLabel.setText(artistName);

                                
                                if (x) {
                                    albumArtView.setImage(y);
                                }

                                // Shuffle/Repeat states
                                shuffleOn = newShuffleState;
                                repeatMode = newRepeatMode;
                                isPlaying = playing;
                                updateShuffleButtonStyle(shuffleOn);
                                updateRepeatButtonStyle(repeatMode);
                                updatePlayPauseButtonStyle(isPlaying);

                                
                                // Update progress slider
                                if (z) {
                                    progressSlider.setValue(fraction);
                                } else {
                                    progressSlider.setValue(0);
                                }

                                // Lyrics update – highlight current line
                                if (timedLyrics != null) {
                                    String currentLine = timedLyrics.getOrDefault(progressMs / 1000, "");
                                    lyricsArea.setText(buildLyricsText(timedLyrics, progressMs / 1000, currentLine));
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.schedule(task, 0, 1000); // update every 1s
    }

    /**
     * Example: build a string of lyrics, highlighting the current line.
     * In a real UI, you might do coloring or advanced text styling.
     */
    private String buildLyricsText(Map<Integer, String> timedLyrics, int currentSec, String currentLine) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> entry : timedLyrics.entrySet()) {
            int second = entry.getKey();
            String lyric = entry.getValue();
            if (second == currentSec) {
                sb.append(">> ").append(lyric).append(" <<\n");
            } else {
                sb.append(lyric).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Example method to get timed lyrics. 
     * Real Spotify doesn't provide direct lyric data in the Web API.
     * You'd need a 3rd-party or your own store of timed lyrics.
     */
    private Map<Integer, String> getLyricsForTrack(String trackId) {
        // A stub example. In reality you might call a separate service or API
        // that returns line-by-line timestamps in seconds -> lyric text
        return Map.of(
            0,  "Sample lyric line at 0s",
            5,  "Sample lyric line at 5s",
            10, "Sample lyric line at 10s",
            15, "Sample lyric line at 15s",
            20, "Sample lyric line at 20s..."
        );
    }

    // -------------------- PLAYBACK CONTROL HANDLERS ------------------------

    private void handleLogin() {
        try {
            spotifyService.authenticate();
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to start Spotify login: " + ex.getMessage());
        }
    }
    
    private void handleShuffle() {
        if (!spotifyService.isAuthorized()) {
            showAlert("Not Logged In", "Please log in first.");
            return;
        }
        shuffleOn = !shuffleOn; // toggle
        try {
            spotifyService.setShuffle(shuffleOn);
            updateShuffleButtonStyle(shuffleOn);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Shuffle Error", e.getMessage());
        }
    }

    private void handleRepeat() {
        if (!spotifyService.isAuthorized()) {
            showAlert("Not Logged In", "Please log in first.");
            return;
        }
        // cycle through off -> context -> track
        switch (repeatMode) {
            case "off":
                repeatMode = "context";
                break;
            case "context":
                repeatMode = "track";
                break;
            default:
                repeatMode = "off";
                break;
        }
        try {
            spotifyService.setRepeat(repeatMode);
            updateRepeatButtonStyle(repeatMode);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Repeat Error", e.getMessage());
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
            showAlert("Error", e.getMessage());
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
            showAlert("Error", e.getMessage());
        }
    }

    private void handlePlayPause() {
        if (!spotifyService.isAuthorized()) {
            showAlert("Not Logged In", "Please log in first.");
            return;
        }
        try {
            if (isPlaying) {
                spotifyService.pause();
            } else {
                spotifyService.play();
            }
            isPlaying = !isPlaying;
            updatePlayPauseButtonStyle(isPlaying);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", e.getMessage());
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

    // ----------------------- VISUAL STYLING HELPERS -----------------------

    private void updateShuffleButtonStyle(boolean on) {
        // For real icons, you'd swap images or color them green
        if (on) {
            shuffleButton.setStyle("-fx-background-color: transparent; -fx-border-color: #1db954; -fx-border-width: 2px;");
        } else {
            shuffleButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        }
    }

    private void updateRepeatButtonStyle(String mode) {
        if ("off".equals(mode)) {
            repeatButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        } else {
            // "context" or "track"
            repeatButton.setStyle("-fx-background-color: transparent; -fx-border-color: #1db954; -fx-border-width: 2px;");
        }
    }

    private void updatePlayPauseButtonStyle(boolean playing) {
        // Swap out the icon from "play.png" to "pause.png"
        String imageName = playing ? "pause.png" : "play.png";
        Image img = new Image(getClass().getResource("/images/" + imageName).toExternalForm());
        ((ImageView) playPauseButton.getGraphic()).setImage(img);
    }

    // ---------------------------------------------------------------------

    private void showAlert(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    @Override
    public void stop() {
        // Clean up, if needed
    }

    public static void main(String[] args) {
        launch(args);
    }
}
