package com.github.dovoloo.surwer.utils;

import com.github.dovoloo.surwer.Surwer;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Arrays;

public class MySQL {

    /**
     * The connection
     */
    private final Connection connection;

    /**
     * Creates a new MySQL instance
     *
     * @param host the host
     * @param port the port
     * @param database the database
     * @param username the username
     * @param password the password
     * @param autoReconnect if the connection should be auto reconnected
     * @param useSSL if SSL should be used
     * @throws SQLException if an error occurs
     */
    public MySQL(@NotNull String host, @NotNull String port, @NotNull String database, @NotNull String username, @NotNull String password, boolean autoReconnect, boolean useSSL) throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=" + autoReconnect + "&useSSL=" + useSSL, username, password);
        PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `player_data` (`uuid` VARCHAR(36) PRIMARY KEY, `inventory` TEXT NOT NULL, `ender_chest` TEXT NOT NULL, `xp` FLOAT NOT NULL, `health` DOUBLE NOT NULL, `hunger` INT NOT NULL, `saturation` FLOAT NOT NULL, `level` INT NOT NULL, `gamemode` ENUM('SURVIVAL', 'CREATIVE', 'ADVENTURE', 'SPECTATOR') NOT NULL);");
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Checks if a player is registered in the database
     *
     * @param uuid the player's uuid
     * @return true, if the player is registered otherwise false
     * @throws SQLException if an error occurs
     */
    public boolean isRegistered(@NotNull String uuid) throws SQLException {
        if (isConnected()) return false;
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `player_data` WHERE `uuid` = ?");
        statement.setString(1, uuid);
        ResultSet resultSet = statement.executeQuery();
        boolean result = resultSet.next();
        resultSet.close();
        statement.close();
        return result;
    }


    /**
     * Registers a player in the database for the first time
     *
     * @param player the player to register
     * @throws SQLException if an error occurs
     */
    public void register(@NotNull Player player) throws SQLException {
        if (isConnected()) return;
        PreparedStatement statement = connection.prepareStatement("INSERT INTO `player_data` (`uuid`, `inventory`, `ender_chest`, `xp`, `health`, `hunger`, `saturation`, `level`, `gamemode`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, ItemStackSerializer.toBase64(Arrays.asList(player.getInventory().getContents())));
        statement.setString(3, ItemStackSerializer.toBase64(Arrays.asList(player.getEnderChest().getContents())));
        statement.setFloat(4, player.getExp());
        statement.setDouble(5, player.getHealth());
        statement.setInt(6, player.getFoodLevel());
        statement.setFloat(7, player.getSaturation());
        statement.setInt(8, player.getLevel());
        statement.setString(9, player.getGameMode().toString());
        statement.executeUpdate();
        statement.close();
    }


    /**
     * Update the player's data in the database if the player is registered
     *
     * @param player the player to update
     * @throws SQLException if an error occurs
     */
    public void update(@NotNull Player player) throws SQLException {
        if (isConnected()) return;
        PreparedStatement statement = connection.prepareStatement("UPDATE `player_data` SET `inventory` = ?, `ender_chest` = ?, `xp` = ?, `health` = ?, `hunger` = ?, `saturation` = ?, `level` = ?, `gamemode` = ? WHERE `uuid` = ?");
        statement.setString(1, ItemStackSerializer.toBase64(Arrays.asList(player.getInventory().getContents())));
        statement.setString(2, ItemStackSerializer.toBase64(Arrays.asList(player.getEnderChest().getContents())));
        statement.setFloat(3, player.getExp());
        statement.setDouble(4, player.getHealth());
        statement.setInt(5, player.getFoodLevel());
        statement.setFloat(6, player.getSaturation());
        statement.setInt(7, player.getLevel());
        statement.setString(8, player.getGameMode().toString());
        statement.setString(9, player.getUniqueId().toString());
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Loads the player's data from the database if the player is registered
     * and applies it to the player
     *
     * @param player the player to load the data for
     */
    public void loadData(Player player) {
        if (isConnected()) return;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `player_data` WHERE `uuid` = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5, 1);
                player.getInventory().setContents(ItemStackSerializer.fromBase64(resultSet.getString("inventory")).toArray(new ItemStack[0]));
                player.getEnderChest().setContents(ItemStackSerializer.fromBase64(resultSet.getString("ender_chest")).toArray(new ItemStack[0]));
                player.setExp(resultSet.getFloat("xp"));
                player.setHealth(resultSet.getDouble("health"));
                player.setFoodLevel(resultSet.getInt("hunger"));
                player.setSaturation(resultSet.getFloat("saturation"));
                player.setLevel(resultSet.getInt("level"));
                player.setGameMode(GameMode.valueOf(resultSet.getString("gamemode")));
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 1);
            Surwer.getInstance().getFallBackData().remove(player);
            Surwer.getInstance().getFallBackEnderchest().remove(player);
            Surwer.getInstance().getFallBackInventory().remove(player);
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the connection to the database if it is not already closed or null
     * Only call this method if you don't need the connection anymore
     *
     * @throws SQLException if an error occurs
     */
    public void close() throws SQLException {
        if (isConnected()) return;
        connection.close();
    }

    /**
     * Checks if the connection is closed or null
     *
     * @return true, if the connection is closed or null otherwise false
     */
    private boolean isConnected() {
        return connection == null;
    }
}