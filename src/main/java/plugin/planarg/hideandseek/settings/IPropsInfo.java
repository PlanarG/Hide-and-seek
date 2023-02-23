package plugin.planarg.hideandseek.settings;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface IPropsInfo {
    void useItem(Player player);
    String getName();
    ItemStack getItem();
    @Nullable
    List<PlayerType> getWhoCanPickup();
    Integer getCD();
    boolean isDisposable();
}
