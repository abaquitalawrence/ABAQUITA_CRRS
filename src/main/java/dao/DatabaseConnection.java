
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:clothing_rental.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");

            try (var result = statement.executeQuery("PRAGMA foreign_keys")) {
                if (!result.next() || result.getInt(1) != 1) {
                    throw new SQLException(
                            "SQLite foreign-key constraints could not be enabled."
                    );
                }
            }

            return connection;

        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }
}
