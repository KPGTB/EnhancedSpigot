package dev.projectenhanced.enhancedspigot.data.connection;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.MysqlDatabaseType;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.sql.SQLException;

public class MySQLConnectionHandler implements IConnectionHandler{

    private String host;
    private short port;
    private String username;
    private String password;
    private String database;

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.MYSQL;
    }

    @Override
    public void retrieveCredentials(ConfigurationSection section) {
        this.host = section.getString("host");
        this.port = (short) section.getInt("port");
        this.username = section.getString("username");
        this.password = section.getString("password");
        this.database = section.getString("database");
    }

    @Override
    public JdbcPooledConnectionSource connect() throws IOException, SQLException {
        String url = "jdbc:mysql://"+this.host+":"+this.port+"/"+this.database;
        return new JdbcPooledConnectionSource(url,username,password,new MysqlDatabaseType());
    }
}
