package plugin.planarg.hideandseek.settings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.planarg.hideandseek.managers.WorldSwitcher;

import javax.annotation.Nullable;
import java.util.*;

public class ItemInteractions {

    private static Map<Player, Integer> voteMap = new HashMap<>();
    private static Map<Player, Material> disguiseBlock = new HashMap<>();
    private static List<Integer> voteCounter = new ArrayList<>();

    public static void init() {
        int tot = WorldSwitcher.INSTANCE.getWorldNameList().size() + 1;
        voteMap.clear(); voteCounter.clear(); disguiseBlock.clear();
        for (int i = 0; i < tot; i++)
            voteCounter.add(0);
        for (String s : WorldSwitcher.INSTANCE.getWorldNameList()) {
            Bukkit.getLogger().info(s);
        }
        Bukkit.getLogger().info("voteCounter: " + voteCounter.size());
    }

    public static Material getDisguiseBlock(Player player) {
        if (disguiseBlock.containsKey(player))
            return disguiseBlock.get(player);
        return Material.BEDROCK;
    }

    public static String getMapName() {
        List<Integer> maxPositions = new ArrayList<>();
        int max = 0;
        for (int i = 0; i < voteCounter.size(); i++) {
            if (voteCounter.get(i) > max) {
                max = voteCounter.get(i);
                maxPositions.clear();
            }
            if (voteCounter.get(i) == max)
                maxPositions.add(i);
        }
        int index = maxPositions.get(new Random().nextInt(0, maxPositions.size()));
        if (index == voteCounter.size() - 1)
            index = new Random().nextInt(0, voteCounter.size() - 1);
        Bukkit.getLogger().info("index: " + index + " " + voteCounter.size());
        return WorldSwitcher.INSTANCE.getWorldNameList().get(index);
    }

    public static void changeDisguiseBlock(Player player, Material block) {
        disguiseBlock.put(player, block);
    }

    private static void changeMapVotes(int index, int votes) {
        ItemStack tmp = ItemCollections.maps.get(index);
        ItemMeta meta = tmp.getItemMeta();
        String name = "";
        if (index == voteCounter.size() - 1)
            name = ChatColor.BOLD + "Random(?)";
        else
            name = WorldSwitcher.INSTANCE.getWorldNameList().get(index);
        name += ChatColor.AQUA + " " + votes + " Votes";
        meta.setDisplayName(name);
        tmp.setItemMeta(meta);
        ItemCollections.menu_content.setItem(index, tmp);
    }

    public static void voteFor(Player p, int index) {
        Integer last = lastVote(p);
        if (last != null) {
            voteCounter.set(last, voteCounter.get(last) - 1);
            changeMapVotes(last, voteCounter.get(last));
        }
        voteCounter.set(index, voteCounter.get(index) + 1);
        changeMapVotes(index, voteCounter.get(index));
        voteMap.put(p, index);
    }

    @Nullable
    private static Integer lastVote(Player p) {
        return voteMap.get(p);
    }
}
