package com.github.dovoloo.surwer.utils;

import com.github.dovoloo.surwer.Surwer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class GithubUpdateChecker {

    private final Surwer plugin;
    private final String version;
    public GithubUpdateChecker(@NotNull Surwer plugin) {
        this.plugin = plugin;
        this.version = plugin.getDescription().getVersion();
    }

    public void checkForUpdates() throws IOException {
        URL obj = new URL("https://raw.githubusercontent.com/dovoloo/surwer/master/version.txt");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(
                new java.io.InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        con.disconnect();

        if (!Objects.equals(version, response.toString())) {
            sendNotification(response.toString());
        } else {
            plugin.getLogger().info("Plugin is up to date!");
        }
    }

    protected void sendNotification(@NotNull String newVersion) {
        plugin.getLogger().info("New version available: " + newVersion);
        plugin.getLogger().info("Download it at: https://www.spigotmc.org/resources/surwer.107958/");
    }

}