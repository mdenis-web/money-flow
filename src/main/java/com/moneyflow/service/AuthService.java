package com.moneyflow.service;

import com.moneyflow.exception.AuthException;
import com.moneyflow.exception.ValidationException;
import com.moneyflow.logging.Logger;
import com.moneyflow.logging.LoggerFactory;
import com.moneyflow.model.User;
import com.moneyflow.model.Wallet;
import com.moneyflow.storage.Storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final Map<String, User> users = new HashMap<>();
    private final Storage storage;

    public AuthService(Storage storage) {
        this.storage = storage;
    }

    public void loadAll() throws IOException {
        Map<String, User> loaded = storage.load();
        users.putAll(loaded);
        logger.info("Loaded users: {}", loaded.size());
    }

    public void saveAll() throws IOException {
        storage.save(users);
        logger.info("Saved users: {}", users.size());
    }

    public User register(String login, String password) {
        logger.info("Registration attempt: user='{}'", login);
        if (login == null || login.isBlank()) {
            logger.warn("Registration rejected: empty login");
            throw new ValidationException("Login must not be empty");
        }
        if (password == null || password.length() < 3) {
            logger.warn("Registration rejected for '{}': password shorter than 3 characters", login);
            throw new ValidationException("Password must be at least 3 characters");
        }
        if (users.containsKey(login)) {
            logger.warn("Registration rejected: user '{}' already exists", login);
            throw new AuthException("User '" + login + "' already exists");
        }

        User user = new User(login, hash(password), new Wallet());
        users.put(login, user);
        logger.info("User '{}' registered successfully", login);
        return user;
    }

    public User login(String login, String password) {
        logger.info("Login attempt: user='{}'", login);
        User user = users.get(login);
        if (user == null || !user.getPasswordHash().equals(hash(password))) {
            logger.warn("Login failed for '{}': invalid credentials", login);
            throw new AuthException("Invalid login or password");
        }
        logger.info("User '{}' logged in successfully", login);
        return user;
    }

    public Optional<User> find(String login) {
        return Optional.ofNullable(users.get(login));
    }

    public Collection<User> findAll() {
        return users.values();
    }

    public Map<String, User> getUsers() {
        return users;
    }

    private static String hash(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            logger.error("SHA-256 algorithm is not available", exception);
            throw new IllegalStateException(exception);
        }
    }
}
