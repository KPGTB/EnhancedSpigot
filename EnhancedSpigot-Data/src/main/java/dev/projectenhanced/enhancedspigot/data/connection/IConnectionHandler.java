package dev.projectenhanced.enhancedspigot.data.connection;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.sql.SQLException;

public interface IConnectionHandler {
    ConnectionType getConnectionType();
    void retrieveCredentials(ConfigurationSection credentialsSection);
    JdbcPooledConnectionSource connect() throws IOException, SQLException;
}
