package com.moneyflow.logging;

public enum LogLevel {
    DEBUG(0), INFO(1), WARN(2), ERROR(3);

    private final int severity;

    LogLevel(int severity) {
        this.severity = severity;
    }

    public boolean isAtLeast(LogLevel other) {
        return this.severity >= other.severity;
    }
}
