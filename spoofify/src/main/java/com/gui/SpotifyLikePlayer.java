// package com.gui;


// import java.io.File;

// import javafx.application.Application;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.control.ToggleButton;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.Priority;
// import javafx.scene.layout.VBox;
// import javafx.scene.media.Media;
// import javafx.scene.media.MediaPlayer;
// import javafx.stage.Stage;
// import javafx.util.Duration;

// /**
//  * "Spotify-like" music player using JavaFX basics.
//  */
// public class SpotifyLikePlayer extends Application {

//     // -- Day/Night mode colors --
//     //Maybe add more colors for themes
//     // ---------------------------

//     private static final String DAY_BACKGROUND = "-fx-background-color: white;";
//     private static final String DAY_TEXT_COLOR = "-fx-text-fill: black;";
//     private static final String NIGHT_BACKGROUND = "-fx-background-color: #333333;";
//     private static final String NIGHT_TEXT_COLOR = "-fx-text-fill: white;";

//     // Array of songs (file paths) -- be sure these exist on your machine!
//     private final String[] songFiles = {
//         "src/main/java/com/gui/media/music/FEN.mp3",
//         "src/main/java/com/gui/media/music/Doom.mp3",
//         "src/main/java/com/gui/media/music/Oops.mp3"
//     };

//     // Parallel arrays for song info (could also make a small Song class)
//     private final String[] songTitles = { 
//         "FE!N", 
//         "The Only Thing They Fear Is You", 
//         "Oops!!!" 
//     };
//     private final String[] artistNames = { 
//         "Travis Scott ft Playboi Carti", 
//         "Mick Gordon", 
//         "Yung Gravy"
//     };
    
//     private final String[] albumName = {
//         "UTOPIA",
//         "Doom Eternal OST",
//         "Oops!!!"
//     };

//     private final String[] albumCovers = {
//         "src/main/java/com/gui/media/images/cover1.png",
//         "src/main/java/com/gui/media/images/cover2.png",
//         "src/main/java/com/gui/media/images/cover3.png"
//     };

//     // Keep track of which song is currently playing
//     private int currentIndex = 0;

//     // Media + MediaPlayer
//     private MediaPlayer mediaPlayer;

//     // UI Controls
//     private Label titleLabel;
//     private Label artistLabel;
//     private Label albumLabel;
//     private Label progressLabel;
//     private ImageView albumArtView;

//     private ToggleButton dayNightToggle;

//     @Override
//     public void start(Stage stage) {
//         stage.setTitle("Spotify-Like Player");

        
//         // Day/Night Toggle
//         // ---------------------------

//         HBox topBar = new HBox();
//         topBar.setStyle(DAY_BACKGROUND);  // Start in day mode by default

//         dayNightToggle = new ToggleButton("Night Mode");
//         dayNightToggle.setOnAction(e -> toggleDayNightMode());
//         topBar.getChildren().add(dayNightToggle);

        
//         // Album Art, Song Title, Artist
//         // ---------------------------
        
//         VBox centerBox = new VBox();
//         centerBox.setSpacing(10);
//         centerBox.setStyle(DAY_BACKGROUND); // match day mode initially

//         albumArtView = new ImageView();
//         albumArtView.setFitWidth(200);
//         albumArtView.setFitHeight(200);
//         albumArtView.setPreserveRatio(true);

//         titleLabel = new Label("Song Title");
//         titleLabel.setStyle(DAY_TEXT_COLOR + "-fx-font-size: 18px; -fx-font-weight: bold;");


//         artistLabel = new Label("Artist Name");
//         artistLabel.setStyle(DAY_TEXT_COLOR + "-fx-font-size: 14px;");

//         albumLabel = new Label("Album Name");
//         albumLabel.setStyle(DAY_TEXT_COLOR + "-fx-font-size: 12px;");

//         centerBox.getChildren().addAll(albumArtView, titleLabel, artistLabel, albumLabel);


//         // Make the center box stretch:
//         VBox.setVgrow(albumArtView, Priority.ALWAYS);

        
//         // Playback Controls + Progress
//         // ---------------------------
//         HBox bottomBar = new HBox();
//         bottomBar.setSpacing(10);
//         bottomBar.setStyle(DAY_BACKGROUND);
//         bottomBar.setPadding(new javafx.geometry.Insets(10));

