package com.moneyflow.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.moneyflow.logging.Logger;
import com.moneyflow.logging.LoggerFactory;
import com.moneyflow.model.User;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class JsonFileStorage implements Storage {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileStorage.class);

    private final Path file;
    private final Gson gson;

    public JsonFileStorage(Path file) {
        this.file = file;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
    }

    @Override
    public void save(Map<String, User> users) throws IOException {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                gson.toJson(users, writer);
            }
            logger.info("Data saved to file: {} (users: {})",
                file.toAbsolutePath(), users.size());
        } catch (IOException exception) {
            logger.error("Failed to save data to " + file.toAbsolutePath(), exception);
            throw exception;
        }
    }

    @Override
    public Map<String, User> load() throws IOException {
        if (!Files.exists(file)) {
            logger.info("Data file not found: {} — starting with empty set",
                file.toAbsolutePath());
            return new HashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Type token = new TypeToken<Map<String, User>>() {
            }.getType();
            Map<String, User> map = gson.fromJson(reader, token);
            Map<String, User> result = map != null ? map : new HashMap<>();
            logger.info("Data loaded from file: {} (users: {})",
                file.toAbsolutePath(), result.size());
            return result;
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to read data from " + file.toAbsolutePath(), exception);
            throw exception;
        }
    }

    public Gson getGson() {
        return gson;
    }
}
