
package dao;

import model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {

    private static final String SELECT_RESERVATIONS = """
            SELECT r.reservation_id, r.customer_id,
                   u.username AS customer_name,
                   r.clothing_id,
                   c.clothing_name,
                   r.reservation_date,
                   r.status
            FROM reservations r
            JOIN users u ON r.customer_id = u.id
            JOIN clothing_items c ON r.clothing_id = c.clothing_id
            """;

    // CREATE RESERVATION
    public boolean createReservation(int customerId, int clothingId) {

        String updateClothing = """
                UPDATE clothing_items
                SET availability = 'UNAVAILABLE'
                WHERE clothing_id = ?
                  AND availability = 'AVAILABLE'
                """;

        String insertReservation = """
                INSERT INTO reservations (customer_id, clothing_id)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement update =
                         connection.prepareStatement(updateClothing);
                 PreparedStatement insert =
                         connection.prepareStatement(insertReservation)) {

                update.setInt(1, clothingId);

                if (update.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }

                insert.setInt(1, customerId);
                insert.setInt(2, clothingId);
                insert.executeUpdate();

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create reservation.", e);
        }
    }

    // VIEW ALL RESERVATIONS
    public List<Reservation> getAllReservations() {
        return getReservations(SELECT_RESERVATIONS + " ORDER BY r.reservation_id");
    }

    // VIEW RESERVATIONS OF ONE CUSTOMER
    public List<Reservation> getReservationsByCustomer(int customerId) {

        String sql = SELECT_RESERVATIONS
                + " WHERE r.customer_id = ? ORDER BY r.reservation_id";

        List<Reservation> reservations = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapReservation(rs));
                }
            }

            return reservations;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to retrieve customer reservations.", e);
        }
    }

    // FIND RESERVATION
    public Reservation getReservationById(int reservationId) {

        String sql = SELECT_RESERVATIONS + " WHERE r.reservation_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, reservationId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find reservation.", e);
        }
    }

    // UPDATE RESERVATION STATUS
    public boolean updateReservationStatus(int reservationId, String status) {

        if (!status.equals("PENDING")
                && !status.equals("APPROVED")
                && !status.equals("REJECTED")
                && !status.equals("CANCELLED")
                && !status.equals("COMPLETED")) {
            return false;
        }

        String updateReservation = """
                UPDATE reservations
                SET status = ?
                WHERE reservation_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                Reservation reservation = getReservationUsingConnection(
                        connection, reservationId);

                if (reservation == null) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateReservation)) {

                    statement.setString(1, status);
                    statement.setInt(2, reservationId);

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                if (status.equals("REJECTED") || status.equals("CANCELLED")) {
                    setClothingAvailable(connection, reservation.getClothingId());
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update reservation.", e);
        }
    }

    // DELETE RESERVATION
    public boolean deleteReservation(int reservationId) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                Reservation reservation = getReservationUsingConnection(
                        connection, reservationId);

                if (reservation == null) {
                    connection.rollback();
                    return false;
                }

                String sql = "DELETE FROM reservations WHERE reservation_id = ?";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, reservationId);

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                if (!reservation.getStatus().equals("REJECTED")
                        && !reservation.getStatus().equals("CANCELLED")) {
                    setClothingAvailable(connection, reservation.getClothingId());
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete reservation.", e);
        }
    }

    private List<Reservation> getReservations(String sql) {

        List<Reservation> reservations = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapReservation(rs));
            }

            return reservations;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to retrieve reservations.", e);
        }
    }

    private Reservation getReservationUsingConnection(
            Connection connection, int reservationId) throws SQLException {

        String sql = SELECT_RESERVATIONS + " WHERE r.reservation_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, reservationId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }
        }

        return null;
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {

        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getInt("clothing_id"),
                rs.getString("clothing_name"),
                rs.getString("reservation_date"),
                rs.getString("status")
        );
    }

    private void setClothingAvailable(
            Connection connection, int clothingId) throws SQLException {

        String sql = """
                UPDATE clothing_items
                SET availability = 'AVAILABLE'
                WHERE clothing_id = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM rental_transactions
                      WHERE clothing_id = ?
                        AND status = 'ACTIVE'
                  )
                AND NOT EXISTS (
                      SELECT 1 FROM reservations
                      WHERE clothing_id = ?
                        AND status IN ('PENDING', 'APPROVED')
                  )
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clothingId);
            statement.setInt(2, clothingId);
            statement.setInt(3, clothingId);
            statement.executeUpdate();
        }
    }
}
