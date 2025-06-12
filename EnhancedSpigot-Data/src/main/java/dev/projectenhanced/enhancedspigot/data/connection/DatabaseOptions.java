package dev.projectenhanced.enhancedspigot.data.connection;

import dev.projectenhanced.enhancedspigot.config.annotation.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class DatabaseOptions {
    private ConnectionType type = ConnectionType.SQLITE;
    @Comment({
            "With these options, you can manage database cache (from OrmLite)",
            "The cache system saves the most recent database queries to improve performance. It's highly recommended to use!",
            "With the capacity option, you can manage the size of the cache per DAO. Greater - Better Server Performance - More RAM usage",
            "Capacity '0' changes the system to default. It means that there can be stored unlimited amount of objects, but they are removed after garbage collection.",
            "Test on your server which option is better for you. Disable only if you have a small amount of RAM!",
            "To disable, set to -1"
    })
    private int cacheCapacity = 0;
    @Comment({
            "Configure only when using MySQL or PostgreSQL"
    })
    private Credentials credentials = new Credentials();

    @Getter @Setter
    @NoArgsConstructor
    public static class Credentials {
        private String host = "localhost";
        private int port = 3306;
        private String username = "root";
        private String password = "";
        private String database = "minecraft";
    }
}