//         Button prevButton = new Button("Prev");
//         Button playButton = new Button("Play");
//         Button pauseButton = new Button("Pause");
//         Button nextButton = new Button("Next");

//         progressLabel = new Label("0:00 / 0:00");
//         progressLabel.setStyle(DAY_TEXT_COLOR);

        
        
//         // Networking all the buttons to the backend
//         prevButton.setOnAction(e -> handlePrevious());
//         playButton.setOnAction(e -> handlePlay());
//         pauseButton.setOnAction(e -> handlePause());
//         nextButton.setOnAction(e -> handleNext());

//         bottomBar.getChildren().addAll(prevButton, playButton, pauseButton, nextButton, progressLabel);

//         // Setups
//         // ---------------------------
//         BorderPane root = new BorderPane();
//         root.setTop(topBar);
//         root.setCenter(centerBox);
//         root.setBottom(bottomBar);
//         Scene scene = new Scene(root, 400, 400);
//         stage.setScene(scene);
//         stage.show();

//         loadTrack(currentIndex);
//     }

//     /**
//      * Load a specific track by index, create media player, and update the UI.
//      */
//     private void loadTrack(int index) {
//         if (mediaPlayer != null) {
//             mediaPlayer.stop();
//         }

//         // Ensure valid index
//         currentIndex = (index + songFiles.length) % songFiles.length;

//         // Create new Media & MediaPlayer
//         File mp3File = new File(songFiles[currentIndex]);
//         Media media = new Media(mp3File.toURI().toString());
//         mediaPlayer = new MediaPlayer(media);

//         // Update album art, song title, artist, album title
//         titleLabel.setText(songTitles[currentIndex]);
//         artistLabel.setText(artistNames[currentIndex]);
//         albumLabel.setText(albumName[currentIndex]);
//         Image cover = new Image("file:" + albumCovers[currentIndex]);
//         albumArtView.setImage(cover);

//         // Reset progress
//         progressLabel.setText("0:00 / 0:00");

//         // Get duration
//         mediaPlayer.setOnReady(() -> {
//             Duration total = mediaPlayer.getMedia().getDuration();
//             String totalStr = formatTime(total);
//             progressLabel.setText("0:00 / " + totalStr);
//         });

//         // Keep track of currentTime to update progress
//         mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
//             Duration total = mediaPlayer.getTotalDuration();
//             if (total != null && !total.isUnknown()) {
//                 String currentStr = formatTime(newTime);
//                 String totalStr = formatTime(total);
//                 progressLabel.setText(currentStr + " / " + totalStr);
//             }
//         });
//     }

//     /**
//      * Code to format a Duration as M:SS (e.g., 3:05).
//      */
//     private String formatTime(Duration duration) {
//         if (duration == null || duration.isUnknown()) {
//             return "0:00";
//         }
//         int seconds = (int) duration.toSeconds();
//         int minutes = seconds / 60;
//         int secs = seconds % 60;
//         return String.format("%d:%02d", minutes, secs);
//     }

//     // -------------------------------
//     // Event Handlers for Buttons
//     // -------------------------------
//     private void handlePrevious() {
//         // Decrement index and reload track
//         loadTrack(currentIndex - 1);
//         mediaPlayer.play();
//     }

//     private void handlePlay() {
//         if (mediaPlayer != null) {
//             mediaPlayer.play();
//         }
//     }

//     private void handlePause() {
//         if (mediaPlayer != null) {
//             mediaPlayer.pause();
//         }
//     }

//     private void handleNext() {
//         // Increment index and reload track
//         loadTrack(currentIndex + 1);
//         mediaPlayer.play();
//     }



//     /**
//      * Toggle between Day and Night mode by updating the style.
//      */
//     private void toggleDayNightMode() {
//         boolean nightMode = dayNightToggle.isSelected();
//         if (nightMode) {
//             dayNightToggle.setText("Day Mode");
//             setOverallTheme(NIGHT_BACKGROUND, NIGHT_TEXT_COLOR);
//         } else {
//             dayNightToggle.setText("Night Mode");
//             setOverallTheme(DAY_BACKGROUND, DAY_TEXT_COLOR);
//         }
//     }

