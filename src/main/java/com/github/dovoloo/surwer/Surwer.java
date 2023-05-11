package com.github.dovoloo.surwer;

import com.github.dovoloo.surwer.listener.MainListener;
import com.github.dovoloo.surwer.utils.GithubUpdateChecker;
import com.github.dovoloo.surwer.utils.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Surwer extends JavaPlugin {

    private final Logger logger =getLogger();
    public static Surwer instance;
    public final HashMap<Player, Inventory> fallBackInventory = new HashMap<>();
    public final HashMap<Player, Inventory> fallBackEnderchest = new HashMap<>();
    public final HashMap<Player, String[]> fallBackData = new HashMap<>();

    private MySQL mySQL;

    /**
     * Start plugin and initialize MySQL connection
     */
    @Override
    public void onEnable() {
        logger.log(Level.INFO, "===============================================");
        logger.log(Level.INFO, "Plugin Surwer by Dovoloo");
        logger.log(Level.INFO, "Version: " + getDescription().getVersion());
        logger.log(Level.INFO, "Status: Enabled");
        logger.log(Level.INFO, "===============================================");
        instance = this;
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops plugin and saves data to MySQL database
     */
    @Override
    public void onDisable() {
        logger.log(Level.INFO, "Saving data...");
        if (mySQL != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                try {
                    mySQL.update(player);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error while saving data to database", e);
                }
            });
        }

        logger.log(Level.INFO, "Data saved!");
        logger.log(Level.INFO, "===============================================");
        logger.log(Level.INFO, "Plugin Surwer by Dovoloo");
        logger.log(Level.INFO, "Version: " + getDescription().getVersion());
        logger.log(Level.INFO, "Status: Disabled");
        logger.log(Level.INFO, "===============================================");
        try {
            if (mySQL != null) mySQL.close();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error while closing MySQL connection! " + e.getMessage());
        }
    }

    /**
     * Load config and initialize MySQL connection
     */
    private void init() throws IOException {
        saveDefaultConfig();
        new GithubUpdateChecker(this).checkForUpdates();
        if (Objects.requireNonNull(getConfig().getString("MySQL.database")).equalsIgnoreCase("database_name")) {
            logger.log(Level.WARNING, "Please setup MySQl in config.yml");
            mySQL = null;
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            mySQL = new MySQL(Objects.requireNonNull(getConfig().getString("MySQL.host")), Objects.requireNonNull(getConfig().getString("MySQL.port")), Objects.requireNonNull(getConfig().getString("MySQL.database")),
                    Objects.requireNonNull(getConfig().getString("MySQL.username")), Objects.requireNonNull(getConfig().getString("MySQL.password")), getConfig().getBoolean("MySQL.useSSL"), getConfig().getBoolean("MySQL.autoReconnect"));
        } catch (Exception e) {
            logger.log(Level.WARNING, "#Error while connecting to MySQL! " + e.getMessage());
            logger.log(Level.WARNING, "#Please check your MySQL settings in config.yml");
            logger.log(Level.WARNING, "#Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        new MainListener(this);
        autoSave();
    }

    /**
     * Auto save data to MySQL database
     */
    private void autoSave() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    mySQL.update(player);
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error while updating player data", e);
                }
            }
        }, 0, 20 * 60 * 5);
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public HashMap<Player, Inventory> getFallBackInventory() {
        return fallBackInventory;
    }

    public HashMap<Player, Inventory> getFallBackEnderchest() {
        return fallBackEnderchest;
    }

    public HashMap<Player, String[]> getFallBackData() {
        return fallBackData;
    }

    public static Surwer getInstance() {
        return instance;
    }
}
