package com.moneyflow.logging;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    private Path tmpDir;

    @BeforeEach
    void setup() throws IOException {
        LoggerFactory.shutdown();
        tmpDir = Files.createTempDirectory("logs-test-");
    }

    @AfterEach
    void teardown() {
        LoggerFactory.shutdown();
    }

    @Test
    void writesInfoToAppLog() throws Exception {
        LoggerFactory.configure(tmpDir, LogLevel.DEBUG, false);
        Logger logger = LoggerFactory.getLogger("TestLogger");
        logger.info("hello {}", "world");
        LoggerFactory.shutdown();

        List<String> lines = Files.readAllLines(tmpDir.resolve("app.log"));
        assertTrue(lines.stream().anyMatch(line -> line.contains("hello world")));
        assertEquals(0, lines.stream().filter(line -> line.contains("ERROR")).count());
    }

    @Test
    void writesErrorsOnlyToErrorLog() throws Exception {
        LoggerFactory.configure(tmpDir, LogLevel.DEBUG, false);
        Logger logger = LoggerFactory.getLogger("TestLogger");
        logger.info("ordinary line");
        logger.error("failure", new RuntimeException("boom"));
        LoggerFactory.shutdown();

        List<String> app = Files.readAllLines(tmpDir.resolve("app.log"));
        List<String> error = Files.readAllLines(tmpDir.resolve("error.log"));

        assertTrue(app.stream().anyMatch(line -> line.contains("ordinary line")));
        assertTrue(app.stream().anyMatch(line -> line.contains("failure")));
        assertTrue(error.stream().anyMatch(line -> line.contains("failure")));
        assertFalse(error.stream().anyMatch(line -> line.contains("ordinary line")));
        assertTrue(error.stream().anyMatch(line -> line.contains("RuntimeException"))
            || error.stream().anyMatch(line -> line.contains("boom")));
    }

    @Test
    void thresholdFiltersLowerLevels() throws Exception {
        LoggerFactory.configure(tmpDir, LogLevel.WARN, false);
        Logger logger = LoggerFactory.getLogger("TestLogger");
        logger.debug("debug msg");
        logger.info("info msg");
        logger.warn("warn msg");
        LoggerFactory.shutdown();

        List<String> lines = Files.readAllLines(tmpDir.resolve("app.log"));
        assertFalse(lines.stream().anyMatch(line -> line.contains("debug msg")));
        assertFalse(lines.stream().anyMatch(line -> line.contains("info msg")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("warn msg")));
    }

    @Test
    void concurrentWritesDoNotCorrupt() throws Exception {
        LoggerFactory.configure(tmpDir, LogLevel.INFO, false);
        Logger logger = LoggerFactory.getLogger("Concurrent");
        int threads = 8;
        int perThread = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int thread = 0; thread < threads; thread++) {
            final int tid = thread;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        logger.info("thread={} iter={}", tid, i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdown();
        LoggerFactory.shutdown();

        List<String> lines = Files.readAllLines(tmpDir.resolve("app.log"));
        long events = lines.stream().filter(line -> line.contains("thread=")).count();
        assertEquals((long) threads * perThread, events,
            "Expected " + (threads * perThread) + " events, got " + events);
    }
}
