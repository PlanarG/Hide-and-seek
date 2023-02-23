package plugin.planarg.hideandseek.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerEnterLobbyEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private Player player;

    public PlayerEnterLobbyEvent(Player player) {
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
