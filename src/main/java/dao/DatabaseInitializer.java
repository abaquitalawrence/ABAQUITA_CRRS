package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {

        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL
                        CHECK (roles IN ('ADMIN', 'STAFF', 'CUSTOMER')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """;

        String createActivityLogsTable = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id)
                        REFERENCES users(id)
                )
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement =
                     connection.createStatement()) {

            // CREATE USERS TABLE IF IT DOES NOT EXIST
            statement.executeUpdate(createUsersTable);

            // CREATE ACTIVITY LOGS TABLE IF IT DOES NOT EXIST
            statement.executeUpdate(createActivityLogsTable);

            System.out.println(
                    "Database initialized successfully.");

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to initialize the database.", e);
        }
    }
}