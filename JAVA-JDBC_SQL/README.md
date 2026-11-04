# 🏦 Banking Management System (Java + JDBC + MySQL)

A console-based banking application built with **Java**, **JDBC** and **MySQL**. It supports account creation, deposits, withdrawals and fund transfers. Every money operation runs as a **database transaction** with commit and rollback, so money is never lost if something fails.

---

## 📌 About the Project

This project shows how a real banking flow works with plain JDBC. The database and tables are created from Java on the first run, demo data is added automatically, and existing data is kept on later runs.

The main idea is **safety**. If a transfer fails halfway (for example, the receiver account does not exist), the sender's debit is **rolled back** and no money is deducted.

---

## ✨ Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | Create account | Adds a customer, an account and an opening deposit in one transaction |
| 2 | Deposit | Adds money to an active account |
| 3 | Withdraw | Withdraws money with an insufficient-balance check |
| 4 | Fund transfer | Moves money between two accounts, with full rollback on failure |
| 5 | Account details | Shows customer and account details (JOIN) |
| 6 | Mini statement | Shows the last 10 transactions of an account |
| 7 | View all accounts | Lists all accounts with customer names (JOIN) |
| 8 | Search customer | Searches customers by part of the name (LIKE) |
| 9 | Minimum balance filter | Lists active accounts with at least a given balance |
| 10 | Summary report | Count, total, average and highest balance per account type (GROUP BY) |
| 11 | Update phone | Changes a customer's phone number with validation |
| 12 | Close account | Closes an account only when its balance is zero |
| 13 | Exit | Closes the connection and exits |

---

## 🧠 Concepts Used

- **JDBC** with a single shared `Connection` (opened once, closed once)
- **Transactions:** `setAutoCommit(false)`, `commit()` and `rollback()`
- **Row locking** with `SELECT ... FOR UPDATE` to avoid wrong balances
- **PreparedStatement** for every query (prevents SQL injection)
- **SQL concepts:** JOIN, LEFT JOIN, GROUP BY, LIKE, ORDER BY, LIMIT, aggregate functions
- **Constraints:** PRIMARY KEY, FOREIGN KEY, UNIQUE, NOT NULL, CHECK, DEFAULT, AUTO_INCREMENT
- **BigDecimal** for accurate money calculations
- **Exception handling** with try-with-resources
- **Input validation** with regular expressions (phone, email)
- **Java text blocks** for readable SQL and menus
- A reusable method that prints any `ResultSet` as a formatted table

---

## 🗄️ Database Design

Database name: `banking_jdbc_db` (created automatically)

### `customers`
| Column | Type | Constraint |
|--------|------|------------|
| customer_id | INT | Primary key, auto increment |
| name | VARCHAR(60) | Not null |
| phone | VARCHAR(10) | Not null, unique |
| email | VARCHAR(100) | Not null, unique |
| city | VARCHAR(40) | Not null |

### `accounts`
| Column | Type | Constraint |
|--------|------|------------|
| account_no | INT | Primary key, auto increment (starts at 1001) |
| customer_id | INT | Foreign key to `customers` |
| account_type | VARCHAR(10) | `Savings` or `Current` |
| balance | DECIMAL(12,2) | Must be 0 or more |
| status | VARCHAR(10) | `Active` or `Closed` (default `Active`) |
| opened_date | DATE | Not null |

### `transactions`
| Column | Type | Constraint |
|--------|------|------------|
| txn_id | INT | Primary key, auto increment |
| account_no | INT | Foreign key to `accounts` |
| txn_type | VARCHAR(15) | `DEPOSIT`, `WITHDRAW`, `TRANSFER_OUT`, `TRANSFER_IN` |
| amount | DECIMAL(12,2) | Must be greater than 0 |
| balance_after | DECIMAL(12,2) | Balance after the transaction |
| related_account | INT | Other account in a transfer (can be null) |
| remarks | VARCHAR(60) | Short note |
| txn_time | TIMESTAMP | Defaults to current time |

