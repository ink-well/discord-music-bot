package util;

import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Created by ink on 07.10.16.
 */
public class ConcUtil {
    public static CompletableFuture<Void> processCommand(Runnable runnable, Logger logger) {
        return CompletableFuture.runAsync(runnable)
                .exceptionally(t -> {
                    logger.warn("Could not complete command", t);
                    return null;
                });
    }
}
