package plugin.planarg.hideandseek.managers;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.planarg.hideandseek.props.SeekerCompass;
import plugin.planarg.hideandseek.settings.IPropsInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PropsManager {
    private static List<IPropsInfo> propsList = new ArrayList<>();
    private static Map<Player, Map<ItemStack, Double>> lastUse = new HashMap<>();

    public static List<IPropsInfo> spawnList = new ArrayList<>(), hiderInitProps = new ArrayList<>(), seekerInitProps = new ArrayList<>();

    @Nullable
    private static IPropsInfo getItemInfo(ItemStack item) {
        for (IPropsInfo p : propsList) {
            if (p.getItem().getType().equals(item.getType())) {
                return p;
            }
        }
        return null;
    }

    public static boolean canPickupItem(Player player, ItemStack item) {
        IPropsInfo info = getItemInfo(item);
        if (info == null) return false;
        if (!spawnList.contains(info)) return false;
        if (info.getWhoCanPickup() == null) return false;
        return info.getWhoCanPickup().contains(GameManager.getPlayerType(player));
    }

    private static Double getLastUse(Player player, ItemStack item) {
        if (!lastUse.containsKey(player))
            return -1000.;
        if (!lastUse.get(player).containsKey(item))
            return -1000.;
        return lastUse.get(player).get(item);
    }

    private static void setLastUse(Player player, ItemStack item, Double time) {
        if (!lastUse.containsKey(player))
            lastUse.put(player, new HashMap<>());
        lastUse.get(player).put(item, time);
    }

    private static ItemStack normalize(ItemStack item) {
        return new ItemStack(item.getType());
    }

    public static Double useItem(Player player, ItemStack item, Double currentTime) {
        ItemStack norm = normalize(item);
        Double last = getLastUse(player, norm);
        IPropsInfo info = getItemInfo(norm);
        if (info == null) return -.1;
        if (info.isDisposable()) {
            info.useItem(player);
            info.getItem().setAmount(info.getItem().getAmount() - 1);
            return .0;
        }
        if (last + info.getCD() >= currentTime) {
            return info.getCD() - (currentTime - last);
        }
        setLastUse(player, norm, currentTime);
        info.useItem(player);
        return .0;
    }

    public static void init() {
        propsList.add(new SeekerCompass()); seekerInitProps.add((new SeekerCompass()));
    }

    public static void reload() {
        lastUse.clear();
    }
}
