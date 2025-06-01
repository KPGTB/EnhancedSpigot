package dev.projectenhanced.enhancedspigot.util.trycatch;

import java.util.function.Consumer;

public class TryUtil {
    public static <T> T tryOrDefault(ITry<T> iface, T or, Consumer<Exception> catchHandler) {
        try {
            return iface.run();
        } catch (Exception e) {
            if(catchHandler != null) catchHandler.accept(e);
            else e.printStackTrace();
            return or;
        }
    }

    public static <T> T tryOrDefault(ITry<T> iface, T or) {
        return tryOrDefault(iface,or,null);
    }

    public static <T> T tryAndReturn(ITry<T> iface) {
        return tryOrDefault(iface,null);
    }

    public static void tryRun(ITry<?> iface, Consumer<Exception> catchHandler) {
        tryOrDefault(iface, null,catchHandler);
    }

    public static void tryRun(ITry<?> iface) {
        tryAndReturn(iface);
    }
}
