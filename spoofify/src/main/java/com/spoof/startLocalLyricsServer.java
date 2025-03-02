// package com.spoof;

// /**
//  * Start the local lyrics server (php -S localhost:8080 api/index.php).
//  * Adjust paths as needed for your environment.
//  */
// private Process startLocalLyricsServer() throws IOException {
//     // Root folder for the 'spotify-lyrics-api-main' repository
//     // Make sure it points to your actual directory
//     String rootFolder = "spotify-lyrics-api-main";

//     // Path to the 'api/index.php' file (relative to rootFolder)
//     String indexFile = "api/index.php";

//     // Launch command: php -S localhost:8080 api/index.php
//     ProcessBuilder pb = new ProcessBuilder(
//         "php", 
//         "-S", 
//         "localhost:8080", 
//         indexFile
//     );

//     // Set the working directory to the root of the repo
//     pb.directory(new java.io.File(rootFolder));

//     // If you need SP_DC for the server, set it here:
//     // pb.environment().put("SP_DC", "YOUR_SP_DC_COOKIE_HERE");

//     // Inherit I/O so you can see server logs in the console
//     pb.inheritIO();

//     // Start the process
//     Process process = pb.start();
//     System.out.println("Local lyrics server launched on http://localhost:8080");
//     return process;
// }
