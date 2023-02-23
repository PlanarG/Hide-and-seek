package plugin.planarg.hideandseek.settings;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import plugin.planarg.hideandseek.utils.BossBarUtils;

public class PlayerData {
    @Getter
    private Double lastMove;
    @Getter
    private Location lastLocation;
    private Boolean isHider;
    private Boolean isSolidBlock;
    @Getter
    private Material disguiseBlock; // set to null default
    @Getter
    @Setter
    private double lastLockTime;
    @Getter
    private BossBarUtils bar;

    public boolean marked;

    public PlayerData(Player p) {
        lastMove = .0; lastLockTime = 0;
        lastLocation = p.getLocation();
        isHider = false; isSolidBlock = false; marked = false;
        disguiseBlock = null;
        bar = new BossBarUtils();
        bar.createBossBar("&cLock Progress");
        bar.addPlayer(p);
    }

    public Boolean isHider() { return isHider; }
    public Boolean isSolidBlock() { return isSolidBlock; }

    public void setTime(double newTime) {
        lastMove = newTime;
    }

    public PlayerData setHider(boolean flag) {
        isHider = flag;
        return this;
    }

    public PlayerData setSolidBlock(Boolean solidBlock) {
        isSolidBlock = solidBlock;
        return this;
    }

    public void setUnlock() {
        marked = true;
    }

    public PlayerData setDisguiseBlock(Material block) {
        disguiseBlock = block;
        return this;
    }

    public boolean locationChange(Player p) {
        return !p.getLocation().getBlock().getLocation().equals(lastLocation);
    }

    public void syncLastLocation(Player p, double time) {
        Location newLocation = p.getLocation().getBlock().getLocation();
        if (!newLocation.equals(lastLocation)) {
            lastMove = time;
            lastLocation = newLocation;
        }
    }
}
