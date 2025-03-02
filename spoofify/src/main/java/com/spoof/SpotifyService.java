package com.spoof;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpServer;

/**
 * Handles Spotify OAuth and basic Web API calls.
 * Requires: Java 11+ for HttpClient and JSSE.
 */
public class SpotifyService {

    // Replace with your own credentials and redirect URI
    private static final String CLIENT_ID = "7f8e899fbc254c01bdecd6a99ea67e70";
    private static final String CLIENT_SECRET = "4232899d65e54f4c982ee65c88385374";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";

    // Scopes define what permissions you want, e.g. controlling playback
    private static final String SCOPES = "user-read-playback-state user-modify-playback-state";

    private String accessToken;



    public static void closeBrowserWindow() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Windows: Send Ctrl+W via PowerShell
                new ProcessBuilder("powershell", "-command",
                    "$wshell = New-Object -ComObject wscript.shell; " + "$wshell.AppActivate(\"localhost\");" +
                    "$wshell.SendKeys('')").start();
            } else if (os.contains("mac")) {
                // fuck u mac users
                // macOS: Send Command+W via AppleScript
                // new ProcessBuilder("osascript", "-e",
                //     "tell application \"System Events\" to keystroke \"w\" using command down").start();
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux: Use xdotool to send Ctrl+W
                new ProcessBuilder("xdotool", "key", "ctrl+w").start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void authenticate() throws IOException {
        // 1) Start a local server to handle the callback
        startLocalCallbackServer();

        // 2) Launch the user's browser to request authorization
        String authorizeUrl = "https://accounts.spotify.com/authorize?" +
                "client_id=" + CLIENT_ID +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);

        System.out.println("Opening browser for Spotify login...");
        java.awt.Desktop.getDesktop().browse(URI.create(authorizeUrl));
        // try {
        //     TimeUnit.MILLISECONDS.sleep(5);
        // } catch (InterruptedException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // } // Wait 50ms
        closeBrowserWindow(); // Close the browser


    }

    private void startLocalCallbackServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String responseText = "Authorization successful! You can close this window.";
            exchange.sendResponseHeaders(200, responseText.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseText.getBytes());
            }
            
            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1];  
                // If there's also a state or other query params,
                if (code.contains("&")) {
                    code = code.split("&")[0];
                }
                System.out.println("Received Spotify auth code: " + code);
                try {
                    exchangeCodeForToken(code);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Stop server after one request
            server.stop(0);
        });
        server.start();
    }



    /**
     * Exchange authorization code for an access token
     */
    private void exchangeCodeForToken(String code) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String form = "grant_type=authorization_code" +
                      "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                      "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", basicAuthHeader(CLIENT_ID, CLIENT_SECRET))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Spotify token response: " + response.body());






        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            accessToken = json.getString("access_token");
            System.out.println("Access token: " + accessToken);
        } else {
            throw new RuntimeException("Failed to get token: " + response.body());
        }
    }

    private String basicAuthHeader(String clientId, String clientSecret) {
        String creds = clientId + ":" + clientSecret;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(creds.getBytes());
    }



    /**
     * Returns true if we have a non-null, non-empty accessToken
     */
    public boolean isAuthorized() {
        return accessToken != null && !accessToken.isEmpty();
    }






    // ------------------------
    // API calls
    // ------------------------------

    public void play() throws IOException, InterruptedException {
        callSpotifyApi("PUT", "/me/player/play", null);
    }
    public void pause() throws IOException, InterruptedException {
        callSpotifyApi("PUT", "/me/player/pause", null);
    }
    public void nextTrack() throws IOException, InterruptedException {
        callSpotifyApi("POST", "/me/player/next", null);
    }
    public void previousTrack() throws IOException, InterruptedException {
        callSpotifyApi("POST", "/me/player/previous", null);
    }
    public void setVolume(int volumePercent) throws IOException, InterruptedException {
        // volume must be 0-100
        callSpotifyApi("PUT", "/me/player/volume?volume_percent=" + volumePercent, null);
    }
    public JSONObject getCurrentPlayback() throws IOException, InterruptedException {
        String response = callSpotifyApi("GET", "/me/player/currently-playing", null);
        if (response == null) return null;
        return new JSONObject(response);
    }

    public void setShuffle(boolean shuffle) throws IOException, InterruptedException {
        // PUT /v1/me/player/shuffle?state=true|false
        String endpoint = "/me/player/shuffle?state=" + shuffle;
        callSpotifyApi("PUT", endpoint, null);
    }
    
    public void setRepeat(String mode) throws IOException, InterruptedException {
        // mode can be "off", "track", or "context"
        // PUT /v1/me/player/repeat?state={mode}
        String endpoint = "/me/player/repeat?state=" + mode;
        callSpotifyApi("PUT", endpoint, null);
    }
    


    public JSONObject getTrackDetails(String trackId) throws IOException, InterruptedException {
        if (!isAuthorized()) {
            throw new IllegalStateException("Not authorized yet!");
        }
        // GET /v1/tracks/{id}
        String endpoint = "/tracks/" + trackId;
        String responseBody = callSpotifyApi("GET", endpoint, null);
        if (responseBody == null) return null;
        return new JSONObject(responseBody);
    }
    
    public JSONObject getAlbumDetails(String albumId) throws IOException, InterruptedException {
        if (!isAuthorized()) {
            throw new IllegalStateException("Not authorized yet!");
        }
        String endpoint = "/albums/" + albumId;
        String responseBody = callSpotifyApi("GET", endpoint, null);
        if (responseBody == null) return null;
        return new JSONObject(responseBody);
    }
    

    private String callSpotifyApi(String method, String endpoint, String body)
            throws IOException, InterruptedException {
        if (!isAuthorized()) {
            throw new IllegalStateException("No valid access token. Please log in first.");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1" + endpoint))
                .header("Authorization", "Bearer " + accessToken);

        switch (method) {
            case "GET" -> builder.GET();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.noBody());
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.noBody());
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        }

        // If there's a body (rarely needed for these calls)
        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // For 204 "No Content", Spotify returns an empty body
        if (response.statusCode() == 204 || response.body().isEmpty()) {
            return null; // e.g. after next/previous track calls
        }
        return response.body();
    }
}
