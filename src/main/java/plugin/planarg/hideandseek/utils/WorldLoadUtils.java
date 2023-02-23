package plugin.planarg.hideandseek.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class WorldLoadUtils {

    public static List<String> getFolders(String path) { // returns only folder's name
        List<String> folders = new ArrayList<>();
        File[] list = new File(path).listFiles();
        for (File item : list) {
            if (item.isDirectory())
                folders.add(item.getName());
        }
        return folders;
    }

    public static World loadWorld(String path) {
        return new WorldCreator(path).createWorld();
    }

    public static void unloadWorld(World world) {
        assert world.getPlayers().isEmpty();
        Bukkit.unloadWorld(world, false);
    }

    public static void teleportPlayersTo(World world) {
        for (Player p : Bukkit.getOnlinePlayers())
            p.teleport(world.getSpawnLocation());
    }

}
