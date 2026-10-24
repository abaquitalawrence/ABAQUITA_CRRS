
package dao;

import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

    public Optional<User> login(String username, String password) {

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
                    return Optional.of(mapUser(rs));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Login database error.", e);
        }

        return Optional.empty();
    }

    // DELIVERABLE 5: GET ALL USERS
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                ORDER BY id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to retrieve users.", e);
        }

        return users;
    }

    // DELIVERABLE 5: GET PENDING USERS
    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE status = 'PENDING'
                ORDER BY id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to retrieve pending users.", e);
        }

        return users;
    }

    // DELIVERABLE 5: APPROVE USER
    public boolean approveUser(int userId) {

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ? AND status = 'PENDING'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to approve user.", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("roles"),
                rs.getString("status")
        );
    }
}
