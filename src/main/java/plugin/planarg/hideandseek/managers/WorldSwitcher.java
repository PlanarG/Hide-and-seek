package plugin.planarg.hideandseek.managers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import plugin.planarg.hideandseek.utils.WorldLoadUtils;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldSwitcher {
    public static WorldSwitcher INSTANCE;
    @Getter
    private List<String> worldNameList;
    private final String PATH;

    public WorldSwitcher(String PATH) {
        this.PATH = PATH;
        this.worldNameList = WorldLoadUtils.getFolders(PATH);
    }

    public World getWorld(String name) {
        String fullName = PATH + name;
        if (Bukkit.getWorld(fullName) != null) {
            return Bukkit.getWorld(fullName);
        } else {
            World world = WorldLoadUtils.loadWorld(fullName);
            return world;
        }
    }

    public static void switchToWorld(Player player, World world) {
        player.teleport(world.getSpawnLocation());
    }

}
