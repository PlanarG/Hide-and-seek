package plugin.planarg.hideandseek.settings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.planarg.hideandseek.managers.WorldSwitcher;

import java.util.ArrayList;
import java.util.List;

public class ItemCollections {
    public static ItemStack menu;
    public static ItemStack disguise;
    public static ItemStack seeker_sword;
    public static ItemStack seeker_helmet;
    public static ItemStack seeker_chestplate;
    public static ItemStack seeker_leggings;
    public static ItemStack seeker_boots;
    public static ItemStack seeker_bow;
    public static Inventory menu_content;
    public static Inventory disguise_content;
    public static List<ItemStack> maps;
    public static List<ItemStack> disguise_blocks;

    private static void addDisguiseBlock(Material material) {
        disguise_blocks.add(new ItemStack(material));
    }


    private static void setUnbreakable(ItemStack item) {
        ItemMeta tmp = item.getItemMeta();
        tmp.setUnbreakable(true);
        item.setItemMeta(tmp);
    }

    public static void init() {
        menu = new ItemStack(Material.BOOK);
        menu.addUnsafeEnchantment(Enchantment.MENDING, 1);
        ItemMeta tmp = menu.getItemMeta();
        tmp.setDisplayName(ChatColor.BOLD + "Vote for a map!");
        menu.setItemMeta(tmp);

        disguise = new ItemStack(Material.FEATHER);
        tmp = disguise.getItemMeta();
        tmp.setDisplayName(ChatColor.BOLD + "Choose your disguise");
        disguise.setItemMeta(tmp);

        seeker_sword = new ItemStack(Material.DIAMOND_SWORD);
        seeker_sword.addUnsafeEnchantment(Enchantment.PIERCING, 1);
        setUnbreakable(seeker_sword);

        seeker_bow = new ItemStack(Material.BOW);
        seeker_bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        setUnbreakable(seeker_bow);

        seeker_helmet = new ItemStack(Material.DIAMOND_HELMET);
        setUnbreakable(seeker_helmet);

        seeker_chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        setUnbreakable(seeker_chestplate);

        seeker_leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        setUnbreakable(seeker_leggings);

        seeker_boots = new ItemStack(Material.DIAMOND_BOOTS);
        setUnbreakable(seeker_boots);

        List<String> names = new ArrayList<>(WorldSwitcher.INSTANCE.getWorldNameList());
        names.add(ChatColor.BOLD + "Random(?)");
        maps = new ArrayList<>();
        for (String name : names) {
            ItemStack mapItem = new ItemStack(Material.SPONGE);
            ItemMeta meta = mapItem.getItemMeta();
            meta.setDisplayName(name + ChatColor.AQUA + " 0 Votes");
            mapItem.setItemMeta(meta);
            maps.add(mapItem);
        }

        menu_content = Bukkit.createInventory(null, 3 * 9, ChatColor.BOLD + "Vote for a map!");
        for (int i = 0; i < maps.size(); i++) {
            menu_content.setItem(i, maps.get(i));
        }

        disguise_content = Bukkit.createInventory(null, 3 * 9, ChatColor.BOLD + "Choose your disguise");

        disguise_blocks = new ArrayList<>();
        addDisguiseBlock(Material.GRASS);
        addDisguiseBlock(Material.GRASS_BLOCK);
        addDisguiseBlock(Material.DIRT);
        addDisguiseBlock(Material.OAK_LEAVES);
        addDisguiseBlock(Material.OAK_WOOD);
        addDisguiseBlock(Material.DARK_OAK_LEAVES);
        addDisguiseBlock(Material.DARK_OAK_WOOD);
        addDisguiseBlock(Material.STONE_BRICK_STAIRS);
        addDisguiseBlock(Material.STONE_BRICKS);
        addDisguiseBlock(Material.FLOWER_POT);
        addDisguiseBlock(Material.STONE);
        addDisguiseBlock(Material.TORCH);
        addDisguiseBlock(Material.COMMAND_BLOCK);

        for (int i = 0; i < disguise_blocks.size(); i++) {
            disguise_content.setItem(i, disguise_blocks.get(i));
        }

    }


}