//     /**
//      * Updates the background/text color for the entire UI.
//      */
//     private void setOverallTheme(String backgroundStyle, String textStyle) {
//         // Top bar
//         dayNightToggle.getParent().setStyle(backgroundStyle); 
//         dayNightToggle.setStyle(textStyle);
        
//         // Center region (titleLabel, artistLabel, albumArtView's container)
//         titleLabel.getParent().setStyle(backgroundStyle);
//         titleLabel.setStyle(textStyle + "-fx-font-size: 18px; -fx-font-weight: bold;");
//         artistLabel.setStyle(textStyle + "-fx-font-size: 14px;");

//         // Bottom bar
//         progressLabel.getParent().setStyle(backgroundStyle);
//         progressLabel.setStyle(textStyle);
        
//         // Also re-style any other buttons in bottomBar if needed
//         // We can loop or do them individually
//         HBox bottomBar = (HBox) progressLabel.getParent();
//         for (javafx.scene.Node node : bottomBar.getChildren()) {
//             if (node instanceof Button) {
//                 node.setStyle(textStyle);
//             }
//         }
//     }

//     // Main
//     // ---------------------------
//     public static void main(String[] args) {
//         launch(args);
//     }
// }


//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
package com.gui;


import java.io.File;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration; 

/**
 * "Spotify-like" music player using JavaFX basics.
 */
public class SpotifyLikePlayer extends Application {

    // -- Day/Night mode colors --
    //Maybe add more colors for themes
    // ---------------------------

    private static final String DAY_BACKGROUND = "-fx-background-color: white;";
    private static final String DAY_TEXT_COLOR = "-fx-text-fill: black;";
    private static final String NIGHT_BACKGROUND = "-fx-background-color: #333333;";
    private static final String NIGHT_TEXT_COLOR = "-fx-text-fill: white;";

    // Array of songs (file paths) -- be sure these exist on your machine!
    private final String[] songFiles = {
        "src/main/java/com/gui/media/music/FEN.mp3",
        "src/main/java/com/gui/media/music/Doom.mp3",
        "src/main/java/com/gui/media/music/Oops.mp3"
    };

    // Parallel arrays for song info (could also make a small Song class)
    private final String[] songTitles = { 
        "FE!N", 
        "The Only Thing They Fear Is You", 
        "Oops!!!" 
    };
    private final String[] artistNames = { 
        "Travis Scott ft Playboi Carti", 
        "Mick Gordon", 
        "Yung Gravy"
    };
    
    private final String[] albumName = {
        "UTOPIA",
        "Doom Eternal OST",
        "Oops!!!"
    };

    private final String[] albumCovers = {
        "src/main/java/com/gui/media/images/cover1.png",
        "src/main/java/com/gui/media/images/cover2.png",
        "src/main/java/com/gui/media/images/cover3.png"
    };

    // Keep track of which song is currently playing
    private int currentIndex = 0;

    // Media + MediaPlayer
    private MediaPlayer mediaPlayer;

    // UI Controls
    private Label titleLabel;
    private Label artistLabel;
    private Label albumLabel;
    private Label progressLabel;
    private ImageView albumArtView;

    private ToggleButton dayNightToggle;

    private ImageView blurredBackgroundView; 
    private ProgressBar playbackBar;

    private Slider volumeSlider;


    @Override
    public void start(Stage stage) {
        stage.setTitle("Spotify-Like Player");

         StackPane rootStack = new StackPane();
        
        //Blurred background
        blurredBackgroundView = new ImageView();
        blurredBackgroundView.setFitWidth(800);  // Big enough to cover window
        blurredBackgroundView.setPreserveRatio(true);
        
        GaussianBlur blur = new GaussianBlur(20.0); // increase radius for stronger blurr
        blurredBackgroundView.setEffect(blur);
        rootStack.getChildren().add(blurredBackgroundView);
        BorderPane mainUI = new BorderPane();
        rootStack.getChildren().add(mainUI);

        // Day/Night Toggle
        // ---------------------------

        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: transparent;");
        
        topBar.setAlignment(Pos.CENTER_RIGHT);
        dayNightToggle = new ToggleButton("Night Mode");
        dayNightToggle.setOnAction(e -> toggleDayNightMode());
        topBar.getChildren().add(dayNightToggle);

        
        // Album Art, Song Title, Artist
        // ---------------------------
        
        VBox centerBox = new VBox();
        centerBox.setSpacing(10);
        topBar.setAlignment(Pos.CENTER);
        centerBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3);"); 
        centerBox.setFillWidth(false);  


