package com.moneyflow.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class Logger implements AutoCloseable {
    private static final DateTimeFormatter TS = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .withZone(ZoneOffset.UTC);

    private final String name;
    private final BufferedWriter appOut;
    private final BufferedWriter errOut;
    private final LogLevel threshold;
    private final boolean echoToConsole;

    private final ReentrantLock appLock = new ReentrantLock(true);
    private final ReentrantLock errLock = new ReentrantLock(true);

    private volatile boolean closed = false;

    Logger(String name,
           BufferedWriter appOut,
           BufferedWriter errOut,
           LogLevel threshold,
           boolean echoToConsole) {
        this.name = Objects.requireNonNull(name);
        this.appOut = Objects.requireNonNull(appOut);
        this.errOut = Objects.requireNonNull(errOut);
        this.threshold = Objects.requireNonNull(threshold);
        this.echoToConsole = echoToConsole;
    }

    public String getName() {
        return name;
    }

    public void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, null, args);
    }

    public void info(String message, Object... args) {
        log(LogLevel.INFO, message, null, args);
    }

    public void warn(String message, Object... args) {
        log(LogLevel.WARN, message, null, args);
    }

    public void error(String message, Object... args) {
        log(LogLevel.ERROR, message, null, args);
    }

    public void error(String message, Throwable t, Object... args) {
        log(LogLevel.ERROR, message, t, args);
    }

    private void log(LogLevel level, String message, Throwable throwable, Object... args) {
        if (closed || !level.isAtLeast(threshold)) return;

        String formatted = format(message, args);
        String line = buildLine(level, formatted);

        writeSafely(appOut, appLock, line, throwable);

        if (level == LogLevel.ERROR) {
            writeSafely(errOut, errLock, line, throwable);
        }

        if (echoToConsole) {
            System.out.print(line);
            if (throwable != null) {
                throwable.printStackTrace(System.out);
            }
        }
    }

    private void writeSafely(BufferedWriter out, ReentrantLock lock,
                             String line, Throwable throwable) {
        lock.lock();
        try {
            out.write(line);
            if (throwable != null) {
                out.write(stackTraceToString(throwable));
            }
            out.flush();
        } catch (IOException exception) {
            System.err.println("[LOGGER] Failed to write log record: " + exception.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private String buildLine(LogLevel level, String message) {
        return TS.format(Instant.now())
            + " [" + Thread.currentThread().getName() + "]"
            + " " + padLevel(level)
            + " " + name
            + " - " + message
            + System.lineSeparator();
    }

    private static String padLevel(LogLevel level) {
        return switch (level) {
            case DEBUG -> "DEBUG";
            case INFO -> "INFO ";
            case WARN -> "WARN ";
            case ERROR -> "ERROR";
        };
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) return message;
        StringBuilder builder = new StringBuilder(message.length() + args.length * 8);
        int argIdx = 0;
        int index = 0;
        while (index < message.length()) {
            if (index + 1 < message.length() && message.charAt(index) == '{' && message.charAt(index + 1) == '}'
                && argIdx < args.length) {
                builder.append(String.valueOf(args[argIdx++]));
                index += 2;
            } else {
                builder.append(message.charAt(index++));
            }
        }
        return builder.toString();
    }

    private static String stackTraceToString(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        appLock.lock();
        try {
            appOut.close();
        } catch (IOException ignored) {
            //
        } finally {
            appLock.unlock();
        }

        errLock.lock();
        try {
            errOut.close();
        } catch (IOException ignored) {
            //
        } finally {
            errLock.unlock();
        }
    }
}
