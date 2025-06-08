package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.annotation.Comment;
import dev.projectenhanced.enhancedspigot.config.annotation.Ignore;
import dev.projectenhanced.enhancedspigot.config.annotation.Serializer;
import dev.projectenhanced.enhancedspigot.config.serializer.ConfigSerializerManager;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import dev.projectenhanced.enhancedspigot.config.util.SectionUtil;
import dev.projectenhanced.enhancedspigot.util.trycatch.TryCatchUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigSerializer implements ISerializer<Object> {
    @Override
    public Object serialize(Object object, JavaPlugin plugin) {
        ConfigurationSection section = SectionUtil.createEmpty();
        this.serializeTo(object,plugin,section);
        return section;
    }

    public void serializeTo(Object object, JavaPlugin plugin, ConfigurationSection section) {
        for (Field field : object.getClass().getDeclaredFields()) {
            Ignore ignoreAnn = field.getDeclaredAnnotation(Ignore.class);
            if(ignoreAnn != null) continue;

            Comment commentAnn = field.getDeclaredAnnotation(Comment.class);
            Serializer serializerAnn = field.getDeclaredAnnotation(Serializer.class);

            String key = camelToKebabCase(field.getName());

            field.setAccessible(true);
            Object value = TryCatchUtil.tryAndReturn(() -> field.get(object));
            field.setAccessible(false);

            section.set(key, handleObjectSerialization(value,serializerAnn,plugin));
            if(commentAnn != null) SectionUtil.addComments(section,key, Arrays.stream(commentAnn.value()).collect(Collectors.toList()));
        }
    }

    private Object handleObjectSerialization(Object value, Serializer serializerAnn, JavaPlugin plugin) {
        if(value instanceof List) return handleListSerialization((List<?>) value,serializerAnn,plugin);
        if(value instanceof Map) return handleMapSerialization((Map<?, ?>) value, serializerAnn, plugin);

        ISerializer<?> serializer = ConfigSerializerManager.getInstance().getSerializer(value.getClass());
        if(serializerAnn != null) serializer = TryCatchUtil.tryAndReturn(() -> serializerAnn.value().getDeclaredConstructor().newInstance());

        Class<?> enclosing = value.getClass().getEnclosingClass();
        while(enclosing != null) {
            if(EnhancedConfig.class.isAssignableFrom(enclosing)) {
                serializer = this;
                break;
            }
            enclosing = enclosing.getEnclosingClass();
        }

        return serializer == null ? value : useSerializer(serializer,value,plugin);
    }

    private List<Object> handleListSerialization(List<?> value, Serializer serializerAnn, JavaPlugin plugin) {
        List<Object> result = new ArrayList<>();
        value.forEach(element -> {
            result.add(handleObjectSerialization(element,serializerAnn,plugin));
        });
        return result;
    }

    public Map<Object,Object> handleMapSerialization(Map<?,?> value, Serializer serializerAnn, JavaPlugin plugin) {
        Map<Object,Object> result = new HashMap<>();

        value.forEach((k,v) -> {
            result.put(k, handleObjectSerialization(v,serializerAnn,plugin));
        });

        return result;
    }

    @Override
    public Object deserialize(Object configValue, JavaPlugin plugin) {
        throw new UnsupportedOperationException("Use deserializeToObject method instead");
    }

    public void deserializeToObject(ConfigurationSection section, JavaPlugin plugin, Object object, EnhancedConfig source) {
        for (Field field : object.getClass().getDeclaredFields()) {
            Ignore ignoreAnn = field.getDeclaredAnnotation(Ignore.class);
            if(ignoreAnn != null) continue;

            Serializer serializerAnn = field.getDeclaredAnnotation(Serializer.class);

            String key = camelToKebabCase(field.getName());

            field.setAccessible(true);
            Type generic = field.getGenericType();
            Type[] types;
            if(generic instanceof ParameterizedType) types = ((ParameterizedType) generic).getActualTypeArguments();
            else types = null;

            TryCatchUtil.tryRun(() -> field.set(
                    object,
                    handleObjectDeserialization(section.get(key), field.getType(),types, serializerAnn, plugin, source)
            ));
            field.setAccessible(false);
        }
    }

    private Object handleObjectDeserialization(Object value, Class<?> clazz, Type[] types, Serializer serializerAnn, JavaPlugin plugin, EnhancedConfig source) {
        if(List.class.isAssignableFrom(clazz)) return handleListDeserialization((List<?>) value,types[0],serializerAnn,plugin,source);
        if(Map.class.isAssignableFrom(clazz)) return handleMapDeserialization(
                value instanceof ConfigurationSection ? ((ConfigurationSection) value).getValues(false) : (Map<?,?>) value
                , types[1],serializerAnn, plugin,source);

        ISerializer<?> serializer = ConfigSerializerManager.getInstance().getSerializer(clazz);
        if(serializerAnn != null) serializer = TryCatchUtil.tryAndReturn(() -> serializerAnn.value().getDeclaredConstructor().newInstance());

        boolean isEnclosed = false;
        Class<?> enclosing = clazz.getEnclosingClass();
        while(enclosing != null) {
            if(EnhancedConfig.class.isAssignableFrom(enclosing)) {
                isEnclosed = true;
                break;
            }
            enclosing = enclosing.getEnclosingClass();
        }

        if(isEnclosed) {
            Object result = getEnclosedClassInstance(clazz,source);

            this.deserializeToObject(
                    value instanceof ConfigurationSection ? (ConfigurationSection) value : SectionUtil.create((Map<?, ?>) value),
                    plugin,
                    result,
                    source
            );
            return result;
        }

        return serializer == null ? value : useDeserializer(serializer,value,plugin);
    }

    private Object getEnclosedClassInstance(Class<?> clazz, EnhancedConfig source) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {}

        Class<?> enclosing = clazz.getEnclosingClass();
        Object invoker = null;

        if(enclosing != null) {
            if(EnhancedConfig.class.isAssignableFrom(enclosing)) invoker = source;
            else invoker = getEnclosedClassInstance(enclosing, source);
        }

        Object finalInvoker = invoker;
        return TryCatchUtil.tryAndReturn(() -> finalInvoker != null ?
                clazz.getDeclaredConstructor(enclosing).newInstance(finalInvoker)
                : clazz.getDeclaredConstructor().newInstance());
    }

    private List<Object> handleListDeserialization(List<?> value, Type valueType, Serializer serializerAnn, JavaPlugin plugin, EnhancedConfig source) {
        Class<?> valueClazz;
        Type[] types;
        if(valueType instanceof Class) {
            types = null;
            valueClazz = (Class<?>) valueType;
        } else if(valueType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) valueType;
            valueClazz = (Class<?>) pt.getRawType();
            types = pt.getActualTypeArguments();
        } else {
            valueClazz = null;
            types = null;
        }


        List<Object> result = new ArrayList<>();
        value.forEach(element -> {
            result.add(handleObjectDeserialization(element, valueClazz, types,serializerAnn,plugin,source));
        });
        return result;
    }

    public Map<Object,Object> handleMapDeserialization(Map<?,?> value, Type valueType, Serializer serializerAnn, JavaPlugin plugin, EnhancedConfig source) {
        Class<?> valueClazz;
        Type[] types;
        if(valueType instanceof Class) {
            types = null;
            valueClazz = (Class<?>) valueType;
        } else if(valueType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) valueType;
            valueClazz = (Class<?>) pt.getRawType();
            types = pt.getActualTypeArguments();
        } else {
            valueClazz = null;
            types = null;
        }

        Map<Object,Object> result = new HashMap<>();

        value.forEach((k,v) -> {
            result.put(k, handleObjectDeserialization(v,valueClazz,types,serializerAnn,plugin,source));
        });

        return result;
    }

    @Override
    public boolean convertToSection() {
        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> Object useSerializer(ISerializer<T> serializer, Object obj, JavaPlugin plugin) {
        return serializer.serialize((T) obj,plugin);
    }

    @SuppressWarnings("unchecked")
    private <T> Object useDeserializer(ISerializer<T> serializer, Object obj, JavaPlugin plugin) {
        if(serializer.convertToSection() && !(obj instanceof MemorySection)) {
            obj = SectionUtil.create((Map<?, ?>) obj);
        }
        return serializer.deserialize((T) obj,plugin);
    }

    private String camelToKebabCase(String str) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";
        str = str.replaceAll(regex, replacement).toLowerCase();
        return str;
    }
}
