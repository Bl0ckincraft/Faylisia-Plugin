package fr.blockincraft.faylisia.task;

import fr.blockincraft.faylisia.Faylisia;
import fr.blockincraft.faylisia.displays.ActionBar;
import fr.blockincraft.faylisia.displays.Tab;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Task that will be activated each tick to actualize and display {@link ActionBar} for all players
 */
public class ActionBarTask extends BukkitRunnable {
    private static ActionBarTask instance;

    /**
     * Initialize instance and start task
     */
    public static void startTask() {
        if (instance == null) {
            instance = new ActionBarTask();
            instance.runTaskTimerAsynchronously(Faylisia.getInstance(), 20, 1);
        }
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ActionBar.display(player);
        }
    }
}
