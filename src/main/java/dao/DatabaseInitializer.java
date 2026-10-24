
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

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createUsersTable);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to initialize the database.", e
            );
        }
    }
}
