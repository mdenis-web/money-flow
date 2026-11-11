package com.moneyflow.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoggerFactory {
    private static final Map<String, Logger> REGISTRY = new ConcurrentHashMap<>();
    private static final Object INIT_LOCK = new Object();

    private static BufferedWriter appOut;
    private static BufferedWriter errOut;
    private static LogLevel threshold = LogLevel.INFO;
    private static boolean echoToConsole = true;
    private static Path logDir = Path.of("logs");
    private static boolean initialized = false;

    private LoggerFactory() {
    }

    public static void configure(Path directory, LogLevel minLevel, boolean echo) {
        synchronized (INIT_LOCK) {
            if (initialized) {
                threshold = minLevel;
                echoToConsole = echo;
                if (directory != null && !directory.equals(logDir)) {
                    System.err.println("[LoggerFactory] configure() called after init, "
                        + "directory change ignored: " + directory);
                }
                return;
            }
            if (directory != null) {
                logDir = directory;
            }
            threshold = minLevel;
            echoToConsole = echo;
        }
    }

    public static Logger getLogger(Class<?> type) {
        return getLogger(type.getName());
    }

    public static Logger getLogger(String name) {
        ensureInitialized();
        return REGISTRY.computeIfAbsent(name, text ->
            new Logger(text, appOut, errOut, threshold, echoToConsole));
    }

    private static void ensureInitialized() {
        if (initialized) {
            return;
        }
        synchronized (INIT_LOCK) {
            if (initialized) {
                return;
            }
            try {
                Files.createDirectories(logDir);
                Path appPath = logDir.resolve("app.log");
                Path errPath = logDir.resolve("error.log");
                appOut = openBuffered(appPath);
                errOut = openBuffered(errPath);
                initialized = true;
                Runtime.getRuntime().addShutdownHook(new Thread(LoggerFactory::shutdown,
                    "logger-shutdown"));
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to initialize logger", exception);
            }
        }
    }

    private static BufferedWriter openBuffered(Path path) throws IOException {
        return Files.newBufferedWriter(
            path,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND,
            StandardOpenOption.WRITE
        );
    }

    public static void shutdown() {
        synchronized (INIT_LOCK) {
            if (!initialized) {
                return;
            }
            REGISTRY.values().forEach(Logger::close);
            REGISTRY.clear();
            try {
                if (appOut != null) {
                    appOut.close();
                }
            } catch (IOException ignored) {
                //
            }
            try {
                if (errOut != null) {
                    errOut.close();
                }
            } catch (IOException ignored) {
                //
            }
            initialized = false;
        }
    }
}
