package plugin.planarg.hideandseek;

import org.bukkit.plugin.java.JavaPlugin;
import plugin.planarg.hideandseek.commands.Start;
import plugin.planarg.hideandseek.events.EventsRegister;
import plugin.planarg.hideandseek.managers.LobbyManager;
import plugin.planarg.hideandseek.managers.PickableManager;
import plugin.planarg.hideandseek.managers.PropsManager;
import plugin.planarg.hideandseek.managers.WorldSwitcher;
import plugin.planarg.hideandseek.settings.ItemCollections;
import plugin.planarg.hideandseek.settings.ItemInteractions;

public final class HideAndSeek extends JavaPlugin {

    public static final String PLUGIN_NAME = "HideAndSeek";

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Hello world");
        EventsRegister.register();
        WorldSwitcher.INSTANCE = new WorldSwitcher("custom_worlds/");
        for (String name : WorldSwitcher.INSTANCE.getWorldNameList()) {
            getLogger().info(name);
        }
        ItemCollections.init();
        ItemInteractions.init();
        LobbyManager.init();
        PropsManager.init();
        PickableManager.init();
        getCommand("start").setExecutor(new Start());
    }
}
