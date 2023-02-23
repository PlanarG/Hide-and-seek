package plugin.planarg.hideandseek.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import plugin.planarg.hideandseek.HideAndSeek;
import plugin.planarg.hideandseek.managers.DisguiseManager;
import plugin.planarg.hideandseek.managers.GameManager;

public final class PlayerInteractWithDisguiseListener {
    public static void run() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(Bukkit.getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME),
                PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                int entityID = packet.getIntegers().read(0);
                Player hider = DisguiseManager.getFromDisguiseID(entityID);
                if (hider != null) {
                    if (hider.equals(event.getPlayer())) return;
                    Bukkit.getServer().getLogger().info(hider.getName());
                    packet.getIntegers().write(0, hider.getEntityId());
                    event.setPacket(packet);
                }
            }
        });

        manager.addPacketListener(new PacketAdapter(Bukkit.getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME),
                PacketType.Play.Client.BLOCK_DIG, PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                    Bukkit.getLogger().info("cancel");
                    event.setCancelled(true);
                    return;
                }
                Location location = event.getPacket().getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld());
                if (GameManager.fakeBlockLocations.containsKey(location)) {
                    Player to = GameManager.fakeBlockLocations.get(location);
                    GameManager.damageList.add(new Pair<>(event.getPlayer(), to));
                    event.setCancelled(true);
                }
            }
        });

    }
}
