package plugin.planarg.hideandseek.events;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import plugin.planarg.hideandseek.managers.GameManager;

public class PlayerUnlockEvent extends Event {
    @Getter
    private Player player;
    @Getter
    private double totalLockTime;
    @Getter
    private double currentTime;

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public PlayerUnlockEvent(Player player, double totalLockTime, double currentTime) {
        this.player = player;
        this.totalLockTime = totalLockTime;
        this.currentTime = currentTime;
    }

    public Location getLocation() {
        return player.getLocation().getBlock().getLocation();
    }

    public Material getDisguiseBlock(Player p) {
        return GameManager.getPlayerData(p).getDisguiseBlock();
    }
}
