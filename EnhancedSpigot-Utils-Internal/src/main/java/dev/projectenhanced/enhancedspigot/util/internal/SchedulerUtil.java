package dev.projectenhanced.enhancedspigot.util.internal;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SchedulerUtil {
    private static final boolean isFolia = checkFolia();

    private static boolean checkFolia() {
        List<String> foliaForks = Arrays.asList("Folia", "Luminol");
        for (String fork : foliaForks) {
            if(Bukkit.getVersion().contains(fork) || Bukkit.getName().contains(fork)) return true;
        }
        return false;
    }

    public static Task runTask(Plugin plugin, Runnable runnable) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler()
                    .execute(plugin, runnable);
            return new Task(null);
        } else {
            return new Task(Bukkit.getScheduler().runTask(plugin, runnable));
        }
    }

    public static Task runTaskLater(Plugin plugin,Runnable runnable, long delayTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runDelayed(plugin, t -> runnable.run(), delayTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks));
    }

    public static Task runTaskTimer(Plugin plugin,Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runAtFixedRate(plugin, t -> runnable.run(), delayTicks < 1 ? 1 : delayTicks, periodTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks));
    }

    public static Task runTaskAsynchronously(Plugin plugin,Runnable runnable) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler()
                    .execute(plugin, runnable);
            return new Task(null);
        }
        return new Task(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public static Task runTaskLaterAsynchronously(Plugin plugin,Runnable runnable, long delayTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runDelayed(plugin, t -> runnable.run(), delayTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks));
    }

    public static Task runTaskTimerAsynchronously(Plugin plugin,Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler()
                    .runAtFixedRate(plugin, t -> runnable.run(), delayTicks < 1 ? 1 : delayTicks, periodTicks));

        else
            return new Task(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks));
    }

    public static void runTaskTimerAsynchronously(Plugin plugin, Consumer<Task> runnable, long delayTicks, long periodTicks) {
        if (isFolia)
            Bukkit.getGlobalRegionScheduler()
                    .runAtFixedRate(plugin, t -> runnable.accept(new Task(t)), delayTicks < 1 ? 1 : delayTicks, periodTicks);

        else
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, t -> runnable.accept(new Task(t)), delayTicks, periodTicks);
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static void cancelAll(Plugin plugin) {
        if(isFolia) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }

    public static class Task {

        private Object foliaTask;
        private BukkitTask bukkitTask;

        public Task(Object foliaTask) {
            this.foliaTask = foliaTask;
        }

        public Task(BukkitTask bukkitTask) {
            this.bukkitTask = bukkitTask;
        }

        public void cancel() {
            if(foliaTask == null && bukkitTask == null) {
                return;
            }
            if (foliaTask != null)
                ((ScheduledTask) foliaTask).cancel();
            else
                bukkitTask.cancel();
        }

        public int getTaskId() {
            if(foliaTask == null && bukkitTask == null) {
                return -1;
            }
            if (foliaTask != null)
                return -1;
            else
                return bukkitTask.getTaskId();
        }

    }
}