        albumArtView = new ImageView();
        albumArtView.setFitWidth(200);
        albumArtView.setFitHeight(200);
        albumArtView.setPreserveRatio(true);
        titleLabel = new Label("Song Title");
        artistLabel = new Label("Artist Name");
        albumLabel = new Label("Album Name");
        
        String TitleStyle  = "-fx-font-family: 'Poppins'; -fx-font-size: 30px; -fx-font-weight: bold;";
        String ArtistStyle = "-fx-font-family: 'Poppins'; -fx-font-size: 22px;";
        String AlbumStyle  = "-fx-font-family: 'Poppins'; -fx-font-size: 18px;";

        titleLabel.setStyle(DAY_TEXT_COLOR + TitleStyle);
        artistLabel.setStyle(DAY_TEXT_COLOR + ArtistStyle);
        albumLabel.setStyle(DAY_TEXT_COLOR + AlbumStyle);



        centerBox.getChildren().addAll(albumArtView, titleLabel, artistLabel, albumLabel);
        mainUI.setCenter(centerBox);


        // Make the center box stretch:
        // VBox.setVgrow(albumArtView, Priority.ALWAYS);//----------------------------------------------------------



        
        // Playback Controls + Progress
        // ---------------------------
        HBox bottomBar = new HBox();
        bottomBar.setSpacing(10);
        bottomBar.setStyle("-fx-background-color: transparent;");
        bottomBar.setPadding(new javafx.geometry.Insets(10));

        Button prevButton = new Button("Prev");
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button nextButton = new Button("Next");

        //Progess label to be removed or moved to either side of the playback bar 
        progressLabel = new Label("0:00 / 0:00");
        progressLabel.setStyle(DAY_TEXT_COLOR + "-fx-font-family: 'Poppins'; -fx-font-size: 12px;");

        volumeSlider = new Slider(0.0, 1.0, 0.5); // range 0–1, default 0.5
        volumeSlider.setPrefWidth(100);

        


        playbackBar = new ProgressBar(0.0);
        playbackBar.setPrefWidth(150);
        playbackBar.setStyle("-fx-accent: #ff0000;"); 
        // “-fx-accent” sets the fill color of a ProgressBar in JavaFX (by default)
        
        // Networking all the buttons to the backend
        prevButton.setOnAction(e -> handlePrevious());
        playButton.setOnAction(e -> handlePlay());
        pauseButton.setOnAction(e -> handlePause());
        nextButton.setOnAction(e -> handleNext());

        bottomBar.getChildren().addAll(prevButton, playButton, pauseButton, nextButton, playbackBar, progressLabel,new Label("Volume:"), volumeSlider);
        mainUI.setBottom(bottomBar);

        // Setup the Scene
        // ---------------------------

        Scene scene = new Scene(rootStack, 600, 500); // bigger to see background
        stage.setScene(scene);
        stage.show();

