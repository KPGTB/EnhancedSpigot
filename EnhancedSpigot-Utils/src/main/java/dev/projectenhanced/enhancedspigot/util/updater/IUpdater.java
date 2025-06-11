package dev.projectenhanced.enhancedspigot.util.updater;

import dev.projectenhanced.enhancedspigot.util.SemanticVersion;

public interface IUpdater {
    /**
     * Check if there are some updates of this plugin
     * @param version Version of plugin as SemanticVersion
     * @return true if there are some updates
     */
    boolean hasUpdate(SemanticVersion version);

    /**
     * Check download link to update
     * @return Download link to update
     */
    String getDownloadLink();
}
