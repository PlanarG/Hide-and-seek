package plugin.planarg.hideandseek.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.planarg.hideandseek.HideAndSeek;
import plugin.planarg.hideandseek.events.*;
import plugin.planarg.hideandseek.managers.GameManager;
import plugin.planarg.hideandseek.managers.LobbyManager;
import plugin.planarg.hideandseek.managers.PropsManager;
import plugin.planarg.hideandseek.managers.WorldSwitcher;
import plugin.planarg.hideandseek.settings.IPropsInfo;
import plugin.planarg.hideandseek.settings.ItemCollections;
import plugin.planarg.hideandseek.settings.ItemInteractions;
import plugin.planarg.hideandseek.utils.FireworksUtils;

import java.util.List;

public class GameListener implements Listener {
    @EventHandler
    public void onGameStart(GameStartEvent event) {
        ItemInteractions.init();
        ItemCollections.init();
        List<Player> hiders = event.getHiders();
        Bukkit.getOnlinePlayers().forEach((Player p) -> {
            String subtitle = "";
            p.getInventory().clear();
            if (hiders.contains(p)) {
                subtitle = ChatColor.GREEN + "You are a HIDER.";
                Material block = GameManager.getPlayerData(p).getDisguiseBlock();
                p.getInventory().setItem(8, new ItemStack(block));
                int count = 0;
                for (IPropsInfo item : PropsManager.hiderInitProps) {
                    p.getInventory().setItem(++count, item.getItem());
                }
            } else {
                subtitle = ChatColor.RED + "You are a SEEKER";
                GameManager.initializeAsSeeker(p);
            }
            p.sendTitle("Game Start", subtitle);

        });
    }

    @EventHandler
    public void onPlayerLock(PlayerLockEvent event) {
        Player p = event.getPlayer();
        ItemStack item = p.getInventory().getItem(8);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        p.getInventory().setItem(8, item);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3, 1);
    }

    @EventHandler
    public void onPlayerUnlock(PlayerUnlockEvent event) {
        Player p = event.getPlayer();
        ItemStack item = p.getInventory().getItem(8);
        item.removeEnchantment(Enchantment.DURABILITY);
        p.getInventory().setItem(8, item);
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        List<Player> survivors = event.getSurvivors();
        if (!survivors.isEmpty()) {
            String message = "";
            for (Player p : survivors) {
                message += GameManager.HIDER_PREFIX + p.getName() + " ";
            }
            if (survivors.size() == 1) {
                message += "is the only winner, congratulations!";
            } else {
                message += "are winners, congratulations!";
            }
            Bukkit.broadcastMessage(message);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            FireworksUtils.spawnRandomFirework(p.getLocation());
            if (survivors.isEmpty())
                p.sendTitle(ChatColor.RED + "Seekers " + ChatColor.WHITE + "win", "GG");
            else
                p.sendTitle(ChatColor.GREEN + "Hiders " + ChatColor.WHITE + "win", "GG");
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemCollections.init();
                ItemInteractions.init();
                LobbyManager.init();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    PlayerEventsHandler.resetPlayer(p);
                    Boolean flag = p.getWorld().getName().contains("Lobby");
                    WorldSwitcher.switchToWorld(p, WorldSwitcher.INSTANCE.getWorld("Lobby"));
                    if (flag) Bukkit.getPluginManager().callEvent(new PlayerEnterLobbyEvent(p));
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME), 20 * 10);
    }
}
