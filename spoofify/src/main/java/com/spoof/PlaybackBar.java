package com.spoof;

import java.net.URL;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * A Spotify-like bottom bar with "Now Playing" info and controls.
 */
public class PlaybackBar extends HBox {

    private final Label trackTitleLabel;
    private final Label artistLabel;
    private final Label albumLabel;

    private final Button shuffleButton;
    private final Button prevButton;
    private Button playPauseButton;
    private final Button nextButton;
    private final Button repeatButton;

    private final Label currentTimeLabel;
    private final Label totalTimeLabel;
    private final Slider progressSlider;
    private final Slider volumeSlider;

    public PlaybackBar() {
        setSpacing(20);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #181818;"); // Dark background

        // Now Playing info
        VBox nowPlayingBox = new VBox(2);
        nowPlayingBox.setAlignment(Pos.CENTER_LEFT);

        trackTitleLabel = new Label("Track Title");
        trackTitleLabel.setFont(Font.font("Arial", 14));
        trackTitleLabel.setStyle("-fx-text-fill: white;");
        artistLabel = new Label("Artist");
        artistLabel.setFont(Font.font("Arial", 12));
        artistLabel.setStyle("-fx-text-fill: #b3b3b3;");
        albumLabel = new Label("Album");
        albumLabel.setFont(Font.font("Arial", 12));
        albumLabel.setStyle("-fx-text-fill: #b3b3b3;");

        nowPlayingBox.getChildren().addAll(trackTitleLabel, artistLabel, albumLabel);

        // Middle controls
        VBox controlsBox = new VBox(5);
        controlsBox.setAlignment(Pos.CENTER);

        HBox controlButtons = new HBox(10);
        controlButtons.setAlignment(Pos.CENTER);

        shuffleButton = createImageButton("shuffle.png");
        prevButton    = createImageButton("prev.png");
        playPauseButton = createImageButton("play.png");
        nextButton    = createImageButton("next.png");
        repeatButton  = createImageButton("repeat.png");


        controlButtons.getChildren().addAll(shuffleButton, prevButton, playPauseButton, nextButton, repeatButton);

        HBox progressBox = new HBox(5);
        progressBox.setAlignment(Pos.CENTER);

        currentTimeLabel = new Label("0:00");
        currentTimeLabel.setStyle("-fx-text-fill: white;");
        progressSlider = new Slider(0, 1, 0);
        progressSlider.setPrefWidth(250);
        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setStyle("-fx-text-fill: white;");

        progressBox.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);

        controlsBox.getChildren().addAll(controlButtons, progressBox);

        // Right volume
        HBox volumeBox = new HBox(5);
        volumeBox.setAlignment(Pos.CENTER_RIGHT);

        Label volumeLabel = new Label("Volume");
        volumeLabel.setStyle("-fx-text-fill: white;");
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(100);

        volumeBox.getChildren().addAll(volumeLabel, volumeSlider);

        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        getChildren().addAll(nowPlayingBox, spacerLeft, controlsBox, spacerRight, volumeBox);
    }

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

    public Label getTrackTitleLabel() { return trackTitleLabel; }
    public Label getArtistLabel()     { return artistLabel; }
    public Label getAlbumLabel()      { return albumLabel; }

    public Button getShuffleButton()  { return shuffleButton; }
    public Button getPrevButton()     { return prevButton; }
    public Button getPlayPauseButton()     { return playPauseButton; }
    public Button getNextButton()     { return nextButton; }
    public Button getRepeatButton()   { return repeatButton; }

    public Label getCurrentTimeLabel() { return currentTimeLabel; }
    public Label getTotalTimeLabel()   { return totalTimeLabel; }
    public Slider getProgressSlider()  { return progressSlider; }
    public Slider getVolumeSlider()    { return volumeSlider; }
}
