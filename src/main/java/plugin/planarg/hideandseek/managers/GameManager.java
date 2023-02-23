package plugin.planarg.hideandseek.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import plugin.planarg.hideandseek.HideAndSeek;
import plugin.planarg.hideandseek.settings.IPropsInfo;
import plugin.planarg.hideandseek.settings.ItemCollections;
import plugin.planarg.hideandseek.settings.PlayerData;
import plugin.planarg.hideandseek.settings.PlayerType;
import plugin.planarg.hideandseek.utils.ScoreBoardUtils;
import plugin.planarg.hideandseek.events.GameEndEvent;
import plugin.planarg.hideandseek.events.GameStartEvent;
import plugin.planarg.hideandseek.events.PlayerLockEvent;
import plugin.planarg.hideandseek.events.PlayerUnlockEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class GameManager {
    public static double timeSinceStart = 0;
    public final static double MAX_GAME_TIME = 10 * 60;
    public final static double LOCK_TIME = 5;

    private static Map<Player, PlayerData> playerMapping = new HashMap<>();
    public static Map<Player, FallingBlock> tmpFallingBlock = new HashMap<>();
    public static Map<Location, Player> fakeBlockLocations = new HashMap<>();

    public static List<Pair<Player, Player>> damageList = new ArrayList<>();

    private static ScoreboardManager manager = Bukkit.getScoreboardManager();
    @Getter
    private static Scoreboard board = manager.getNewScoreboard();

    public static final String HIDER_PREFIX = ChatColor.GREEN + "[HIDER] " + ChatColor.WHITE;
    public static final String SEEKER_PREFIX = ChatColor.RED + "[SEEKER] " + ChatColor.WHITE;

    private static void clearAll() {
        DisguiseManager.clearAll(); playerMapping.clear(); tmpFallingBlock.clear(); fakeBlockLocations.clear();
        timeSinceStart = 0;
    }

    private static void spawnProps() {
        String name = LobbyManager.getWorldName();
        List<BlockPosition> locations = PickableManager.getLocations(name);
        List<IPropsInfo> props = PropsManager.spawnList;
        World world = WorldSwitcher.INSTANCE.getWorld(name);
        for (BlockPosition p : locations) {
            IPropsInfo gen = props.get(new Random().nextInt(0, props.size()));
            world.dropItem(p.toLocation(world), gen.getItem());
            world.playEffect(p.toLocation(world), Effect.BLAZE_SHOOT, 5);
        }
    }

    private static void update() { // invoked every tick
        timeSinceStart += 0.05;
        damageList.forEach((Pair<Player, Player> t) -> {
            t.getFirst().attack(t.getSecond());
        });
        damageList.clear();
        getHiders().forEach((Player p) -> {
            if (willPlayerUnlock(p)) unlockPlayer(p);
            if (willPlayerLock(p)) lockPlayer(p);
            double originTime = timeSinceStart - getPlayerData(p).getLastMove();
            double intervalTime = Math.floor(originTime);
            if (Math.abs(originTime - intervalTime) <= 0.05 && originTime >= 2 && originTime < 5) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 3, 0);
            }
            getPlayerData(p).getBar().setProgress(Math.min(1, intervalTime / LOCK_TIME));
        });
        Bukkit.getServer().getOnlinePlayers().forEach((Player p) -> {
            playerMapping.get(p).syncLastLocation(p, timeSinceStart);
        });
        int hiders = getHiders().size();
        int seekers = Bukkit.getOnlinePlayers().size() - hiders;
        int time = (int) Math.floor(timeSinceStart);
        ScoreBoardUtils.updateBoard(board.getObjective("panel"), hiders, seekers, time);
        if (timeSinceStart - time < 0.05) {
            if (time % 600 == 0)
                spawnProps();
        }
    }

    private static void start(Map<Player, Material> hiderSet) {
        Team hider = board.registerNewTeam("Hider");
        Team seeker = board.registerNewTeam("Seeker");

        hider.setPrefix(HIDER_PREFIX);
        seeker.setPrefix(SEEKER_PREFIX);

        hider.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        hider.setCanSeeFriendlyInvisibles(false);
        hider.setNameTagVisibility(NameTagVisibility.NEVER);

        int hiders = hiderSet.size();
        int seekers = Bukkit.getOnlinePlayers().size() - hiders;

        ScoreBoardUtils.createBoard(hiders, seekers, 0);

        Bukkit.getOnlinePlayers().forEach((Player p) -> {
            PlayerData data = new PlayerData(p);
            if (hiderSet.containsKey(p)) {
                hider.addPlayer(p);
                data.setHider(true).setDisguiseBlock(hiderSet.get(p));
                DisguiseManager.putOnDisguise(p, hiderSet.get(p));
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            } else seeker.addPlayer(p);
            playerMapping.put(p, data);
            ScoreBoardUtils.sendToPlayer(p);
        });

        Bukkit.getPluginManager().callEvent(new GameStartEvent(getHiders()));
    }

    public static PlayerData getPlayerData(Player p) {
        return playerMapping.get(p);
    }

    public static boolean isGameEnd() {
        if (timeSinceStart >= MAX_GAME_TIME) return true;
        if (getHiders().isEmpty() || getHiders().size() == Bukkit.getOnlinePlayers().size()) return true;
        return false;
    }

    public static void playerDied(Player p, Player from) {
        p.setHealth(20);
        if (getHiders().contains(p)) {
            Bukkit.broadcastMessage(HIDER_PREFIX + p.getDisplayName() + " was found by " + SEEKER_PREFIX + from.getDisplayName());
            initializeAsSeeker(p);
            removeHider(p);
        } else {
            Bukkit.broadcastMessage(SEEKER_PREFIX + p.getDisplayName() + " was slaughtered by " + HIDER_PREFIX + from.getDisplayName());
        }
    }

    public static void playerDied(Player p) {
        p.setHealth(20);
        p.teleport(p.getWorld().getSpawnLocation());
        if (getHiders().contains(p)) {
            Bukkit.broadcastMessage(HIDER_PREFIX + p.getDisplayName() + " died");
            initializeAsSeeker(p);
            removeHider(p);
        } else {
            System.out.println(p.getWorld().getName());
            if (!p.getWorld().getName().contains("Lobby"))
                Bukkit.broadcastMessage(SEEKER_PREFIX + p.getDisplayName() + " died");
        }
    }

    private static void end() {
        GameEndEvent event = new GameEndEvent(getHiders(), timeSinceStart);
        Bukkit.getPluginManager().callEvent(event);
        getHiders().forEach(GameManager::removeHider);
        if (board.getTeam("Hider") != null)
            board.getTeam("Hider").unregister();
        if (board.getTeam("Seeker") != null)
            board.getTeam("Seeker").unregister();
        board = manager.getNewScoreboard();
        Scoreboard finalBoard = board;
        Bukkit.getOnlinePlayers().forEach((Player p) -> {
            p.setScoreboard(finalBoard);
            getPlayerData(p).getBar().remove();
        });
        PropsManager.reload();
        clearAll();
    }

    public static ArrayList<Player> getHiders() {
        ArrayList<Player> res = new ArrayList<>();
        for (Map.Entry<Player, PlayerData> entry : playerMapping.entrySet()) {
            if (entry.getValue().isHider())
                res.add(entry.getKey());
        }
        return res;
    }

    private static boolean willPlayerLock(Player p) {
        PlayerData data = playerMapping.get(p);
        if (data.locationChange(p)) return false;
        if (data.getLastMove() + LOCK_TIME >= timeSinceStart) return false;
        if (data.isSolidBlock()) return false;
        Block block = p.getLocation().getBlock();
        if (block.getType() != Material.AIR) {
            if (timeSinceStart % 5 < 0.05)
            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You can't hide here!");
            return false;
        }
        PlayerLockEvent event = new PlayerLockEvent(p, timeSinceStart);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static boolean willPlayerUnlock(Player p) {
        PlayerData data = playerMapping.get(p);
        if (!data.isSolidBlock()) return false;
        if (data.marked || playerMapping.get(p).locationChange(p)) {
            if (data.marked) {
                data.marked = false;
                data.setTime(timeSinceStart);
            }
            PlayerUnlockEvent event = new PlayerUnlockEvent(p, timeSinceStart - data.getLastLockTime(), timeSinceStart);
            Bukkit.getPluginManager().callEvent(event);
            return true;
        }
        return false;
    }

    public static void initializeAsSeeker(Player p) {
        PlayerInventory inventory = p.getInventory();
        inventory.setItem(0, ItemCollections.seeker_sword);
        inventory.setItem(1, ItemCollections.seeker_bow);
        inventory.setItem(2, new ItemStack(Material.ARROW));
        int count = 2;
        for (IPropsInfo item : PropsManager.seekerInitProps) {
            inventory.setItem(++count, item.getItem());
        }
        inventory.setItem(EquipmentSlot.HEAD, ItemCollections.seeker_helmet);
        inventory.setItem(EquipmentSlot.CHEST, ItemCollections.seeker_chestplate);
        inventory.setItem(EquipmentSlot.LEGS, ItemCollections.seeker_leggings);
        inventory.setItem(EquipmentSlot.FEET, ItemCollections.seeker_boots);
        // TODO : teleport this seeker
    }

    private static void lockPlayer(Player p) {
        DisguiseManager.stopDisguise(p);
        FallingBlock tmp = p.getWorld().spawnFallingBlock(p.getLocation(),
                playerMapping.get(p).getDisguiseBlock(), (byte)0);
        tmp.setGravity(false);
        tmp.setInvulnerable(true);
        tmp.setPersistent(true);
        tmpFallingBlock.put(p, tmp);
        Bukkit.getServer().getOnlinePlayers().stream().filter((Player player) ->
                (!player.equals(p))).forEach((Player player) -> {
            player.hideEntity(Bukkit.getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME), tmp);
        });
        Location location = p.getLocation();
        fakeBlockLocations.put(location.getBlock().getLocation(), p);
        Material block = playerMapping.get(p).getDisguiseBlock();
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        packet.getBlockPositionModifier().write(0,
                new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        packet.getBlockData().write(0, WrappedBlockData.createData(block));
        Bukkit.getServer().getOnlinePlayers().stream().filter((Player player) ->
                (!player.equals(p))).forEach((Player player) -> {
            try {
                manager.sendServerPacket(player, packet, false);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        playerMapping.get(p).setSolidBlock(true).setLastLockTime(timeSinceStart);
    }

    private static void unlockPlayer(Player p) {
        System.out.println(p.getName() + " unlock");
        DisguiseManager.putOnDisguise(p, playerMapping.get(p).getDisguiseBlock());
        Location location = playerMapping.get(p).getLastLocation();
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        packet.getBlockPositionModifier().write(0, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        packet.getBlockData().write(0, WrappedBlockData.createData(Material.AIR));
        Bukkit.getServer().getOnlinePlayers().stream().filter((Player player) -> (!player.equals(p))).forEach((Player player) -> {
            try {
                manager.sendServerPacket(player, packet, false);
            } catch (InvocationTargetException e) {
                e.printStackTrace();;
            }
        });
        fakeBlockLocations.remove(location);
        tmpFallingBlock.get(p).remove();
        tmpFallingBlock.remove(p);
        playerMapping.get(p).setSolidBlock(false);
    }

    public static void removeHider(Player p) {
        if (!getHiders().contains(p)) return;
        if (playerMapping.get(p).isSolidBlock()) unlockPlayer(p);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        DisguiseManager.stopDisguise(p);
        playerMapping.get(p).setHider(false);
        board.getTeam("Hider").removePlayer(p);
        board.getTeam("Seeker").addPlayer(p);
    }

    public static PlayerType getPlayerType(Player player) {
        if (getHiders().contains(player))
            return PlayerType.HIDDER;
        return PlayerType.SEEKER;
    }

    public static void startGame(Map<Player, Material> hiderSet) {
        clearAll(); start(hiderSet);
        new BukkitRunnable() {
            @Override
            public void run() {
                update();
                if (isGameEnd()) {
                    end(); this.cancel();
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME), 0, 0);
    }
}
