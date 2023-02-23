package plugin.planarg.hideandseek.managers;

import com.comphenix.protocol.wrappers.BlockPosition;
import plugin.planarg.hideandseek.utils.ConfigUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickableManager {

    private static Map<String, ConfigUtils> genLocations = new HashMap<>();

    public static void init() {
        for (String s : WorldSwitcher.INSTANCE.getWorldNameList()) {
            genLocations.put(s, new ConfigUtils(new File("config/" + s + ".json")));
        }
    }

    public static List<BlockPosition> getLocations(String world) {
        return genLocations.get(world).getLoc();
    }
}
