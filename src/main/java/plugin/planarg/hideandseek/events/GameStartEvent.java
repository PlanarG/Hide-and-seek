package plugin.planarg.hideandseek.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import plugin.planarg.hideandseek.managers.GameManager;
import plugin.planarg.hideandseek.settings.PlayerData;

import java.util.List;

public class GameStartEvent extends Event {
    @Getter
    private List<Player> hiders;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public GameStartEvent(List<Player> hiders) {
        this.hiders = hiders;
    }

    public PlayerData getPlayerData(Player p) {
        return GameManager.getPlayerData(p);
    }

    public void changeToSeeker(Player p) {
        if (hiders.contains(p)) {
            GameManager.removeHider(p);
        }
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
