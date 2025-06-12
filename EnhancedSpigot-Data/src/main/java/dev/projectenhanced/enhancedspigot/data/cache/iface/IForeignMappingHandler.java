package dev.projectenhanced.enhancedspigot.data.cache.iface;

public interface IForeignMappingHandler {
    void dbToJava(IForeignMapping entity);
    void javaToDb(IForeignMapping entity);
}
