package plugin.planarg.hideandseek.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import plugin.planarg.hideandseek.HideAndSeek;
import plugin.planarg.hideandseek.listeners.GameListener;
import plugin.planarg.hideandseek.managers.LobbyManager;

import java.util.ArrayList;

public final class EventsRegister {

    private EventsRegister() {}

    private static ArrayList<Listener> listeners = new ArrayList<>();

    private static void loadListeners() {
        listeners.add(new PlayerEventsHandler());
        listeners.add(new ServerEventsHandler());
        listeners.add(new GameListener());
        listeners.add(new LobbyManager());
    }

    public static void register() {
        loadListeners();
        Bukkit.getServer().getLogger().info("Loaded " + listeners.size() + " listeners.");
        for (int i = 0; i < listeners.size(); i++) {
            Bukkit.getServer().getPluginManager().registerEvents(listeners.get(i),
                    Bukkit.getServer().getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME));
        }
    }



}
