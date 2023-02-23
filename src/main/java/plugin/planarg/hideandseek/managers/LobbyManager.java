package plugin.planarg.hideandseek.managers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.planarg.hideandseek.HideAndSeek;
import plugin.planarg.hideandseek.events.PlayerEnterLobbyEvent;
import plugin.planarg.hideandseek.settings.ItemInteractions;
import plugin.planarg.hideandseek.utils.BossBarUtils;

import java.util.*;

public class LobbyManager implements Listener {
    private static BossBarUtils bar = new BossBarUtils();
    private static final int PLAYERS_LOWER_LIMIT = 2;
    private static final double MAX_WAITING_TIME = 60;

    private static final String WAITING_TITLE = ChatColor.BOLD + "" + ChatColor.AQUA + "Waiting For Players...";
    private static final String READY = ChatColor.BOLD + "" + ChatColor.YELLOW + "Getting Ready";

    private static boolean active;

    @Getter
    private static String worldName;

    @EventHandler
    public void onPlayerEnter(PlayerEnterLobbyEvent event) {
        bar.addPlayer(event.getPlayer());
    }

    public static void startGame() {
        remove();
        List<Player> hiderList = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers())
            hiderList.add(p);
        hiderList.remove(new Random().nextInt(0, hiderList.size()));
        Map<Player, Material> hiderSet = new HashMap<>();
        for (Player p : hiderList)
            hiderSet.put(p, ItemInteractions.getDisguiseBlock(p));
        String name = ItemInteractions.getMapName();
        for (Player p : Bukkit.getOnlinePlayers())
            WorldSwitcher.switchToWorld(p, WorldSwitcher.INSTANCE.getWorld(name));
        worldName = name;
        GameManager.startGame(hiderSet);
    }

    public static void init() {
        active = true;
        bar = new BossBarUtils();
        bar.createBossBar(WAITING_TITLE);
        bar.setProgress(0);
        new BukkitRunnable() {
            private double totalTime = 0;
            @Override
            public void run() {
                totalTime += 0.05;
                int playerCounter = Bukkit.getOnlinePlayers().size();
                if (playerCounter < PLAYERS_LOWER_LIMIT) {
                    totalTime = 0;
                    bar.setProgress(0);
                    bar.setTitle(WAITING_TITLE);
                } else {
                    double progress = totalTime / MAX_WAITING_TIME;
                    if (progress > 1) progress = 1;
                    bar.setProgress(progress);
                    bar.setTitle(READY);
                    if (totalTime - (int) totalTime < 0.05) {
                        int remain = (int) MAX_WAITING_TIME - (int) totalTime;
                        if (remain % 30 == 0 || remain <= 10) {
                            Bukkit.broadcastMessage(ChatColor.GOLD + "Before game start: " + ChatColor.WHITE + remain +
                                    ChatColor.GOLD + " second");
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 3, 1);
                            }
                        }
                    }
                    if (totalTime >= MAX_WAITING_TIME) {
                        startGame();
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME), 0, 0);
    }

    public static void remove() {
        bar.remove();
        active = false;
    }
}