**Relationship:** `customers` (1) → (many) `accounts` (1) → (many) `transactions`

---

## 📏 Business Rules and Validation

- Minimum opening deposit is **₹500**
- Phone number must be **10 digits and start with 6, 7, 8 or 9**
- Email must be in a valid format (for example `name@domain.com`)
- Phone and email must be **unique**
- Account type must be **Savings** or **Current**
- Amounts must be greater than 0 with at most 2 decimal places
- A withdrawal or transfer cannot exceed the available balance
- Sender and receiver of a transfer cannot be the same account
- Transfers to a missing or closed account fail and are rolled back
- An account can be closed only if it is **Active** and has **zero balance**

---

## 🔐 How Transactions Work

Every money operation follows this pattern:

```
setAutoCommit(false)
   → lock the row (SELECT ... FOR UPDATE)
   → do the work (UPDATE / INSERT)
commit()                       ← everything succeeded
on any SQLException → rollback()  ← everything is undone
finally → setAutoCommit(true)
```

**Fund transfer steps:**
1. Debit the sender
2. Credit the receiver (fails if the account is missing or closed)
3. Record both transaction entries
4. Commit. If any step fails, **all steps are rolled back**.

---

## 🧰 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 15 or later (uses text blocks) |
| Database | MySQL 8.0.16 or later (needed for CHECK constraints) |
| Connectivity | JDBC with MySQL Connector/J |
| Interface | Console (command line) |

---

## 🚀 Getting Started

### Prerequisites
- JDK 15 or later
- MySQL Server running on `localhost:3306`
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) JAR file

### 1. Set your MySQL password

Open `BankingSystem.java` and change these lines if needed:

```java
private static final String USER = "root";
private static final String PASSWORD = "root";   // change to your MySQL password
```

### 2. Compile

```bash
javac -cp ".;mysql-connector-j-x.x.x.jar" BankingSystem.java
```

### 3. Run

**Windows**
```bash
java -cp ".;mysql-connector-j-x.x.x.jar" BankingSystem
```

**Linux / macOS**
```bash
javac -cp ".:mysql-connector-j-x.x.x.jar" BankingSystem.java
java -cp ".:mysql-connector-j-x.x.x.jar" BankingSystem
```

Replace `x.x.x` with your Connector/J version. In an IDE (IntelliJ, Eclipse, VS Code), add the connector JAR to the project libraries and run `BankingSystem`.

On the first run, the program creates the database, the tables and three demo accounts.

---

## 🧪 Demo Data (added automatically on first run)

| Account No | Customer | Type | City |
|------------|----------|------|------|
| 1001 | Arun Kumar | Savings | Chennai |
| 1002 | Priya Sharma | Savings | Bengaluru |
| 1003 | Ravi Traders | Current | Hyderabad |

The demo data also includes one deposit and one transfer, so the mini statement has records to show.

---

## 🖥️ Menu Preview

```
===== BANKING SYSTEM =====
 1. Create account (customer + account + opening deposit)
 2. Deposit
 3. Withdraw
 4. Fund transfer
 5. Account details (join)
 6. Mini statement (last 10 transactions)
 7. View all accounts (join)
 8. Search customer by name (LIKE)
 9. Accounts with balance of at least ...
10. Summary report by account type (GROUP BY)
11. Update customer phone
12. Close account
13. Exit
```

---

## 🔮 Future Improvements

- Login with PIN or password for customers
- Interest calculation for savings accounts
- Daily withdrawal and transfer limits
- Convert to a Spring Boot REST API with a React frontend
- Store database credentials in a config file or environment variables
- Add unit tests with JUnit

---

## 📁 Project Structure

```
BankingSystem/
├── BankingSystem.java      # Complete application (menu, logic, queries)
└── README.md
```

---

## 👨‍💻 Author

**Shyleshkanna B**
GitHub: [@shyleshcse](https://github.com/shyleshcse)

---

⭐ If you like this project, give it a star!
