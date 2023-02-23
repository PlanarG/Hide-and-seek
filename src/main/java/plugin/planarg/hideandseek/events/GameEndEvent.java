package plugin.planarg.hideandseek.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class GameEndEvent extends Event {
    @Getter
    private double totalPlayTime;

    @Getter
    private List<Player> survivors;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public GameEndEvent(List<Player> survivors, double totalPlayTime) {
        this.survivors = survivors; this.totalPlayTime = totalPlayTime;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
