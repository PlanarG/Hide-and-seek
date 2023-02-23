package plugin.planarg.hideandseek.props;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.planarg.hideandseek.managers.GameManager;
import plugin.planarg.hideandseek.settings.IPropsInfo;
import plugin.planarg.hideandseek.settings.PlayerType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SeekerCompass implements IPropsInfo {

    @Override
    public void useItem(Player player) {
        double minDistance = Double.MAX_VALUE;
        for (Player p : Bukkit.getOnlinePlayers())
            if (GameManager.getPlayerType(p) == PlayerType.HIDDER) {
                double distance = p.getLocation().distance(player.getLocation());
                if (distance < minDistance) minDistance = distance;
            }
        int distance = (int) minDistance;
        player.sendMessage(ChatColor.GOLD + "The nearest hider is " + ChatColor.AQUA + distance + ChatColor.GOLD + " blocks away.");
    }

    @Override
    public String getName() {
        return "Seeker Compass";
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getName());
        item.setItemMeta(meta);
        return item;
    }

    @Nullable
    @Override
    public List<PlayerType> getWhoCanPickup() {
        return new ArrayList<>();
    }

    @Override
    public Integer getCD() {
        return 25;
    }

    @Override
    public boolean isDisposable() {
        return false;
    }
}
