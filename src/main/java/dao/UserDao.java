package dao;

import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class UserDao {

    private final ActivityLogDao activityLogDao =
            new ActivityLogDao();

    // CHECK IF USERNAME EXISTS
    public boolean usernameExists(String username) {

        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to check username.", e);
        }
    }

    // REGISTER USER AND RECORD ACTIVITY
    public boolean register(
            String username,
            String password,
            String role) {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()
                || role == null) {

            return false;
        }

        String normalizedRole =
                role.trim().toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("ADMIN")
                && !normalizedRole.equals("STAFF")
                && !normalizedRole.equals("CUSTOMER")) {

            return false;
        }

        String sql = """
                INSERT INTO users
                (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                int newUserId;

                try (PreparedStatement ps =
                             conn.prepareStatement(sql)) {

                    ps.setString(1, username.trim());
                    ps.setString(2, password);
                    ps.setString(3, normalizedRole);

                    ps.executeUpdate();
                }

                // GET NEWLY CREATED USER ID
                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     "SELECT last_insert_rowid()");
                     ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException(
                                "Could not retrieve new user ID.");
                    }

                    newUserId = rs.getInt(1);
                }

                // RECORD REGISTRATION ACTIVITY
                activityLogDao.logAction(
                        conn,
                        newUserId,
                        "ACCOUNT_REGISTERED",
                        "User " + username.trim()
                                + " registered with role "
                                + normalizedRole
                                + ". Account status: PENDING."
                );

                conn.commit();

                return true;

            } catch (SQLException e) {

                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Registration failed.", e);
        }
    }

    // LOGIN USER
    // SUCCESSFUL LOGIN LOGGING IS HANDLED IN AUTHENTICATION.JAVA
    // AFTER THE ACCOUNT APPROVAL CHECK.
    public Optional<User> login(
            String username,
            String password) {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

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

    // GET ALL USERS
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                ORDER BY id
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
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

    // GET PENDING USERS
    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE status = 'PENDING'
                ORDER BY id
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
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

    // APPROVE USER ACCOUNT AND RECORD ADMIN ACTIVITY
    public boolean approveUser(
            int targetUserId,
            int adminId) {

        if (targetUserId <= 0 || adminId <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ? AND status = 'PENDING'
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                int rowsUpdated;

                try (PreparedStatement ps =
                             conn.prepareStatement(sql)) {

                    ps.setInt(1, targetUserId);

                    rowsUpdated = ps.executeUpdate();
                }

                if (rowsUpdated != 1) {
                    conn.rollback();
                    return false;
                }

                // RECORD ACCOUNT APPROVAL
                activityLogDao.logAction(
                        conn,
                        adminId,
                        "ACCOUNT_APPROVED",
                        "Admin [" + adminId
                                + "] approved account for User ID "
                                + targetUserId + "."
                );

                conn.commit();

                return true;

            } catch (SQLException e) {

                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to approve user.", e);
        }
    }

    // UPDATE USER ROLE AND STATUS
    // RECORD ADMIN ACTIVITY IN THE SAME TRANSACTION
    public boolean updateUserRoleAndStatus(
            int targetUserId,
            String newRole,
            String newStatus,
            int adminId) {

        if (targetUserId <= 0
                || adminId <= 0
                || targetUserId == adminId
                || newRole == null
                || newStatus == null) {

            return false;
        }

        String normalizedRole =
                newRole.trim().toUpperCase(Locale.ROOT);

        String normalizedStatus =
                newStatus.trim().toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("ADMIN")
                && !normalizedRole.equals("STAFF")
                && !normalizedRole.equals("CUSTOMER")) {

            return false;
        }

        if (!normalizedStatus.equals("APPROVED")
                && !normalizedStatus.equals("PENDING")) {

            return false;
        }

        String selectSql = """
                SELECT username, roles, status
                FROM users
                WHERE id = ?
                """;

        String updateSql = """
                UPDATE users
                SET roles = ?, status = ?
                WHERE id = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                String username;
                String oldRole;
                String oldStatus;

                // GET EXISTING USER INFORMATION
                try (PreparedStatement ps =
                             conn.prepareStatement(selectSql)) {

                    ps.setInt(1, targetUserId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        username = rs.getString("username");
                        oldRole = rs.getString("roles");
                        oldStatus = rs.getString("status");
                    }
                }

                // UPDATE USER ROLE AND STATUS
                int rowsUpdated;

                try (PreparedStatement ps =
                             conn.prepareStatement(updateSql)) {

                    ps.setString(1, normalizedRole);
                    ps.setString(2, normalizedStatus);
                    ps.setInt(3, targetUserId);

                    rowsUpdated = ps.executeUpdate();
                }

                if (rowsUpdated != 1) {
                    conn.rollback();
                    return false;
                }

                // RECORD ADMIN UPDATE ACTIVITY
                activityLogDao.logAction(
                        conn,
                        adminId,
                        "USER_UPDATED",
                        "Admin [" + adminId
                                + "] changed role of User ID "
                                + targetUserId
                                + " (" + username + ") from "
                                + oldRole + " to " + normalizedRole
                                + ", and status from "
                                + oldStatus + " to "
                                + normalizedStatus + "."
                );

                conn.commit();

                return true;

            } catch (SQLException e) {

                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to update user role and status.", e);
        }
    }

    // MAP DATABASE RECORD TO USER OBJECT
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