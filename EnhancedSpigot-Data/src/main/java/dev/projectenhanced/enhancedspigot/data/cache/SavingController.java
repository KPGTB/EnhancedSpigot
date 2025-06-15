package dev.projectenhanced.enhancedspigot.data.cache;

import com.google.common.collect.Sets;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ISavable;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class SavingController {
    private final Plugin plugin;
    private Set<SaveTask> saveTasks = Sets.newHashSet();

    public SavingController(Plugin plugin) {
        this.plugin = plugin;
    }

    public void addSavable(ISavable<?,?> savable, int interval) {
        this.saveTasks.add(new SaveTask(this.plugin, savable, interval));
    }

    public void clearController() {
        for (SaveTask saveTask : this.saveTasks) {
            saveTask.stop();
        }
        this.saveTasks.clear();
    }

    public class SaveTask {
        private final ISavable<?,?> savable;
        private final SchedulerUtil.Task bukkitTask;

        public SaveTask(Plugin plugin, ISavable<?,?> savable, int interval) {
            this.savable = savable;
            this.bukkitTask = SchedulerUtil.runTaskTimerAsynchronously(plugin, task -> this.savable.saveAll(),interval, interval);
        }

        public void stop() {
            this.bukkitTask.cancel();
        }
    }

}
