package com.moneyflow;

import com.moneyflow.cli.CommandProcessor;
import com.moneyflow.cli.Console;
import com.moneyflow.logging.LogLevel;
import com.moneyflow.logging.Logger;
import com.moneyflow.logging.LoggerFactory;
import com.moneyflow.service.*;
import com.moneyflow.storage.JsonFileStorage;
import com.moneyflow.storage.Storage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        LoggerFactory.configure(Path.of("logs"), LogLevel.INFO, false);
        Logger logger = LoggerFactory.getLogger(Main.class);

        Path dbFile = Paths.get("data", "users.json");
        Storage storage = new JsonFileStorage(dbFile);
        AuthService auth = new AuthService(storage);

        logger.info("====================================================");
        logger.info("Starting money-flow");
        logger.info("Data file: {}", dbFile.toAbsolutePath());

        try {
            auth.loadAll();
            logger.info("Data loaded successfully. Users: {}", auth.getUsers().size());
        } catch (Exception exception) {
            logger.error("Failed to load data on startup", exception);
        }

        FinanceManager financeManager = new FinanceManager(auth);
        ReportGenerator reportGenerator = new ReportGenerator();
        AlertService alert = new AlertService();

        int exitCode = 0;
        try (Console console = new Console(System.in, System.out)) {
            CommandProcessor processor = new CommandProcessor(auth, financeManager,
                reportGenerator, alert, console);
            processor.run();
        } catch (Exception exception) {
            logger.error("Fatal application error", exception);
            exitCode = 1;
        } finally {
            try {
                auth.saveAll();
                logger.info("Data saved to {}", dbFile.toAbsolutePath());
            } catch (Exception exception) {
                logger.error("Failed to save data on shutdown", exception);
                exitCode = 1;
            }
            logger.info("Application stopped (exit={})", exitCode);
            logger.info("====================================================");
            LoggerFactory.shutdown();
        }
        System.exit(exitCode);
    }
}
