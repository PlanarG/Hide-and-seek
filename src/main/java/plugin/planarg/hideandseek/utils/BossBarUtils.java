package plugin.planarg.hideandseek.utils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarUtils {
    @Getter
    private BossBar bossBar;

    public BossBarUtils() {

    }

    public void addPlayer(Player p) {
        bossBar.addPlayer(p);
    }

    public void remove() {
        bossBar.removeAll();
    }

    public void createBossBar(String title) {
        bossBar = Bukkit.createBossBar(format(title), BarColor.PINK, BarStyle.SOLID);
        bossBar.setProgress(0);
        bossBar.setVisible(true);
    }

    public void setTitle(String title) {
        bossBar.setTitle(format(title));
    }

    private String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void setProgress(double progress) {
        bossBar.setProgress(progress);
    }
}
