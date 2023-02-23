package plugin.planarg.hideandseek.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import plugin.planarg.hideandseek.managers.GameManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreBoardUtils {

    private static Map<UUID, Integer> tasks = new HashMap<>();
    private final UUID uuid;

    public ScoreBoardUtils(UUID uuid) {
        this.uuid = uuid;
    }

    public void setID(int id) {
        tasks.put(uuid, id);
    }

    public int getID() {
        return tasks.get(uuid);
    }

    public boolean hasID() {
        return tasks.containsKey(uuid);
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(getID());
        tasks.remove(uuid);
    }

    private static String getTime(int time) {
        int minute = time / 60;
        int second = time % 60;
        return second < 10 ? (minute + " : 0" + second) : (minute + " : " + second);
    }

    private static Team getTeam(String name) {
        Scoreboard scoreboard = GameManager.getBoard();
        if (scoreboard.getTeam(name) != null)
            return scoreboard.getTeam(name);
        return scoreboard.registerNewTeam(name);
    }

    private static final String HIDER_COUNTER = ChatColor.GREEN + "" + ChatColor.WHITE;
    private static final String SEEKER_COUNTER = ChatColor.RED + "" + ChatColor.WHITE;
    private static final String TIME_COUNTER = ChatColor.BLUE + "" + ChatColor.WHITE;

    public static void updateBoard(Objective obj, int hiders, int seekers, int time) {
        getTeam("hiderCounter").setPrefix(ChatColor.GREEN + "Hider: " + ChatColor.DARK_AQUA + hiders);
        obj.getScore(HIDER_COUNTER).setScore(2);
        getTeam("seekerCounter").setPrefix(ChatColor.RED + "Seeker: " + ChatColor.DARK_AQUA + seekers);
        obj.getScore(SEEKER_COUNTER).setScore(1);
        getTeam("timeCounter").setPrefix(ChatColor.AQUA + "Time: " + ChatColor.DARK_AQUA + getTime(time));
        obj.getScore(TIME_COUNTER).setScore(0);
    }

    public static void sendToPlayer(Player p) {
        p.setScoreboard(GameManager.getBoard());
    }

    public static void createBoard(int hiders, int seekers, int time) {
        Scoreboard board = GameManager.getBoard();
        Objective obj = board.getObjective("panel");
        if (obj == null)
            obj = board.registerNewObjective("panel", "dummy",
                ChatColor.translateAlternateColorCodes('&', "&a&l<< &2&lHide And Seek &a&l>>"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score score = obj.getScore(ChatColor.BLUE + "=-=-=-=-=-=-=-=-=-=");
        score.setScore(3);

        Team hiderCounter = getTeam("hiderCounter");
        hiderCounter.addEntry(HIDER_COUNTER);

        Team seekerCounter = getTeam("seekerCounter");
        seekerCounter.addEntry(SEEKER_COUNTER);

        Team timeCounter = getTeam("timeCounter");
        timeCounter.addEntry(TIME_COUNTER);

        updateBoard(obj, hiders, seekers, time);
    }
}
