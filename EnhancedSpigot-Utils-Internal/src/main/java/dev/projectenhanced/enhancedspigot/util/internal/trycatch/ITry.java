package dev.projectenhanced.enhancedspigot.util.internal.trycatch;

@FunctionalInterface
public interface ITry<T> {
    T run() throws Exception;
}
