package plugin.planarg.hideandseek.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugin.planarg.hideandseek.HideAndSeek;
import plugin.planarg.hideandseek.managers.DisguiseManager;
import plugin.planarg.hideandseek.managers.GameManager;
import plugin.planarg.hideandseek.managers.PropsManager;
import plugin.planarg.hideandseek.managers.WorldSwitcher;
import plugin.planarg.hideandseek.settings.IPropsInfo;
import plugin.planarg.hideandseek.settings.ItemCollections;
import plugin.planarg.hideandseek.settings.ItemInteractions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

public class PlayerEventsHandler implements Listener {

    public static void resetPlayer(Player player) {
        player.setCollidable(false);
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Hello! " + player.getName());
        WorldSwitcher.switchToWorld(player, WorldSwitcher.INSTANCE.getWorld("Lobby"));
        player.getInventory().clear();
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
        player.getInventory().setItem(0, ItemCollections.menu);
        player.getInventory().setItem(1, ItemCollections.disguise);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.getFrom().getWorld().getName() != e.getTo().getWorld().getName()) {
            if (e.getTo().getWorld().getName().contains("Lobby")) {
                Bukkit.getPluginManager().callEvent(new PlayerEnterLobbyEvent(e.getPlayer()));
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItems(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!PropsManager.canPickupItem(p, e.getItem().getItemStack()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.getEntity().setFallDistance(0);
            e.setDamage(0);
            e.setCancelled(true);
        }
        if ((e.getCause() == EntityDamageEvent.DamageCause.DROWNING || e.getCause() == EntityDamageEvent.DamageCause.VOID) &&
                e.getFinalDamage() >= ((Player) e.getEntity()).getHealth()) {
            e.setDamage(0);
            e.setCancelled(true);
            GameManager.playerDied((Player) e.getEntity());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!GameManager.isGameEnd()) {
            player.kickPlayer("You can't log in when HideAndSeek is still running.");
        }
        if (player.getWorld().getName().contains("Lobby")) {
            Bukkit.getPluginManager().callEvent(new PlayerEnterLobbyEvent(player));
        }
        resetPlayer(player);
    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;
        if (item == null) return;
        if (item.equals(ItemCollections.menu)) {
            e.setCancelled(true);
            e.getPlayer().openInventory(ItemCollections.menu_content);
        }
        if (item.equals(ItemCollections.disguise)) {
            e.setCancelled(true);
            e.getPlayer().openInventory(ItemCollections.disguise_content);
        }
        if (item.equals(ItemCollections.seeker_sword)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().normalize().multiply(15)));
        }
        double result = PropsManager.useItem(e.getPlayer(), item, GameManager.timeSinceStart);
        if (result >= 0) e.setCancelled(true);
        if (result > 0) {
            int time = (int) Math.ceil(result);
            e.getPlayer().sendMessage(ChatColor.RED + "Cooling down : " + ChatColor.AQUA + time + ChatColor.RED + " seconds");
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        e.setAmount(e.getAmount() * 0.1);
    }

    @EventHandler
    public void onPlayerDamagedByEntity(EntityDamageByEntityEvent e) {
        if (GameManager.isGameEnd()) {
            e.setCancelled(true);
            return;
        }
        Entity from = e.getDamager();
        Entity to = e.getEntity();
        if (!(to instanceof Player)) return;
        if (from instanceof Player) {
            ArrayList<Player> hiderList = GameManager.getHiders();
            if (hiderList.contains(from) == hiderList.contains(to)) {
                e.setCancelled(true);
            } else {
                if (hiderList.contains(to)) GameManager.getPlayerData((Player) to).setUnlock();
                if (((Player) to).getHealth() - e.getFinalDamage() <= 0) {
                    e.setCancelled(true);
                    GameManager.playerDied((Player) to, (Player) from);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {
        System.out.println("Placing");
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerShoot(EntityShootBowEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) return;
        Arrow arrow = (Arrow) e.getProjectile();
        Bukkit.getLogger().info(((Player) entity).getDisplayName() + " shoot");
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().getOnlinePlayers().stream().filter((Player p) ->
                        (p.getLocation().subtract(arrow.getLocation().add(arrow.getVelocity()))).length() < 1.75).forEach((Player p) -> {
                    if (p.equals(entity)) return;
                    Vector velocity = p.getVelocity().multiply(2);
                    Vector apply = arrow.getVelocity().setY(0).normalize().multiply(2 * (0.47 +
                            new Random().nextDouble() / 70 + arrow.getKnockbackStrength() / 1.42)).setY(0.400023);
                    double result = arrow.getDamage() * arrow.getVelocity().length() * new Random().nextDouble(1, 1.5);
                    if (arrow.getShooter() instanceof Player) {
                        Player from = (Player) arrow.getShooter();
                        if (GameManager.getHiders().contains(from) != GameManager.getHiders().contains(p)) {
                            p.damage(result, from);
                            velocity.add(apply);
                            p.setVelocity(velocity);
                            from.playSound(from.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 3, 1);
                            arrow.remove();
                            this.cancel();
                        }
                    }
                });
                if (arrow.isDead()) this.cancel();
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin(HideAndSeek.PLUGIN_NAME), 0, 0);
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Arrow)) return;
        if (e.getHitEntity() == null) entity.remove();
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractWithMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (e.getWhoClicked() instanceof Player) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            if (player.getOpenInventory().getTitle().contains("map")) {
                int index = e.getSlot() + 1;
                ItemInteractions.voteFor(player, e.getSlot());
                player.closeInventory();
                player.sendMessage("You voted for map " + ChatColor.AQUA + index + ChatColor.WHITE + ".");
            }
            if (player.getOpenInventory().getTitle().contains("disguise")) {
                int index = e.getSlot() + 1;
                ItemInteractions.changeDisguiseBlock(player, e.getCurrentItem().getType());
                player.closeInventory();
                player.sendMessage("You chose disguise block " + ChatColor.AQUA + index + ChatColor.WHITE + ".");
            }
        }
    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e) {
        GameManager.removeHider(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent e) {
        GameManager.removeHider(e.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!DisguiseManager.isDisguising(player)) return;
        Entity entity = DisguiseManager.getFromPlayer(player);
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        Location location = player.getLocation();
        packet.getIntegers().write(0, entity.getEntityId());
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        Bukkit.getServer().getOnlinePlayers().forEach((Player p) -> {
            try {
                manager.sendServerPacket(p, packet, false);
            } catch (InvocationTargetException exception) {
                exception.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        event.setCancelled(true);
    }
}