        // Finally, load the first track
        loadTrack(currentIndex);
    }

    /**
     * Load a specific track by index, create media player, and update the UI.
     */
    private void loadTrack(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        // Ensure valid index
        currentIndex = (index + songFiles.length) % songFiles.length;

        File mp3File = new File(songFiles[currentIndex]);
        Media media = new Media(mp3File.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        // Update album art, song title, artist, album title
        titleLabel.setText(songTitles[currentIndex]);
        artistLabel.setText(artistNames[currentIndex]);
        albumLabel.setText(albumName[currentIndex]);
        Image cover = new Image("file:" + albumCovers[currentIndex]);
        albumArtView.setImage(cover);

        blurredBackgroundView.setImage(cover);

        // Reset progress
        playbackBar.setProgress(0.0);
        progressLabel.setText("0:00 / 0:00");
        playbackBar.setStyle(
        "-fx-accent: red;" +
        "-fx-control-inner-background: #cccccc;" + // background of the bar when empty
        "-fx-background-color: #999999;"           // outer border area
        );


        // Get duration
        mediaPlayer.setOnReady(() -> {
            Duration total = mediaPlayer.getMedia().getDuration();
            String totalStr = formatTime(total);
            progressLabel.setText("0:00 / " + totalStr);
        });

        // Keep track of currentTime to update progress
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            Duration total = mediaPlayer.getTotalDuration();
            if (total != null && !total.isUnknown()) {
                double current   = newTime.toSeconds();
                double totalSecs = total.toSeconds();
                double frac      = (totalSecs > 0) ? (current / totalSecs) : 0.0;
                playbackBar.setProgress(frac); // 0.0 -> 1.0

                String currentStr = formatTime(newTime);
                String totalStr   = formatTime(total);
                progressLabel.setText(currentStr + " / " + totalStr);
            }
        });

        //volume slider
        volumeSlider.valueProperty().addListener((ov, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });
    }

    /**
     * Code to format a Duration as M:SS (e.g., 3:05).
     */
    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "0:00";
        }
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        int secs = seconds % 60;

        return String.format("%d:%02d", minutes, secs);
    }

    // -------------------------------
    // Event Handlers for Buttons
    // -------------------------------
    private void handlePrevious() {
        // Decrement index and reload track
        loadTrack(currentIndex - 1);
        mediaPlayer.play();
    }

    private void handlePlay() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    private void handlePause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void handleNext() {
        // Increment index and reload track
        loadTrack(currentIndex + 1);
        mediaPlayer.play();
    }



    /**
     * Toggle between Day and Night mode by updating the style.
     */
    private void toggleDayNightMode() {

        
        boolean nightMode = dayNightToggle.isSelected();
        if (nightMode) {
            dayNightToggle.setText("Day Mode");
            setOverallTheme(NIGHT_BACKGROUND, NIGHT_TEXT_COLOR);
        } else {
            dayNightToggle.setText("Night Mode");
            setOverallTheme(DAY_BACKGROUND, DAY_TEXT_COLOR);
        }
    }

    /**
     * Updates the background/text color for the entire UI.
     */
    private void setOverallTheme(String backgroundStyle, String textStyle) {
        
        
        StackPane rootStack = (StackPane) dayNightToggle.getScene().getRoot();
        BorderPane mainUI = (BorderPane) rootStack.getChildren().get(1);

        // Top
        HBox topBar = (HBox) mainUI.getTop();
        topBar.setStyle(backgroundStyle);
        dayNightToggle.setStyle(textStyle);

        // Center
        VBox centerBox = (VBox) mainUI.getCenter();
        centerBox.setStyle(backgroundStyle);
        titleLabel.setStyle(textStyle + "-fx-font-family: 'Poppins'; -fx-font-size: 30px; -fx-font-weight: bold;");
        artistLabel.setStyle(textStyle + "-fx-font-family: 'Poppins'; -fx-font-size: 22px;");
        albumLabel.setStyle(textStyle + "-fx-font-family: 'Poppins'; -fx-font-size: 18px;");

        // Bottom
        HBox bottomBar = (HBox) mainUI.getBottom();
        // In toggleDayNightMode or setOverallTheme, apply something like:
        bottomBar.setStyle("-fx-background-color: white;"); // if you want to keep white
        playbackBar.setStyle(
            "-fx-accent: #0073e6;"       +  // progress fill color
            "-fx-control-inner-background: #e0e0e0;" +  // bar background color
            "-fx-background-color: #999;"             // outer border area
        );
        volumeSlider.setStyle("-fx-control-inner-background: #e0e0e0;");

        // Re-style the bottom buttons
        for (javafx.scene.Node node : bottomBar.getChildren()) {
            if (node instanceof Button) {
                node.setStyle(textStyle + "-fx-font-family: 'Poppins';");
            }
        }
    }

    // Main
    // ---------------------------
    public static void main(String[] args) {
        launch(args);
    }
}
