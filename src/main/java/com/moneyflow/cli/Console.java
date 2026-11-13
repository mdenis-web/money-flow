package com.moneyflow.cli;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Console implements AutoCloseable {
    private final BufferedReader reader;
    private final PrintWriter writer;

    public Console(InputStream in, OutputStream out) {
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
    }

    public void print(String message) {
        writer.print(message);
        writer.flush();
    }

    public void println(String message) {
        writer.println(message);
    }

    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException exception) {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        reader.close();
        writer.close();
    }
}
