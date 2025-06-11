package dev.projectenhanced.enhancedspigot.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class PDCUtil {

    /**
     * With this method you can save data to cache
     * @param target Object where you want to save data
     * @param plugin Plugin instance
     * @param key Key of data
     * @param data Object with data
     */
    @SuppressWarnings("unchecked")
    public <T> void setData(Object target, JavaPlugin plugin, String key, T data) {
        Class<T> clazz = (Class<T>) data.getClass();
        PersistentDataType<T,T> pdcType = getPdcType(clazz);

        if(pdcType == null) throw new IllegalArgumentException("You try to save unsupported type!");

        ItemStack is = null;
        if(target instanceof ItemStack) {
            is = (ItemStack) target;
            target = is.getItemMeta();
        }

        if(!(target instanceof PersistentDataHolder)) return;
        ((PersistentDataHolder) target).getPersistentDataContainer()
                .set(new NamespacedKey(plugin,key), pdcType, data);
        if(is != null) is.setItemMeta((ItemMeta) target);
    }

    /**
     * With this method you can remove data from cache
     * @param target Object where you want to save data (or null if cacheSource is SERVER)
     * @param plugin Plugin instance
     * @param key Key of data
     */
    public void removeData( Object target, JavaPlugin plugin, String key) {
        ItemStack is = null;
        if(target instanceof ItemStack) {
            is = (ItemStack) target;
            target = is.getItemMeta();
        }

        if(!(target instanceof PersistentDataHolder)) return;
        ((PersistentDataHolder) target).getPersistentDataContainer()
                .remove(new NamespacedKey(plugin,key));
        if(is != null) is.setItemMeta((ItemMeta) target);
    }

    /**
     * This method returns data from cache
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param plugin Plugin instance
     * @param key Key of data
     * @param expected Class that is expected in return
     * @return Object with data or null if there isn't any data
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(Object target, JavaPlugin plugin, String key, Class<T> expected) {
        PersistentDataType<T,T> pdcType = getPdcType(expected);

        if(pdcType == null) throw new IllegalArgumentException("You try to save unsupported type!");

        if(target instanceof ItemStack) target = ((ItemStack) target).getItemMeta();
        if(!(target instanceof PersistentDataHolder)) return null;

        return ((PersistentDataHolder) target).getPersistentDataContainer()
                .get(new NamespacedKey(plugin,key),pdcType);
    }

    /**
     * This method returns data from cache or defined data
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param plugin Plugin instance
     * @param key Key of data
     * @param or Data that should be returned when data is null
     * @return Object with data
     */
    @SuppressWarnings("unchecked")
    public <T> T getDataOr(Object target, JavaPlugin plugin, String key, T or) {
        Class<T> expected = (Class<T>) or.getClass();
        if(!hasData(target, plugin,key,expected)) {
            return or;
        }

        T result = getData(target,plugin,key,expected);
        return result == null ? or : result;
    }

    /**
     * This method checks if cache contains data
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param plugin Plugin instance
     * @param key Key of data
     * @param expected Class that should be checked
     * @return true if exists
     */
    public <T> boolean hasData(Object target, JavaPlugin plugin, String key, Class<T> expected) {
        try {
            T data = getData(target,plugin,key,expected);
            return data != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method returns PersistentDataType from class
     * @param clazz Class that is expected
     * @return PersistentDataType of this class or null if there isn't any PDT with this class
     */
    @SuppressWarnings("unchecked")
    private <Z> PersistentDataType<Z,Z> getPdcType(Class<Z> clazz) {
        Map<Class<?>, PersistentDataType<?,?>> acceptedTypes = new HashMap<>();
        acceptedTypes.put(Byte.class, PersistentDataType.BYTE);
        acceptedTypes.put(Short.class, PersistentDataType.SHORT);
        acceptedTypes.put(Integer.class, PersistentDataType.INTEGER);
        acceptedTypes.put(Long.class, PersistentDataType.LONG);
        acceptedTypes.put(Float.class, PersistentDataType.FLOAT);
        acceptedTypes.put(Double.class, PersistentDataType.DOUBLE);
        acceptedTypes.put(String.class, PersistentDataType.STRING);
        acceptedTypes.put(byte[].class, PersistentDataType.BYTE_ARRAY);
        acceptedTypes.put(int[].class, PersistentDataType.INTEGER_ARRAY);
        acceptedTypes.put(long[].class, PersistentDataType.LONG_ARRAY);
        return (PersistentDataType<Z, Z>) acceptedTypes.get(clazz);
    }

}
