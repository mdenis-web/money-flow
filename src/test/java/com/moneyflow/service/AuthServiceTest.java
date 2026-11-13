package com.moneyflow.service;

import com.moneyflow.exception.AuthException;
import com.moneyflow.exception.ValidationException;
import com.moneyflow.storage.JsonFileStorage;
import org.junit.jupiter.api.*;

import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    private AuthService auth;

    @BeforeEach
    void setup() throws Exception {
        Path tmp = Files.createTempFile("users", ".json");
        Files.deleteIfExists(tmp);
        auth = new AuthService(new JsonFileStorage(tmp));
    }

    @Test
    void registerAndLogin() {
        auth.register("alice", "pass123");
        var user = auth.login("alice", "pass123");
        assertEquals("alice", user.getLogin());
    }

    @Test
    void duplicateRegisterFails() {
        auth.register("alice", "pass123");
        Assertions.assertThrows(AuthException.class, () -> auth.register("alice", "pass456"));
    }

    @Test
    void wrongPassword() {
        auth.register("bob", "pass");
        Assertions.assertThrows(AuthException.class, () -> auth.login("bob", "wrong"));
    }

    @Test
    void shortPasswordRejected() {
        Assertions.assertThrows(ValidationException.class, () -> auth.register("x", "1"));
    }
}
