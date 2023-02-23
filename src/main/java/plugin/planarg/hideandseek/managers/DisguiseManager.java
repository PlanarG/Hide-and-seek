package plugin.planarg.hideandseek.managers;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public final class DisguiseManager {
    public static Map<Player, Integer> disguises = new HashMap<>();
    public static Map<Integer, Entity> entityMap = new HashMap<>();

    public static void clearAll() {
        disguises.clear(); entityMap.clear();
    }

    @Nullable
    public static Player getFromDisguiseID(int entityID) {
        for (Map.Entry<Player, Integer> entry : disguises.entrySet()) {
            int id = entry.getValue();
            Player player = entry.getKey();
            if (id == entityID)
                return player;
        }
        return null;
    }

    @Nullable
    public static Entity getFromPlayer(Player p) {
        Integer id = disguises.get(p);
        if (id == null) return null;
        return entityMap.get(id);
    }

    public static boolean isDisguising(Player player) {
        return disguises.containsKey(player);
    }

    public static void putOnDisguise(Player player, Material block) {
        FallingBlock disguise = player.getWorld().spawnFallingBlock(player.getLocation(), block, (byte)0);
        disguise.setGravity(false);
        disguise.setInvulnerable(true);
        disguise.setPersistent(true);
        int id = disguise.getEntityId();
        disguises.put(player, id);
        entityMap.put(id, disguise);
    }

    public static void stopDisguise(Player player) {
        if (disguises.containsKey(player)) {
            int id = disguises.get(player);
            entityMap.get(id).remove();
            entityMap.remove(id);
            disguises.remove(player);
        }
    }
}
