
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
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """;

        String createClothingItemsTable = """
                CREATE TABLE IF NOT EXISTS clothing_items (
                    clothing_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    clothing_name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    size TEXT NOT NULL,
                    color TEXT NOT NULL,
                    rental_price REAL NOT NULL CHECK (rental_price >= 0),
                    availability TEXT NOT NULL DEFAULT 'AVAILABLE'
                        CHECK (availability IN ('AVAILABLE', 'UNAVAILABLE'))
                )
                """;

        String createReservationsTable = """
                CREATE TABLE IF NOT EXISTS reservations (
                    reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    clothing_id INTEGER NOT NULL,
                    reservation_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN (
                            'PENDING', 'APPROVED', 'REJECTED',
                            'CANCELLED', 'COMPLETED'
                        )),
                    FOREIGN KEY (customer_id) REFERENCES users(id),
                    FOREIGN KEY (clothing_id) REFERENCES clothing_items(clothing_id)
                )
                """;

        String createRentalTransactionsTable = """
                CREATE TABLE IF NOT EXISTS rental_transactions (
                    rental_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    clothing_id INTEGER NOT NULL,
                    rental_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status TEXT NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'RETURNED', 'CANCELLED')),
                    FOREIGN KEY (customer_id) REFERENCES users(id),
                    FOREIGN KEY (clothing_id) REFERENCES clothing_items(clothing_id)
                )
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createUsersTable);
            statement.executeUpdate(createActivityLogsTable);
            statement.executeUpdate(createClothingItemsTable);
            statement.executeUpdate(createReservationsTable);
            statement.executeUpdate(createRentalTransactionsTable);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to initialize the database.", e);
        }
    }
}
