package com.moneyflow.storage;

import com.moneyflow.model.*;
import org.junit.jupiter.api.Test;

import java.nio.file.*;
import java.util.*;

class StorageTest {
    @Test
    void saveAndLoadRoundTrip() throws Exception {
        Path tmp = Files.createTempFile("db", ".json");
        JsonFileStorage storage = new JsonFileStorage(tmp);

        Wallet wallet = new Wallet();
        wallet.addIncome(new Income("Salary", 1000));
        wallet.addExpense(new Expense("Food", 250));
        wallet.setBudget("Food", 500);

        User user = new User("alice", "hash", wallet);
        Map<String, User> map = new HashMap<>();
        map.put("alice", user);
        storage.save(map);

        Map<String, User> loaded = storage.load();
        assert loaded.containsKey("alice");
        Wallet loadedWallet = loaded.get("alice").getWallet();
        assert loadedWallet.totalIncome() == 1000;
        assert loadedWallet.getBudget("Food") == 500;
    }
}
