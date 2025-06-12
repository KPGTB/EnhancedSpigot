package dev.projectenhanced.enhancedspigot.data.connection;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.MysqlDatabaseType;
import com.j256.ormlite.jdbc.db.PostgresDatabaseType;

import java.io.IOException;
import java.sql.SQLException;

public class PostgreSQLConnectionHandler implements IConnectionHandler{

    private DatabaseOptions.Credentials credentials;

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.MYSQL;
    }

    @Override
    public void retrieveCredentials(DatabaseOptions options) {
        this.credentials = options.getCredentials();
    }

    @Override
    public JdbcPooledConnectionSource connect() throws IOException, SQLException {
        String url = "jdbc:postgresql://"+this.credentials.getHost()+":"+this.credentials.getPort()+"/"+this.credentials.getDatabase();
        return new JdbcPooledConnectionSource(url,this.credentials.getUsername(),this.credentials.getPassword(),new PostgresDatabaseType());
    }
}
