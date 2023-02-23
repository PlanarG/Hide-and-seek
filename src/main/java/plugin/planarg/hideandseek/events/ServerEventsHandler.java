package plugin.planarg.hideandseek.events;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import plugin.planarg.hideandseek.HideAndSeek;
import plugin.planarg.hideandseek.listeners.PlayerInteractWithDisguiseListener;
import plugin.planarg.hideandseek.managers.DisguiseManager;
import plugin.planarg.hideandseek.managers.GameManager;
import plugin.planarg.hideandseek.managers.WorldSwitcher;

import java.util.Map;

public class ServerEventsHandler implements Listener {

    private static void runCommand(String commandLine) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), commandLine);
    }
    @EventHandler
    public void onServerLoad(ServerLoadEvent e) {
        runCommand("gamerule fallDamage false");
        runCommand("gamerule keepInventory true");
        runCommand("gamerule doMobSpawning false");

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME),
                new Runnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<Player, Integer> entry : DisguiseManager.disguises.entrySet()) {
                            int id = entry.getValue();
                            Entity entity = DisguiseManager.entityMap.get(id);
                            entity.setTicksLived(1);
                        }
                        for (Map.Entry<Player, FallingBlock> entry : GameManager.tmpFallingBlock.entrySet()) {
                            entry.getValue().setTicksLived(1);
                        }
                    }
                }, 0, 100);
        PlayerInteractWithDisguiseListener.run();
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        e.setCancelled(true);
    }

}
