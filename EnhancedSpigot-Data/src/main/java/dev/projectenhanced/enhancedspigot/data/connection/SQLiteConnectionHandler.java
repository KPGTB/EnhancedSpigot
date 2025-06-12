package dev.projectenhanced.enhancedspigot.data.connection;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SQLiteConnectionHandler implements IConnectionHandler{

    private final String fileName = "database.db";
    private final File file;

    public SQLiteConnectionHandler(File dataFolder) {
        this.file = new File(dataFolder, this.fileName);
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.SQLITE;
    }

    @Override
    public void retrieveCredentials(DatabaseOptions options) {return;}

    @Override
    public JdbcPooledConnectionSource connect() throws IOException, SQLException {
        this.file.getParentFile().mkdirs();
        if(!this.file.exists()) {
            this.file.createNewFile();
        }

        String connectionUrl = "jdbc:sqlite:" + this.file.getAbsolutePath();
        return new JdbcPooledConnectionSource(connectionUrl, new SqliteDatabaseType());
    }
}
