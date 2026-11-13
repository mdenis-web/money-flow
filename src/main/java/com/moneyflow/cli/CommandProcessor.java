package com.moneyflow.cli;

import com.moneyflow.exception.AuthException;
import com.moneyflow.exception.NotFoundException;
import com.moneyflow.exception.ValidationException;
import com.moneyflow.logging.Logger;
import com.moneyflow.logging.LoggerFactory;
import com.moneyflow.model.User;
import com.moneyflow.service.*;
import com.moneyflow.storage.JsonFileStorage;
import com.moneyflow.util.Validator;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class CommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    private final AuthService auth;
    private final FinanceManager financeManager;
    private final ReportGenerator reportGenerator;
    private final AlertService alert;
    private final Console console;

    public CommandProcessor(AuthService auth, FinanceManager financeManager,
                            ReportGenerator reportGenerator, AlertService alert, Console console) {
        this.auth = auth;
        this.financeManager = financeManager;
        this.reportGenerator = reportGenerator;
        this.alert = alert;
        this.console = console;
    }

    public void run() {
        console.println("=== money-flow ===");
        console.println("Type 'help' to see available commands.");
        logger.info("CLI started, waiting for commands");

        while (true) {
            console.print("> ");
            String line = console.readLine();
            if (line == null) {
                console.println("EOF received — shutting down");
                logger.info("EOF received — shutting down");
                break;
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            logger.info("User command: '{}'", maskCommand(line));

            try {
                if (dispatch(line)) {
                    break;
                }
            } catch (ValidationException | AuthException | NotFoundException exception) {
                logger.warn("Command '{}' rejected: {}", firstWord(line), exception.getMessage());
                console.println("[ERROR] " + exception.getMessage());
            } catch (Exception exception) {
                logger.error("Unexpected error while processing command: '" + line + "'", exception);
                console.println("[ERROR] " + exception.getMessage());
            }
        }

        console.println("Goodbye!");
        logger.info("CLI stopped");
    }

    private boolean dispatch(String line) throws Exception {
        String[] textCollection = line.split("\\s+");
        String cmd = textCollection[0].toLowerCase(Locale.ROOT);

        switch (cmd) {
            case "exit", "quit" -> {
                return true;
            }
            case "help" -> printHelp();
            case "register" -> {
                expectArgs(textCollection, 3, "register <login> <password>");
                auth.register(textCollection[1], textCollection[2]);
                console.println("User '" + textCollection[1] + "' registered.");
            }
            case "login" -> {
                expectArgs(textCollection, 3, "login <login> <password>");
                financeManager.login(textCollection[1], textCollection[2]);
                console.println("Welcome, " + textCollection[1] + "!");
                for (String a : alert.check(financeManager.getWallet())) console.println(a);
            }
            case "logout" -> {
                ensureAuth();
                logger.info("User '{}' logging out", financeManager.getCurrentUser().getLogin());
                financeManager.logout();
                console.println("You have been logged out.");
            }
            case "add-income" -> {
                ensureAuth();
                expectArgs(textCollection, 3, "add-income <category> <amount>");
                financeManager.addIncome(textCollection[1], Validator.parseAmount(textCollection[2]));
                console.println("Income added.");
                printAlerts();
            }
            case "add-expense" -> {
                ensureAuth();
                expectArgs(textCollection, 3, "add-expense <category> <amount>");
                financeManager.addExpense(textCollection[1], Validator.parseAmount(textCollection[2]));
                console.println("Expense added.");
                printAlerts();
            }
            case "add-category" -> {
                ensureAuth();
                expectArgs(textCollection, 2, "add-category <name>");
                financeManager.addCategory(textCollection[1]);
                console.println("Category created.");
            }
            case "set-budget" -> {
                ensureAuth();
                expectArgs(textCollection, 3, "set-budget <category> <amount>");
                financeManager.setBudget(textCollection[1], Validator.parseAmount(textCollection[2]));
                console.println("Budget set.");
            }
            case "edit-budget" -> {
                ensureAuth();
                expectArgs(textCollection, 3, "edit-budget <category> <amount>");
                financeManager.editBudget(textCollection[1], Validator.parseAmount(textCollection[2]));
                console.println("Budget updated.");
            }
            case "delete-budget" -> {
                ensureAuth();
                expectArgs(textCollection, 2, "delete-budget <category>");
                financeManager.deleteBudget(textCollection[1]);
                console.println("Budget deleted.");
            }
            case "delete-category" -> {
                ensureAuth();
                expectArgs(textCollection, 2, "delete-category <category>");
                financeManager.deleteCategory(textCollection[1]);
                console.println("Category deleted.");
            }
            case "report" -> {
                ensureAuth();
                console.print(reportGenerator.fullReport(financeManager.getWallet()));
            }
            case "report-category" -> {
                ensureAuth();
                expectArgs(textCollection, 2, "report-category <category>");
                console.print(reportGenerator.categoryReport(financeManager.getWallet(), textCollection[1]));
            }
            case "report-categories" -> {
                ensureAuth();
                if (textCollection.length < 2) {
                    throw new ValidationException("Please specify categories");
                }
                console.print(reportGenerator.categoriesReport(financeManager.getWallet(),
                    Arrays.asList(textCollection).subList(1, textCollection.length)));
            }
            case "report-period" -> {
                ensureAuth();
                expectArgs(textCollection, 3, "report-period <yyyy-MM-dd> <yyyy-MM-dd>");
                console.print(reportGenerator.periodReport(financeManager.getWallet(),
                    LocalDate.parse(textCollection[1]), LocalDate.parse(textCollection[2])));
            }
            case "transfer" -> {
                ensureAuth();
                expectArgs(textCollection, 3, "transfer <toLogin> <amount>");
                financeManager.transfer(textCollection[1], Validator.parseAmount(textCollection[2]));
                console.println("Transfer completed.");
                printAlerts();
            }
            case "export" -> {
                ensureAuth();
                expectArgs(textCollection, 2, "export <path>");
                exportWallet(textCollection[1]);
            }
            case "import" -> {
                ensureAuth();
                expectArgs(textCollection, 2, "import <path>");
                importWallet(textCollection[1]);
            }
            default -> {
                logger.warn("Unknown command: '{}'", cmd);
                console.println("Unknown command. Type 'help' for the list of commands.");
            }
        }

        return false;
    }

    private void exportWallet(String path) throws Exception {
        Map<String, User> wrapper = new HashMap<>();
        wrapper.put(financeManager.getCurrentUser().getLogin(), financeManager.getCurrentUser());
        new JsonFileStorage(Path.of(path)).save(wrapper);
        logger.info("Wallet exported: user='{}', path={}",
            financeManager.getCurrentUser().getLogin(), path);
        console.println("Wallet exported to " + path);
    }

    private void importWallet(String path) throws Exception {
        Map<String, User> data = new JsonFileStorage(Path.of(path)).load();
        User imported = data.get(financeManager.getCurrentUser().getLogin());
        if (imported == null) {
            logger.warn("Import rejected: no data for '{}' in file {}",
                financeManager.getCurrentUser().getLogin(), path);
            throw new NotFoundException("No data for the current user in the file");
        }
        financeManager.getCurrentUser().setWallet(imported.getWallet());
        logger.info("Wallet imported: user='{}', path={}",
            financeManager.getCurrentUser().getLogin(), path);
        console.println("Wallet imported from " + path);
    }

    private void printAlerts() {
        for (String text : alert.check(financeManager.getWallet())) {
            console.println(text);
        }
    }

    private void ensureAuth() {
        if (!financeManager.isLoggedIn()) {
            logger.warn("Command requires authentication, but no user is logged in");
            throw new AuthException("Please log in first (login)");
        }
    }

    private void expectArgs(String[] textCollection, int size, String usage) {
        if (textCollection.length != size) {
            logger.warn("Invalid argument count: expected {}, got {}", size, textCollection.length);
            throw new ValidationException("Usage: " + usage);
        }
    }

    private static String maskCommand(String line) {
        String[] textCollection = line.split("\\s+");
        if (textCollection.length >= 3 && ("login".equalsIgnoreCase(textCollection[0])
            || "register".equalsIgnoreCase(textCollection[0]))) {
            return textCollection[0] + " " + textCollection[1] + " ***";
        }

        return line;
    }

    private static String firstWord(String line) {
        int index = line.indexOf(' ');
        return index < 0 ? line : line.substring(0, index);
    }

    private void printHelp() {
        logger.info("Help displayed");
        console.println("""
            --- Commands ---
            help                                        Show this help
            register <login> <password>                 Register a new user
            login <login> <password>                    Log in
            logout                                      Log out
            exit / quit                                 Exit the application

            add-income <category> <amount>              Add income
            add-expense <category> <amount>             Add expense
            add-category <name>                         Create category
            set-budget <category> <amount>              Set budget
            edit-budget <category> <amount>             Update budget
            delete-budget <category>                    Delete budget
            delete-category <category>                  Delete category

            report                                      Full report
            report-category <category>                  Report for one category
            report-categories <c1> <c2> ...             Report for several categories
            report-period <yyyy-MM-dd> <yyyy-MM-dd>     Report for a period

            transfer <toLogin> <amount>                 Transfer to another user
            export <path.json>                          Export wallet to JSON
            import <path.json>                          Import wallet from JSON
            """);
    }
}
