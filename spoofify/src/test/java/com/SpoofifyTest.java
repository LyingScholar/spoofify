package com;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.spoof.SpotifyService;

/**
 * A large diagnostic tool with extra tests focusing on the local lyrics server at localhost:8080.
 * 
 * Run via:
 *   mvn compile exec:java -Dexec.mainClass=com.spoof.SpoofifyDiagnostic
 * 
 * You can comment out any older tests you don't want to run, or reorder them as needed.
 */
public class SpoofifyTest {

    private static final SpotifyService spotifyService = new SpotifyService();
    private static Timer timer;

    // (Optional) Set a test track ID you know is valid, or keep it blank.
    // This is just to see if the local server returns something for it.
    private static final String TEST_TRACK_ID = "0WHi11uzahqpEtPGYCW6oQ";

    public static void main(String[] args) throws Exception {
        System.out.println("=== EXTRA DIAGNOSTICS FOR LOCAL LYRICS SERVER ON localhost:8080 ===");
        
        // 1. Test if local server is listening at http://localhost:8080 with NO params
        testLocalServerConnectivity();

        // 2. Test local server with trackid param (but no sp_dc set or might fail)
        testLocalServerWithTrackid(TEST_TRACK_ID);

        // 3. If you want to still do a playback poll or older tests, uncomment this:
        //startPlaybackPolling();

        // keep alive if we used a Timer
        // otherwise just exit
        System.out.println("Done with immediate tests. If you had a Timer, you'd keep the program alive.\n");
    }

    /**
     * Attempt to GET http://localhost:8080 (no parameters) 
     * Expect a JSON error: {"error":true,"message":"url or trackid parameter is required!","usage":"..."}
     */
    private static void testLocalServerConnectivity() {
        System.out.println("\n--- testLocalServerConnectivity() ---");
        try {
            String endpoint = "http://localhost:8080";
            String responseBody = doHttpGet(endpoint);
            System.out.println("Raw body => " + responseBody);

            // If it’s the typical Musixmatch-based error:
            // {"error":true,"message":"url or trackid parameter is required!","usage":"..."}
            // then your local server is reachable at least.

            JSONObject json = new JSONObject(responseBody);
            boolean isError = json.optBoolean("error", false);
            String msg = json.optString("message", "No message in JSON");

            if (isError && msg.contains("url or trackid parameter")) {
                System.out.println("SUCCESS: Server responded with the expected 'url or trackid parameter is required!'");
            } else {
                System.out.println("Server did not respond with the usual error. Possibly a different build or config.");
            }
        } catch (Exception e) {
            System.out.println("FAILED: Could not connect to http://localhost:8080 with no params.");
            e.printStackTrace();
        }
    }

    /**
     * Attempt to GET http://localhost:8080/?trackid=XYZ&format=lrc
     */
    private static void testLocalServerWithTrackid(String trackId) {
        System.out.println("\n--- testLocalServerWithTrackid() ---");
        try {
            String endpoint = "http://localhost:8080/?trackid=" + trackId + "&format=lrc";
            String responseBody = doHttpGet(endpoint);

            System.out.println("Raw body => " + responseBody);
            JSONObject json = new JSONObject(responseBody);

            // If the user doesn't have sp_dc set or track lyrics aren't found, it might say so.
            boolean isError = json.optBoolean("error", false);
            if (isError) {
                String msg = json.optString("message", "Unknown error");
                System.out.println("Server returned error => " + msg);
            } else {
                JSONArray lines = json.optJSONArray("lines");
                if (lines == null || lines.isEmpty()) {
                    System.out.println("No lines found. Possibly no lyrics for that track.");
                } else {
                    System.out.println("Found " + lines.length() + " lines of lyrics. Good sign!");
                }
            }

        } catch (Exception e) {
            System.out.println("FAILED: Could not connect or parse response from http://localhost:8080 with trackid param.");
            e.printStackTrace();
        }
    }

    /**
     * If you want to replicate a standard polling approach, uncomment and run it for a minute or two.
     */
    private static void startPlaybackPolling() {
        timer = new Timer(true);
        TimerTask fetchTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("\n--- Playback Poll ---");
                try {
                    // e.g. call getCurrentPlayback, check a track
                    JSONObject playback = spotifyService.getCurrentPlayback();
                    System.out.println("Playback => " + (playback == null ? "null" : playback.toString(2)));
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error in getCurrentPlayback:");
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(fetchTask, 0, 5000);
        System.out.println("Started a polling timer. Press Ctrl+C to end.");
    }


    // --------------------------------------------------------------------
    // Helper to do a GET request
    // --------------------------------------------------------------------
    private static String doHttpGet(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // If the server is not running or not on that port, you get a ConnectException
        // If everything is fine, we parse the body
        System.out.println("HTTP GET " + url + " => status " + response.statusCode());
        return response.body();
    }
}
