import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * PROJECT 1: Banking Management System
 *
 * Tables  : customers, accounts, transactions   (created from Java)
 * Concepts: Transactions (commit / rollback), Exception handling, PreparedStatement,
 *           Joins, GROUP BY, LIKE search, Validation, ONE shared connection.
 * Advanced: money is NEVER deducted if a transfer fails (rollback).

 */
public class BankingSystem {

    // ------------------------------------------------------------------
    // 1. CONNECTION DETAILS (change PASSWORD to your MySQL password)
    // ------------------------------------------------------------------
    private static final String URL = "jdbc:mysql://localhost:3306/";   // server only, no database yet
    private static final String DB_NAME = "banking_jdbc_db";            // change this name if you want
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static final BigDecimal MIN_OPENING_DEPOSIT = new BigDecimal("500");

    private static Connection con;                       // opened ONCE, reused everywhere
    private static final Scanner sc = new Scanner(System.in);

    // ------------------------------------------------------------------
    // 2. MAIN
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);   // ONE connection for the whole program

            resetDatabase();      // create the database if it does not already exist and keep existing data
            createTables();
            seedIfEmpty();

            int choice;
            do {
                printMenu();
                choice = readInt("Enter choice: ");
                try {
                    switch (choice) {
                        case 1:  createAccountMenu(); break;
                        case 2:  depositMenu(); break;
                        case 3:  withdrawMenu(); break;
                        case 4:  transferMenu(); break;
                        case 5:  accountDetails(); break;
                        case 6:  miniStatement(); break;
                        case 7:  viewAllAccounts(); break;
                        case 8:  searchCustomerByName(); break;
                        case 9:  accountsWithMinBalance(); break;
                        case 10: summaryReport(); break;
                        case 11: updatePhone(); break;
                        case 12: closeAccount(); break;
                        case 13: System.out.println("Thank you for banking with us. Bye!"); break;
                        default: System.out.println("Invalid choice, try 1 to 13.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
            } while (choice != 13);

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Could not start: " + e.getMessage());
        } finally {
            try {
                if (con != null) con.close();            // closed ONCE
            } catch (SQLException e) {
                System.out.println("Close failed: " + e.getMessage());
            }
            sc.close();
        }
    }

    static void printMenu() {
        System.out.println("""

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
                13. Exit""");
    }

    // ------------------------------------------------------------------
    // 3A. KEEP EXISTING DATABASE
    // We do not delete the old database on every run.
    // CREATE DATABASE IF NOT EXISTS creates the database only if it does not exist.
    // If the database already exists, all existing data is preserved.
    // Then setCatalog() switches the SAME connection to the database.
    // ------------------------------------------------------------------
    static void resetDatabase() throws SQLException {
        try (Statement st = con.createStatement()) {
            // st.execute("DROP DATABASE IF EXISTS " + DB_NAME);
            st.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        }
        con.setCatalog(DB_NAME);                             // same as: USE DB_NAME
        System.out.println("Fresh database '" + DB_NAME + "' created.");
    }

    // ------------------------------------------------------------------
    // 3. TABLE CREATION WITH CONSTRAINTS
    // ------------------------------------------------------------------
    static void createTables() throws SQLException {
        String customers = """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INT PRIMARY KEY AUTO_INCREMENT,
                    name        VARCHAR(60)  NOT NULL,
                    phone       VARCHAR(10)  NOT NULL UNIQUE,
                    email       VARCHAR(100) NOT NULL UNIQUE,
                    city        VARCHAR(40)  NOT NULL
                )""";

        String accounts = """
                CREATE TABLE IF NOT EXISTS accounts (
                    account_no   INT PRIMARY KEY AUTO_INCREMENT,
                    customer_id  INT NOT NULL,
                    account_type VARCHAR(10) NOT NULL
                                 CHECK (account_type IN ('Savings', 'Current')),
                    balance      DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
                    status       VARCHAR(10) NOT NULL DEFAULT 'Active'
                                 CHECK (status IN ('Active', 'Closed')),
                    opened_date  DATE NOT NULL,
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
                ) AUTO_INCREMENT = 1001""";

        String transactions = """
                CREATE TABLE IF NOT EXISTS transactions (
                    txn_id          INT PRIMARY KEY AUTO_INCREMENT,
                    account_no      INT NOT NULL,
                    txn_type        VARCHAR(15) NOT NULL
                                    CHECK (txn_type IN ('DEPOSIT', 'WITHDRAW',
                                                        'TRANSFER_OUT', 'TRANSFER_IN')),
                    amount          DECIMAL(12,2) NOT NULL CHECK (amount > 0),
                    balance_after   DECIMAL(12,2) NOT NULL,
                    related_account INT NULL,
                    remarks         VARCHAR(60),
                    txn_time        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (account_no) REFERENCES accounts(account_no)
                )""";

        try (Statement st = con.createStatement()) {
            st.execute(customers);
            st.execute(accounts);
            st.execute(transactions);
        }
        System.out.println("Tables are ready (created with constraints if they did not exist).");
    }

    static void seedIfEmpty() throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customers")) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }
        int a1 = createAccount("Arun Kumar",   "9876543210", "arun@gmail.com",  "Chennai",   "Savings", new BigDecimal("25000"));
        int a2 = createAccount("Priya Sharma", "9123456780", "priya@gmail.com", "Bengaluru", "Savings", new BigDecimal("40000"));
        int a3 = createAccount("Ravi Traders", "9988776655", "ravi@gmail.com",  "Hyderabad", "Current", new BigDecimal("100000"));
        deposit(a1, new BigDecimal("5000"));
        transfer(a2, a1, new BigDecimal("2000"));
        System.out.println("Demo data added. Demo accounts: " + a1 + ", " + a2 + ", " + a3);
    }

    // ------------------------------------------------------------------
    // 4. VALIDATION METHODS
    // ------------------------------------------------------------------
    static boolean isValidPhone(String phone) {
        return phone.matches("[6-9]\\d{9}");
    }

    static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    static boolean isValidAccountType(String type) {
        return type.equalsIgnoreCase("Savings") || type.equalsIgnoreCase("Current");
    }

    // ------------------------------------------------------------------
    // 5. INPUT HELPERS
    // ------------------------------------------------------------------
    static String readText(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readText(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    /** Reads a money value. Keeps asking until it is a positive number. */
    static BigDecimal readAmount(String prompt) {
        while (true) {
            try {
                BigDecimal value = new BigDecimal(readText(prompt));
                if (value.signum() > 0 && value.scale() <= 2) return value;
                System.out.println("Amount must be greater than 0 with at most 2 decimal places.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    // ------------------------------------------------------------------
    // 6. ONE PRINT METHOD USED BY EVERY SELECT
    // ------------------------------------------------------------------
    static boolean printResults(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        int[] width = new int[cols];
        for (int i = 0; i < cols; i++) {
            width[i] = md.getColumnLabel(i + 1).length();
        }
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[cols];
            for (int i = 0; i < cols; i++) {
                String value = rs.getString(i + 1);
                row[i] = (value == null) ? "-" : value;
                width[i] = Math.max(width[i], row[i].length());
            }
            rows.add(row);
        }
        if (rows.isEmpty()) {
            System.out.println("No records found.");
            return false;
        }
        StringBuilder border = new StringBuilder("+");
        for (int w : width) border.append("-".repeat(w + 2)).append("+");
        System.out.println(border);
        StringBuilder head = new StringBuilder("|");
        for (int i = 0; i < cols; i++) {
            head.append(String.format(" %-" + width[i] + "s |", md.getColumnLabel(i + 1)));
        }
        System.out.println(head);
        System.out.println(border);
        for (String[] row : rows) {
            StringBuilder line = new StringBuilder("|");
            for (int i = 0; i < cols; i++) {
                line.append(String.format(" %-" + width[i] + "s |", row[i]));
            }
            System.out.println(line);
        }
        System.out.println(border);
        System.out.println(rows.size() + " record(s).");
        return true;
    }

    static void runQuery(String sql, String... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                printResults(rs);
            }
        }
    }

    // ------------------------------------------------------------------
    // 7. CORE BANKING LOGIC (each method is ONE transaction)
    //    Pattern: setAutoCommit(false) -> work -> commit()
    //             on any SQLException -> rollback() and re-throw
    // ------------------------------------------------------------------

    /** Reads the balance of an ACTIVE account and LOCKS the row until commit/rollback. */
    private static BigDecimal lockBalance(int accountNo) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_no = ? AND status = 'Active' FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Account " + accountNo + " does not exist or is closed.");
                }
                return rs.getBigDecimal(1);
            }
        }
    }

    private static void addTransaction(int accountNo, String type, BigDecimal amount,
                                       BigDecimal balanceAfter, Integer related, String remarks)
            throws SQLException {
        String sql = "INSERT INTO transactions (account_no, txn_type, amount, balance_after, "
                   + "related_account, remarks) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.setBigDecimal(4, balanceAfter);
            if (related == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, related);
            }
            ps.setString(6, remarks);
            ps.executeUpdate();
        }
    }

    private static void rollbackQuietly(SQLException original) {
        try {
            con.rollback();                                  // UNDO everything in this transaction
        } catch (SQLException ex) {
            original.addSuppressed(ex);
        }
    }

    static int createAccount(String name, String phone, String email, String city,
                             String type, BigDecimal openingDeposit) throws SQLException {
        try {
            con.setAutoCommit(false);

            int customerId;
            String c = "INSERT INTO customers (name, phone, email, city) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(c, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, email);
                ps.setString(4, city);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    customerId = keys.getInt(1);
                }
            }

            int accountNo;
            String a = "INSERT INTO accounts (customer_id, account_type, balance, opened_date) "
                     + "VALUES (?, ?, ?, CURDATE())";
            try (PreparedStatement ps = con.prepareStatement(a, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.setString(2, type);
                ps.setBigDecimal(3, openingDeposit);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    accountNo = keys.getInt(1);
                }
            }

            addTransaction(accountNo, "DEPOSIT", openingDeposit, openingDeposit, null, "Opening deposit");
            con.commit();
            return accountNo;
        } catch (SQLException e) {
            rollbackQuietly(e);
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    static void deposit(int accountNo, BigDecimal amount) throws SQLException {
        try {
            con.setAutoCommit(false);
            BigDecimal newBalance = lockBalance(accountNo).add(amount);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE accounts SET balance = ? WHERE account_no = ?")) {
                ps.setBigDecimal(1, newBalance);
                ps.setInt(2, accountNo);
                ps.executeUpdate();
            }
            addTransaction(accountNo, "DEPOSIT", amount, newBalance, null, "Cash deposit");
            con.commit();
        } catch (SQLException e) {
            rollbackQuietly(e);
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    static void withdraw(int accountNo, BigDecimal amount) throws SQLException {
        try {
            con.setAutoCommit(false);
            BigDecimal balance = lockBalance(accountNo);
            if (balance.compareTo(amount) < 0) {
                throw new SQLException("Insufficient balance. Available: " + balance);
            }
            BigDecimal newBalance = balance.subtract(amount);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE accounts SET balance = ? WHERE account_no = ?")) {
                ps.setBigDecimal(1, newBalance);
                ps.setInt(2, accountNo);
                ps.executeUpdate();
            }
            addTransaction(accountNo, "WITHDRAW", amount, newBalance, null, "Cash withdrawal");
            con.commit();
        } catch (SQLException e) {
            rollbackQuietly(e);
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    /**
     * FUND TRANSFER. The debit is done FIRST on purpose, so you can see the rollback work:
     * if the credit step fails (for example the receiver account does not exist),
     * the debit is undone and the sender loses no money.
     */
    static void transfer(int from, int to, BigDecimal amount) throws SQLException {
        if (from == to) {
            throw new SQLException("Sender and receiver account cannot be the same.");
        }
        try {
            con.setAutoCommit(false);

            // Step 1: debit the sender
            BigDecimal senderBalance = lockBalance(from);
            if (senderBalance.compareTo(amount) < 0) {
                throw new SQLException("Insufficient balance. Available: " + senderBalance);
            }
            BigDecimal senderNew = senderBalance.subtract(amount);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE accounts SET balance = ? WHERE account_no = ?")) {
                ps.setBigDecimal(1, senderNew);
                ps.setInt(2, from);
                ps.executeUpdate();
            }

            // Step 2: credit the receiver (fails if the account is missing or closed)
            BigDecimal receiverNew = lockBalance(to).add(amount);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE accounts SET balance = ? WHERE account_no = ?")) {
                ps.setBigDecimal(1, receiverNew);
                ps.setInt(2, to);
                ps.executeUpdate();
            }

            // Step 3: record both sides
            addTransaction(from, "TRANSFER_OUT", amount, senderNew, to, "Transfer to " + to);
            addTransaction(to, "TRANSFER_IN", amount, receiverNew, from, "Transfer from " + from);

            con.commit();                                    // all 3 steps succeeded
        } catch (SQLException e) {
            rollbackQuietly(e);                              // debit is undone too
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    // ------------------------------------------------------------------
    // 8. MENU ACTIONS (read input, validate, call the logic, show result)
    // ------------------------------------------------------------------
    static void createAccountMenu() {
        String name = readText("Customer name: ");
        String phone = readText("Phone (10 digits): ");
        String email = readText("Email: ");
        String city = readText("City: ");
        String type = readText("Account type (Savings/Current): ");
        BigDecimal opening = readAmount("Opening deposit (minimum 500): ");

        if (name.isEmpty() || city.isEmpty()) {
            System.out.println("Name and city cannot be empty.");
            return;
        }
        if (!isValidPhone(phone)) {
            System.out.println("Phone must be 10 digits and start with 6, 7, 8 or 9.");
            return;
        }
        if (!isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }
        if (!isValidAccountType(type)) {
            System.out.println("Account type must be Savings or Current.");
            return;
        }
        if (opening.compareTo(MIN_OPENING_DEPOSIT) < 0) {
            System.out.println("Opening deposit must be at least " + MIN_OPENING_DEPOSIT + ".");
            return;
        }
        type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();

        try {
            int accountNo = createAccount(name, phone, email, city, type, opening);
            System.out.println("Account created successfully. Account number: " + accountNo);
        } catch (SQLException e) {
            System.out.println("Account NOT created (rolled back): " + e.getMessage());
        }
    }

    static void depositMenu() {
        int acc = readInt("Account number: ");
        BigDecimal amt = readAmount("Amount to deposit: ");
        try {
            deposit(acc, amt);
            System.out.println("Deposit successful. New balance: " + balanceOf(acc));
        } catch (SQLException e) {
            System.out.println("Deposit failed (rolled back): " + e.getMessage());
        }
    }

    static void withdrawMenu() {
        int acc = readInt("Account number: ");
        BigDecimal amt = readAmount("Amount to withdraw: ");
        try {
            withdraw(acc, amt);
            System.out.println("Withdrawal successful. New balance: " + balanceOf(acc));
        } catch (SQLException e) {
            System.out.println("Withdrawal failed (rolled back): " + e.getMessage());
        }
    }

    static void transferMenu() {
        int from = readInt("From account number: ");
        int to = readInt("To account number: ");
        BigDecimal amt = readAmount("Amount to transfer: ");
        try {
            transfer(from, to, amt);
            System.out.println("Transfer successful. Your new balance: " + balanceOf(from));
        } catch (SQLException e) {
            System.out.println("Transfer FAILED. No money was deducted (rolled back): " + e.getMessage());
        }
    }

    static String balanceOf(int accountNo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT balance FROM accounts WHERE account_no = ?")) {
            ps.setInt(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1).toPlainString() : "unknown";
            }
        }
    }

    // ------------------------------------------------------------------
    // 9. QUERIES: JOIN, LIKE, GROUP BY, custom filters
    // ------------------------------------------------------------------
    static void accountDetails() throws SQLException {
        int acc = readInt("Account number: ");
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT a.account_no, c.name, c.phone, a.account_type, a.balance, a.status, a.opened_date
                FROM accounts a
                JOIN customers c ON a.customer_id = c.customer_id
                WHERE a.account_no = ?""")) {
            ps.setInt(1, acc);
            try (ResultSet rs = ps.executeQuery()) {
                printResults(rs);
            }
        }
    }

    static void miniStatement() throws SQLException {
        int acc = readInt("Account number: ");
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT txn_id, txn_type, amount, balance_after, related_account, remarks,
                       DATE_FORMAT(txn_time, '%Y-%m-%d %H:%i:%s') AS txn_time
                FROM transactions
                WHERE account_no = ?
                ORDER BY txn_id DESC
                LIMIT 10""")) {
            ps.setInt(1, acc);
            try (ResultSet rs = ps.executeQuery()) {
                printResults(rs);
            }
        }
    }

    static void viewAllAccounts() throws SQLException {
        runQuery("""
                SELECT a.account_no, c.name, a.account_type, a.balance, a.status
                FROM accounts a
                JOIN customers c ON a.customer_id = c.customer_id
                ORDER BY a.account_no""");
    }

    static void searchCustomerByName() throws SQLException {
        String word = readText("Enter part of the name: ");
        runQuery("""
                SELECT c.customer_id, c.name, c.phone, c.city, a.account_no, a.balance
                FROM customers c
                LEFT JOIN accounts a ON a.customer_id = c.customer_id
                WHERE c.name LIKE ?""", "%" + word + "%");
    }

    static void accountsWithMinBalance() throws SQLException {
        BigDecimal min = readAmount("Minimum balance: ");
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT a.account_no, c.name, a.balance
                FROM accounts a
                JOIN customers c ON a.customer_id = c.customer_id
                WHERE a.balance >= ? AND a.status = 'Active'
                ORDER BY a.balance DESC""")) {
            ps.setBigDecimal(1, min);
            try (ResultSet rs = ps.executeQuery()) {
                printResults(rs);
            }
        }
    }

    static void summaryReport() throws SQLException {
        runQuery("""
                SELECT account_type,
                       COUNT(*)     AS accounts,
                       SUM(balance) AS total_balance,
                       AVG(balance) AS average_balance,
                       MAX(balance) AS highest_balance
                FROM accounts
                WHERE status = 'Active'
                GROUP BY account_type""");
    }

    static void updatePhone() throws SQLException {
        int id = readInt("Customer id: ");
        String phone = readText("New phone (10 digits): ");
        if (!isValidPhone(phone)) {
            System.out.println("Phone must be 10 digits and start with 6, 7, 8 or 9.");
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE customers SET phone = ? WHERE customer_id = ?")) {
            ps.setString(1, phone);
            ps.setInt(2, id);
            System.out.println(ps.executeUpdate() > 0 ? "Phone updated." : "Customer id not found.");
        } catch (SQLException e) {
            System.out.println("Could not update (phone may belong to another customer): " + e.getMessage());
        }
    }

    /** An account can be closed only when its balance is zero. */
    static void closeAccount() throws SQLException {
        int acc = readInt("Account number to close: ");
        String sql = "UPDATE accounts SET status = 'Closed' WHERE account_no = ? "
                   + "AND status = 'Active' AND balance = 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, acc);
            System.out.println(ps.executeUpdate() > 0 ? "Account closed."
                    : "Not closed. The account must exist, be Active and have zero balance (withdraw first).");
        }
    }
}
