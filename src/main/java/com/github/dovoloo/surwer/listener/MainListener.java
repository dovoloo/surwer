package com.github.dovoloo.surwer.listener;

import com.github.dovoloo.surwer.Surwer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Level;

public class MainListener implements Listener {

    private final Surwer plugin;
    public MainListener(@NotNull Surwer plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = Objects.requireNonNull(plugin.getConfig().getString("prefix")).replace("&", "§");
        try {
            if (!plugin.getMySQL().isRegistered(player.getUniqueId().toString())) {
                plugin.getMySQL().register(player);
                return;
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(prefix + "§6Loading data..."));
            saveFallBackData(player);
            player.getInventory().clear();
            player.getEnderChest().clear();
            plugin.getMySQL().loadData(player);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(prefix + "§6Data downloaded!"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error while loading data from database", e);
            player.sendMessage(prefix + "§cError while loading data from database");
            loadFallBackData(player);
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        try {
            plugin.getMySQL().update(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error while saving data to database", e);
        }
    }

    protected void saveFallBackData(@NotNull Player player) {
        plugin.getFallBackInventory().put(player, player.getInventory());
        plugin.getFallBackEnderchest().put(player, player.getEnderChest());
        plugin.getFallBackData().put(player, new String[]{String.valueOf(player.getExp()), String.valueOf(player.getLevel()), String.valueOf(player.getFoodLevel()),
                String.valueOf(player.getSaturation())});
    }

    protected void loadFallBackData(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().setContents(plugin.getFallBackInventory().get(player).getContents());
        player.getEnderChest().clear();
        player.getEnderChest().setContents(plugin.getFallBackEnderchest().get(player).getContents());
        player.setExp(Float.parseFloat(plugin.getFallBackData().get(player)[0]));
        player.setLevel(Integer.parseInt(plugin.getFallBackData().get(player)[1]));
        player.setFoodLevel(Integer.parseInt(plugin.getFallBackData().get(player)[2]));
        player.setSaturation(Float.parseFloat(plugin.getFallBackData().get(player)[3]));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }
}