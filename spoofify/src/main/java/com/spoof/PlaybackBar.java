package com.spoof;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class PlaybackBar extends HBox {

    private Label trackTitleLabel;
    private Label artistLabel;
    private Label albumLabel;

    private Button shuffleButton;
    private Button prevButton;
    private Button playButton;
    private Button nextButton;
    private Button repeatButton;

    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private Slider progressSlider;
    private Slider volumeSlider;

    public PlaybackBar() {
        setSpacing(20);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);

        // Left: Now Playing
        VBox nowPlayingBox = new VBox(2);
        nowPlayingBox.setAlignment(Pos.CENTER_LEFT);

        trackTitleLabel = new Label("Track Title");
        trackTitleLabel.setFont(Font.font("Arial", 14));
        artistLabel = new Label("Artist Name");
        artistLabel.setFont(Font.font("Arial", 12));
        albumLabel = new Label("Album Name");
        albumLabel.setFont(Font.font("Arial", 12));



        nowPlayingBox.getChildren().addAll(trackTitleLabel, artistLabel, albumLabel);

        // Center: Controls + Progess
        VBox controlsBox = new VBox(5);
        controlsBox.setAlignment(Pos.CENTER);

        // Row 1: Shuffle, Prev, Play,  Next, Repeat
        HBox controlButtons = new HBox(10);
        controlButtons.setAlignment(Pos.CENTER);

        shuffleButton = new Button("Shuffle");
        prevButton = new Button("Prev");
        playButton = new Button("Play");
        nextButton = new Button("Next");
        repeatButton = new Button("Repeat");


        controlButtons.getChildren().addAll(shuffleButton, prevButton, playButton, nextButton, repeatButton);

        // Row 2: Progress Bar with currentTime / totalTime
        HBox progressBox = new HBox(5);
        progressBox.setAlignment(Pos.CENTER);

        currentTimeLabel = new Label("0:00");
        progressSlider = new Slider(0, 1, 0);
        progressSlider.setPrefWidth(250);
        totalTimeLabel = new Label("0:00");
        progressBox.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);
        controlsBox.getChildren().addAll(controlButtons, progressBox);

        // Right side: Volume
        HBox volumeBox = new HBox(5);
        volumeBox.setAlignment(Pos.CENTER_RIGHT);
        Label volumeLabel = new Label("Volume");
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(100);

        volumeBox.getChildren().addAll(volumeLabel, volumeSlider);

        // "Region" to push center to center:
        Region spacerLeft = new Region();
        Region spacerRight = new Region();

        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        getChildren().addAll(nowPlayingBox, spacerLeft, controlsBox, spacerRight, volumeBox);
    }

    // Getters to wire shit eventually
    public Button getShuffleButton() { return shuffleButton; }
    public Button getPrevButton()    { return prevButton; }
    public Button getPlayButton()    { return playButton; }
    public Button getNextButton()    { return nextButton; }
    public Button getRepeatButton()  { return repeatButton; }

    public Slider getProgressSlider() { return progressSlider; }
    public Label getCurrentTimeLabel() { return currentTimeLabel; }
    public Label getTotalTimeLabel()   { return totalTimeLabel; }

    public Slider getVolumeSlider() { return volumeSlider; }

    public Label getTrackTitleLabel() { return trackTitleLabel; }
    public Label getArtistLabel()     { return artistLabel; }
    public Label getAlbumLabel()      { return albumLabel; }
}
