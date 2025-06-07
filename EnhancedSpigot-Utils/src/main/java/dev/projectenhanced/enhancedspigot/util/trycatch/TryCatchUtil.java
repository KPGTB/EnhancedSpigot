package dev.projectenhanced.enhancedspigot.util.trycatch;

import java.util.function.Consumer;

public class TryCatchUtil {
    public static <T> T tryOrDefault(ITryCatchWithReturn<T> iface, T or, Consumer<Exception> catchHandler) {
        try {
            return iface.run();
        } catch (Exception e) {
            if(catchHandler != null) catchHandler.accept(e);
            else e.printStackTrace();
            return or;
        }
    }

    public static <T> T tryOrDefault(ITryCatchWithReturn<T> iface, T or) {
        return tryOrDefault(iface,or,null);
    }

    public static <T> T tryAndReturn(ITryCatchWithReturn<T> iface) {
        return tryOrDefault(iface,null);
    }

    public static void tryRun(ITryCatch iface, Consumer<Exception> catchHandler) {
        tryOrDefault(() -> {
            iface.run();
            return null;
        }, null,catchHandler);
    }

    public static void tryRun(ITryCatch iface) {
        tryRun(iface, null);
    }

    @FunctionalInterface
    public interface ITryCatchWithReturn<T> {
        T run() throws Exception;
    }

    @FunctionalInterface
    public interface ITryCatch {
        void run() throws Exception;
    }
}
