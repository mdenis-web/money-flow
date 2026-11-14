# Project Money-Flow

## Описание

Приложение для управления личными финансами на Java.
Данные хранятся в памяти и сохраняются в JSON-файл (`data/users.json`).
Интерфейс — интерактивный CLI.
Логирование — кастомный логгер без внешних зависимостей, пишет в `logs/app.log` и `logs/error.log`.

---

## 🚀 Сборка и запуск

### Сборка проекта
```bash
mvn clean package
```

### Запуск приложения

**Через Maven:**
```bash
mvn exec:java -Dexec.mainClass=com.moneyflow.Main
```

**Напрямую через Java (fat JAR):**
```bash
java -jar target/money-flow-1.0.0.jar
```

---

## 🕹️ Команды CLI

После запуска вы увидите приглашение `>`.

| Команда | Описание |
| :--- | :--- |
| `register <login> <password>` | Зарегистрировать нового пользователя. |
| `login <login> <password>` | Войти в аккаунт. |
| `logout` | Выйти из аккаунта. |
| `add-income <category> <amount>` | Добавить доход. |
| `add-expense <category> <amount>` | Добавить расход. |
| `add-category <name>` | Создать категорию. |
| `set-budget <category> <amount>` | Установить бюджет для категории. |
| `edit-budget <category> <amount>` | Изменить бюджет категории. |
| `delete-budget <category>` | Удалить бюджет категории. |
| `delete-category <category>` | Удалить категорию. |
| `report` | Полный отчёт по кошельку. |
| `report-category <category>` | Отчёт по одной категории. |
| `report-categories <c1> <c2>...` | Отчёт по нескольким категориям. |
| `report-period <yyyy-MM-dd> <yyyy-MM-dd>` | Отчёт за период. |
| `transfer <toLogin> <amount>` | Перевод средств другому пользователю. |
| `export <path.json>` | Экспортировать кошелёк в файл. |
| `import <path.json>` | Импортировать кошелёк из файла. |
| `help` | Показать список команд. |
| `exit` \| `quit` | Выйти из приложения. |

### 💡 Примеры использования:

**Регистрация, добавление операций и просмотр отчёта:**
```text
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

---

## 🏗️ Архитектура проекта

Проект построен по принципам **слоёной архитектуры** с чётким разделением ответственности. Зависимости передаются вручную в `Main`.

*   **`model`** — Сущности (`User`, `Wallet`, `Transaction`, `Income`, `Expense`, `Category`, `TransactionType`).
*   **`service`** — Бизнес-логика (`AuthService`, `FinanceManager`, `ReportGenerator`, `AlertService`).
*   **`storage`** — Интерфейс `Storage` и реализация `JsonFileStorage` (Gson). Данные сохраняются в `data/users.json` при выходе и загружаются при старте.
*   **`cli`** — Консольный ввод/вывод (`Console`) и диспетчер команд (`CommandProcessor`).
*   **`logging`** — Кастомный логгер (`Logger`, `LoggerFactory`, `LogLevel`).
*   **`util`** — Вспомогательные утилиты (`Validator`).
*   **`exception`** — Типизированные исключения (`AuthException`, `NotFoundException`, `ValidationException`).

---

## 📝 Логирование

Все действия пользователя и системные события записываются в каталог `logs/`.

| Файл | Уровень | Содержимое |
| :--- | :--- | :--- |
| `logs/app.log` | INFO+ | Все действия: логин, операции, отчёты, сохранения |
| `logs/error.log` | ERROR | Только ошибки (с полным стектрейсом) |

Формат логов: `[Дата-Время] [Поток] УРОВЕНЬ Логгер - Сообщение`

*Пример лога:*
```text
2026-11-14 12:00:00.000 [main] INFO  com.moneyflow.service.AuthService - user registered: alice
2026-11-14 12:00:01.000 [main] WARN  com.moneyflow.service.AlertService - budget exceeded: Food 500/4000
2026-11-14 12:00:02.000 [main] ERROR com.moneyflow.storage.JsonFileStorage - failed to save data
```

Настройка через `LoggerFactory.configure(...)` в `Main`:

```java
LoggerFactory.configure(
    Path.of("logs"),   // каталог логов
    LogLevel.INFO,     // минимальный уровень
    false              // эхо в консоль
);
```

---

## 🧪 Тестирование

В проекте тестами покрыты все ключевые сценарии: модели кошелька, аутентификация, бизнес-операции, генерация отчётов, хранилище и логгер.

Запуск тестов:
```bash
mvn test
```

Проверка стиля кода (Checkstyle):
```bash
mvn checkstyle:check
```

---

## 🔄 CI/CD

В проекте настроен **GitHub Actions** (`.github/workflows/ci.yml`).
При каждом пуше в ветки `main`/`master` и при открытии pull request автоматически:
1. Собирается проект и запускаются все тесты (`mvn clean verify`).
2. Проверяется стиль кода (`mvn checkstyle:check`).

---
