package dao;

import model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // INSERT NEW ACTIVITY LOG
    public void logAction(
            int userId,
            String actionType,
            String description) {

        String sql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);

            ps.executeUpdate();

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to save activity log.", e);
        }
    }

    // INSERT ACTIVITY LOG USING AN EXISTING CONNECTION
    // THIS ALLOWS THE BUSINESS ACTION AND AUDIT LOG
    // TO BE SAVED IN THE SAME TRANSACTION.
    public void logAction(
            Connection conn,
            int userId,
            String actionType,
            String description) throws SQLException {

        String sql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);

            ps.executeUpdate();
        }
    }

    // GET ALL SYSTEM LOGS
    // ORDERED BY TIMESTAMP DESCENDING
    public List<ActivityLog> getAllLogs() {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT log_id, user_id, action_type,
                       description, timestamp
                FROM activity_logs
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                logs.add(mapActivityLog(rs));
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve system activity logs.", e);
        }

        return logs;
    }

    // GET LOGS BY SPECIFIC USER ID
    public List<ActivityLog> getLogsByUserId(int userId) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT log_id, user_id, action_type,
                       description, timestamp
                FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    logs.add(mapActivityLog(rs));
                }
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Failed to retrieve user activity logs.", e);
        }

        return logs;
    }

    // MAP DATABASE RECORD TO ACTIVITYLOG OBJECT
    private ActivityLog mapActivityLog(ResultSet rs)
            throws SQLException {

        return new ActivityLog(
                rs.getInt("log_id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}