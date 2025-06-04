/*
 * This file is part of the Minecraft Simple Timer project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.codecrafter.smartAfk.SmartAfk;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateChecker {
    private static final String githubApiUrl = "https://api.github.com/repos/IschdeFelin/mc-smart-afk/releases/latest";
    private final SmartAfk plugin;

    private boolean updateAvailable = false;
    private String latestVersion = "";
    private String currentVersion = "";

    public UpdateChecker(SmartAfk plugin) {
        this.plugin = plugin;
    }

    public void check() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(githubApiUrl).toURL().openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setRequestProperty("User-Agent", "SimpleTimerUpdateChecker");

                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    String latestVersion = normalizeVersion(json.get("tag_name").getAsString());
                    String currentVersion = normalizeVersion(plugin.getPluginMeta().getVersion());

                    if (isNewerVersion(currentVersion, latestVersion)) {
                        this.updateAvailable = true;
                        this.latestVersion = latestVersion;
                        this.currentVersion = currentVersion;
                        plugin.getLogger().warning("A new version is available: " + latestVersion + " (you have " + currentVersion + ")");
                    } else {
                        plugin.getLogger().info("You are using the latest version (" + currentVersion + ")");
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String normalizeVersion(String version) {
        return version.replace("v", "").replace("_", "-").trim().toLowerCase();
    }

    private boolean isNewerVersion(String current, String latest) {
        String[] currentParts = current.split("-", 2);
        String currentBase = currentParts[0];
        boolean currentIsPreRelease = currentParts.length > 1;

        String[] latestParts = latest.split("-", 2);
        String latestBase = latestParts[0];
        boolean latestIsPreRelease = latestParts.length > 1;

        String[] currentNumbers = currentBase.split("\\.");
        String[] latestNumbers = latestBase.split("\\.");

        int length = Math.max(currentNumbers.length, latestNumbers.length);
        for (int i = 0; i < length; i++) {
            int curr = i < currentNumbers.length ? parseSafe(currentNumbers[i]) : 0;
            int lat = i < latestNumbers.length ? parseSafe(latestNumbers[i]) : 0;
            if (curr < lat) return true; // A newer version exists -> return true
            if (curr > lat) return false; // Already up to date -> return false
        }

        // If both version are equal -> check if current version is pre-release and latest not
        return currentIsPreRelease && !latestIsPreRelease;
    }

    private int parseSafe(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
