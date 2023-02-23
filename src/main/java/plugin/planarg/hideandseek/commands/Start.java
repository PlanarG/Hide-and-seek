package plugin.planarg.hideandseek.commands;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.planarg.hideandseek.managers.DisguiseManager;
import plugin.planarg.hideandseek.managers.GameManager;

import java.util.HashMap;

public class Start implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HashMap<Player, Material> hiderSet = new HashMap<>();
        Bukkit.getServer().getOnlinePlayers().forEach((Player p) -> {
            if (p.getName().charAt(0) != 'P')
                hiderSet.put(p, Material.STONE);
        });
        GameManager.startGame(hiderSet);
        return true;
    }
}
