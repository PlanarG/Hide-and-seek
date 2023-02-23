package plugin.planarg.hideandseek.events;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import plugin.planarg.hideandseek.managers.GameManager;

public class PlayerLockEvent extends Event implements Cancellable {
    @Getter
    private Player player;
    @Getter
    private double currentTime;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean isCancelled;

    public PlayerLockEvent(Player player, double currentTime) {
        this.player = player; this.currentTime = currentTime; isCancelled = false;
    }

    public Location getLocation() {
        return player.getLocation().getBlock().getLocation();
    }

    public Material getDisguiseBlock(Player p) {
        return GameManager.getPlayerData(p).getDisguiseBlock();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
