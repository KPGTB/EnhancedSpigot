package dev.projectenhanced.enhancedspigot.data.cache.iface;

import com.j256.ormlite.dao.ForeignCollection;

import java.util.Collection;
import java.util.Map;

public interface IForeignMapping {
    Map<ForeignCollection<?>, Collection<?>> getForeignMapping();
}
