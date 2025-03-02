package com;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.spoof.SpotifyService;

/**
 * A large diagnostic tool to systematically check potential pitfalls
 * in your "Spoofify" environment and calls. Run via:
 *   mvn compile exec:java -Dexec.mainClass=com.spoof.SpoofifyDiagnostic
 */
public class SpoofifyDiagnostic {

    private static final SpotifyService spotifyService = new SpotifyService();
    private static Timer timer;
    
    public static void main(String[] args) throws Exception {
        // 1. Print System Info
        printSystemInfo();
        
        // 2. Attempt Spotify auth
        if (!tryAuth()) {
            System.out.println("Exiting: Could not authenticate with Spotify.");
            return;
        }
        
        // 3. Timer to mimic your polling approach
        timer = new Timer(true);
        TimerTask diagnosticTask = new TimerTask() {
            @Override
            public void run() {
                runDiagnostic();
            }
        };
        timer.schedule(diagnosticTask, 0, 5000); // every 5 sec
        
        // Keep alive
        System.out.println("\n** Diagnostic started. Will poll every 5s. Press Ctrl+C to exit. **\n");
        while (true) {
            Thread.sleep(60000);
        }
    }
    
    /**
     * If you want to shut down gracefully at some point, call from another thread.
     */
    public static void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
    
    /**
     * Grabs environment details, OS, Java version, etc.
     */
    private static void printSystemInfo() {
        System.out.println("=== SYSTEM INFO ===");
        System.out.println("OS:       " + System.getProperty("os.name") + ", version: " + System.getProperty("os.version"));
        System.out.println("Java:     " + System.getProperty("java.version"));
        System.out.println("User Dir: " + System.getProperty("user.dir"));
        System.out.println("SP_DC:    " + System.getenv("SP_DC")); // Typically for your lyrics server, if needed
        System.out.println("===================\n");
    }
    
    /**
     * Attempt to open browser for Spotify login, wait for a short moment,
     * then check if we are authorized.
     */
    private static boolean tryAuth() {
        System.out.println("=== Checking Spotify Authorization ===");
        try {
            if (spotifyService.isAuthorized()) {
                System.out.println("Already had a non-null access token, skipping re-auth.\n");
                return true;
            }
            System.out.println("Calling spotifyService.authenticate() now...");
            spotifyService.authenticate();
            
            // Give user time to log in
            System.out.println("Waiting 8 seconds for user to log in and callback to arrive...");
            Thread.sleep(8000);
            
            boolean ok = spotifyService.isAuthorized();
            System.out.println("After wait, isAuthorized() => " + ok + "\n");
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * The main logic: fetch current playback, parse track ID, attempt local lyrics, etc.
     */
    private static void runDiagnostic() {
        System.out.println("=== DIAGNOSTIC POLLING ===");
        
        if (!spotifyService.isAuthorized()) {
            System.out.println("Not authorized. Stopping. (Or you could re-auth if you want a loop).");
            return;
        }
        
        // 1. Fetch current playback
        JSONObject playback = null;
        try {
            playback = spotifyService.getCurrentPlayback();
            System.out.println("getCurrentPlayback() => " + (playback == null ? "null" : playback.toString(2)));
        } catch (IOException | InterruptedException e) {
            System.out.println("Error calling getCurrentPlayback():");
            e.printStackTrace();
            return;
        }
        
        if (playback == null) {
            System.out.println("No item currently playing, or 204 from Spotify. Possibly no active device.");
            return;
        }
        if (!playback.has("item")) {
            System.out.println("playback does not have 'item', user may not have anything playing. JSON => " + playback);
            return;
        }
        
        // 2. Parse track
        JSONObject track = playback.getJSONObject("item");
        String trackId = track.optString("id", null);
        System.out.println("trackId => " + trackId);
        if (trackId == null || trackId.isEmpty()) {
            System.out.println("Track ID is null or empty, skipping lyrics call.\n");
            return;
        }
        
        // 3. Attempt local lyrics
        try {
            System.out.println("Attempting getLyricsFromTrack(" + trackId + ")...");
            String[] lines = spotifyService.getLyricsFromTrack(trackId);
            System.out.println("Local lyrics array length => " + lines.length);
            
            // If you want to mimic your distribution logic:
            int progressMs = playback.optInt("progress_ms", 0);
            int durationMs = track.optInt("duration_ms", 0);
            if (durationMs > 0 && lines.length > 0) {
                int lineIndex = (int) (((double) progressMs / durationMs) * lines.length);
                if (lineIndex < 0) lineIndex = 0;
                if (lineIndex >= lines.length) lineIndex = lines.length - 1;
                String line = lines[lineIndex];
                System.out.println("Lyric line for current progress => " + line);
            } else {
                System.out.println("durationMs= " + durationMs + ", lines= " + lines.length + ". Can't pick a line.");
            }
            
        } catch (Exception e) {
            System.out.println("Error calling getLyricsFromTrack:");
            e.printStackTrace();
        }
        
        // 4. Hypothetical Timed Lyrics (your old stub):
        Map<Integer, String> timed = new SpoofifyDiagnostic().stubGetLyricsForTrack(trackId);
        int progressSec = playback.optInt("progress_ms", 0) / 1000;
        String oldLine = timed.getOrDefault(progressSec, "(no line in stub)");
        System.out.println("Stub timed lyric => second=" + progressSec + ", line=" + oldLine + "\n");
        
        // Possibly more checks, e.g. Shuffle state, album name, etc.
        System.out.println("=== DIAGNOSTIC POLLING DONE ===\n");
    }
    
    /**
     * A clone of your old stub method, just to check if it conflicts.
     * Not used in the real app, purely for debug logging.
     */
    private Map<Integer, String> stubGetLyricsForTrack(String trackId) {
        return Map.of(
            0,  "Sample line @0s",
            5,  "Sample line @5s",
            10, "Sample line @10s"
        );
    }
}
