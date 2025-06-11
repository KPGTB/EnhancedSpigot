package dev.projectenhanced.enhancedspigot.util.updater;

import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import dev.projectenhanced.enhancedspigot.util.rest.client.EnhancedRequest;
import dev.projectenhanced.enhancedspigot.util.rest.client.EnhancedResponse;

public class SpigotUpdater implements IUpdater{
    private final String resourceID;

    public SpigotUpdater(String resourceID) {
        this.resourceID = resourceID;
    }

    @Override
    public boolean hasUpdate(SemanticVersion version) {
        try {
            EnhancedResponse response = EnhancedRequest.builder()
                    .url("https://api.spigotmc.org/legacy/update.php?resource="+resourceID)
                    .build()
                    .send();
            SemanticVersion latest = new SemanticVersion(response.getAsString());
            return latest.isNewerThan(version);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getDownloadLink() {
        return "https://www.spigotmc.org/resources/"+resourceID;
    }
}
