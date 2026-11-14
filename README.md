# Project Money-Flow

## Описание
Приложение для управления личными финансами на JAVA.
Данные хранятся в памяти и сохраняются в JSON. Интерфейс — интерактивный CLI.
Логирование — кастомный логгер с записью в `app.log` и `error.log`.

## Запуск

```bash
mvn clean package
java -jar target/money-flow-1.0.0.jar
```

Или без сборки:

```bash
mvn exec:java -Dexec.mainClass=com.moneyflow.Main
```

## Тесты

```bash
mvn test
```

## Логирование

### Описание
Логгер без внешних зависимостей.
Пишет в каталог `logs/`:

| Файл | Уровень | Содержимое |
|---|---|---|
| `logs/app.log`   | INFO+ | все действия: логин, операции, отчёты, сохранения |
| `logs/error.log` | ERROR | только ошибки (с полным стектрейсом) |

### Настройка

Через `LoggerFactory.configure(...)` в `Main`:

```java
LoggerFactory.configure(
    Path.of("logs"),        // каталог логов
    LogLevel.INFO,          // минимальный уровень
    true                    // эхо в консоль
);
```

## CLI Команды

| Команда | Описание |
|---|---|
| `register <login> <password>` | регистрация |
| `login <login> <password>` | вход |
| `logout` | выход из аккаунта |
| `add-income <category> <amount>` | добавить доход |
| `add-expense <category> <amount>` | добавить расход |
| `add-category <name>` | создать категорию |
| `set-budget <category> <amount>` | установить бюджет |
| `edit-budget <category> <amount>` | изменить бюджет |
| `delete-budget <category>` | удалить бюджет |
| `delete-category <category>` | удалить категорию |
| `report` | полный отчёт |
| `report-category <category>` | отчёт по категории |
| `report-categories <c1> <c2>...` | отчёт по нескольким категориям |
| `report-period <yyyy-MM-dd> <yyyy-MM-dd>` | отчёт за период |
| `transfer <toLogin> <amount>` | перевод другому пользователю |
| `export <path.json>` | экспорт кошелька |
| `import <path.json>` | импорт кошелька |
| `help` | справка |
| `exit` / `quit` | выход |

## Пример сессии

```
> register alice pass123
> login alice pass123
> add-income Salary 20000
> add-income Salary 40000
> add-expense Food 300
> add-expense Food 500
> add-expense Entertainment 3000
> set-budget Food 4000
> set-budget Entertainment 3000
> report
```

## Архитектура

- **model** — `User`, `Wallet`, `Transaction` (`Income`/`Expense`), `Category`.
- **service** — `AuthService`, `FinanceManager`, `ReportGenerator`, `AlertService`.
- **storage** — `Storage` (интерфейс) + `JsonFileStorage` (Gson).
- **cli** — `Console` (ввод/вывод) + `CommandProcessor` (диспетчер).
- **logging** — `LogLevel`, `Logger`, `LoggerFactory`.
- **util** — валидация и форматирование.

Данные сохраняются в `data/users.json` при выходе и загружаются при старте.

## Зависимости

**Gson 2.10.1**.

## CI

`.github/workflows/ci.yml` запускает `mvn clean verify` и checkstyle при push/PR.
