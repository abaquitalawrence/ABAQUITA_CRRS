
package dao;

import model.User;

import java.sql.*;
import java.util.Optional;

public class UserDao {

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to check username.", e);
        }
    }

    public boolean register(String username,
                            String password,
                            String role) {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()
                || role == null
                || !("ADMIN".equals(role)
                || "STAFF".equals(role)
                || "CUSTOMER".equals(role))) {
            return false;
        }

        String sql = """
                INSERT INTO users
                (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, role);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println(
                    "Registration error: " + e.getMessage());
            return false;
        }
    }

    public Optional<User> login(String username,
                                String password) {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status")
                    );

                    return Optional.of(user);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Login database error.", e);
        }

        return Optional.empty();
    }

    public boolean updateStatus(int userId, String status) {

        if (!("ACTIVE".equals(status)
                || "PENDING".equals(status)
                || "INACTIVE".equals(status))) {
            return false;
        }

        String sql = "UPDATE users SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to update account status.", e);
        }
    }
}
