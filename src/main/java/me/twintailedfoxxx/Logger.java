package me.twintailedfoxxx;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final PrintStream stream;

    public Logger(PrintStream stream) {
        this.stream = stream;
    }

    public void log(LogLevel lv, String message) {
        String record = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss")) + " " +
                "[" + lv + "]: " + message;
        stream.println(record);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void severe(String message) {
        log(LogLevel.SEVERE, message);
    }

    public enum LogLevel {
        INFO,
        SEVERE,
        WARN
    }
}