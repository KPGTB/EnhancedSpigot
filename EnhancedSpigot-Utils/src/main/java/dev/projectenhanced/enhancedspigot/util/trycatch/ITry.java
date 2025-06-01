package dev.projectenhanced.enhancedspigot.util.trycatch;

@FunctionalInterface
public interface ITry<T> {
    T run() throws Exception;
}
