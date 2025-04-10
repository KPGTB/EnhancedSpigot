package dev.projectenhanced.enhancedspigot.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.LruObjectCache;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import dev.projectenhanced.enhancedspigot.data.connection.ConnectionType;
import dev.projectenhanced.enhancedspigot.data.connection.IConnectionHandler;
import dev.projectenhanced.enhancedspigot.data.connection.MySQLConnectionHandler;
import dev.projectenhanced.enhancedspigot.data.connection.SQLiteConnectionHandler;
import dev.projectenhanced.enhancedspigot.util.internal.ReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter @Setter
public class DatabaseController {
    private final JavaPlugin plugin;
    private final File jarFile;
    private final ConnectionType connectionType;
    private final ConfigurationSection options;

    private boolean debug;
    private JdbcPooledConnectionSource source;
    private Map<Class<?>, Dao<?,?>> daoMap;

    public DatabaseController(JavaPlugin plugin, File jarFile, ConnectionType connectionType, ConfigurationSection options) {
        this.plugin = plugin;
        this.jarFile = jarFile;
        this.connectionType = connectionType;
        this.options = options;

        this.debug = false;
        this.daoMap = new HashMap<>();
    }

    public void connect() {
        IConnectionHandler handler = null;
        switch (this.connectionType) {
            case MYSQL:
                handler = new MySQLConnectionHandler();
                break;
            case SQLITE:
                handler = new SQLiteConnectionHandler(this.plugin.getDataFolder());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported connection type: " + this.connectionType.name());
        }
        handler.retrieveCredentials(this.options);
        try {
            this.source = handler.connect();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }

        if(!this.debug) LoggerFactory.setLogBackendFactory(LogBackendType.NULL);
    }

    @SneakyThrows
    public void close() {
        if(this.source == null) return;
        this.source.close();
    }

    public void registerEntities(String packageName) {
        if(this.source == null) return;

        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(this.jarFile, packageName)) {
            if(clazz.getDeclaredAnnotation(DatabaseTable.class) == null) continue;

            Field idField = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> {
                        DatabaseField ann = f.getDeclaredAnnotation(DatabaseField.class);
                        if(ann == null) return false;
                        return ann.id() || ann.generatedId() || !ann.generatedIdSequence().isEmpty();
                    })
                    .findAny().orElse(null);

            if(idField == null) continue;

            try {
                TableUtils.createTableIfNotExists(this.source, clazz);
            } catch (SQLException e) {
                e.printStackTrace();
                continue;
            }

            Dao<?,?> dao;
            try {
                dao = DaoManager.createDao(this.source, clazz);
                int cache = this.options.getInt("cache",0);
                switch (cache) {
                    case -1:
                        break;
                    case 0:
                        dao.setObjectCache(true);
                        break;
                    default:
                        dao.setObjectCache(new LruObjectCache(cache));
                        break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                continue;
            }
            this.daoMap.put(clazz, dao);
        }
    }

    @SuppressWarnings("unchecked")
    public <T,Z> Dao<T,Z> getDao(Class<T> daoClass, Class<Z> idClass) {
        if(this.source == null) return null;

        Dao<?,?> dao = this.daoMap.get(daoClass);
        return dao == null ? null : (Dao<T,Z>) dao;
    }

    /**
     * Register all OrmLite persisters from specified package
     * @param packageName Package where are stored all persisters
     */
    public void registerPersisters(String packageName) {
        if(this.source == null) return;
        ReflectionUtil.getAllClassesInPackage(this.jarFile, packageName)
                .stream()
                .filter(DataPersister.class::isAssignableFrom)
                .map(clazz -> {
                    try {
                        return (DataPersister) clazz.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(DataPersisterManager::registerDataPersisters);
    }
}
