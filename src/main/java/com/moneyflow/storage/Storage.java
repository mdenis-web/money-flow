package com.moneyflow.storage;

import com.moneyflow.model.User;
import java.io.IOException;
import java.util.Map;

public interface Storage {
    Map<String, User> load() throws IOException;

    void save(Map<String, User> users) throws IOException;
}
