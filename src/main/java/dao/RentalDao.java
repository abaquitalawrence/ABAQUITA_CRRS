package dao;

import model.Rental;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalDao {

    private static final String SELECT_RENTALS = """
            SELECT rt.rental_id, rt.customer_id,
                   u.username AS customer_name,
                   rt.clothing_id,
                   c.clothing_name,
                   rt.rental_date,
                   rt.status
            FROM rental_transactions rt
            JOIN users u ON rt.customer_id = u.id
            JOIN clothing_items c ON rt.clothing_id = c.clothing_id
            """;

    // CREATE RENTAL
    public boolean createRental(int customerId, int clothingId) {

        String updateClothing = """
                UPDATE clothing_items
                SET availability = 'UNAVAILABLE'
                WHERE clothing_id = ?
                  AND availability = 'AVAILABLE'
                """;

        String insertRental = """
                INSERT INTO rental_transactions
                (customer_id, clothing_id, status)
                VALUES (?, ?, 'ACTIVE')
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement update =
                         connection.prepareStatement(updateClothing);
                 PreparedStatement insert =
                         connection.prepareStatement(insertRental)) {

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
            throw new IllegalStateException("Failed to create rental.", e);
        }
    }

    // VIEW ALL RENTALS
    public List<Rental> getAllRentals() {
        return getRentals(SELECT_RENTALS + " ORDER BY rt.rental_id");
    }

    // VIEW RENTALS OF ONE CUSTOMER
    public List<Rental> getRentalsByCustomer(int customerId) {

        String sql = SELECT_RENTALS
                + " WHERE rt.customer_id = ? ORDER BY rt.rental_id";

        List<Rental> rentals = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rentals.add(mapRental(rs));
                }
            }

            return rentals;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to retrieve customer rentals.", e);
        }
    }

    // FIND RENTAL
    public Rental getRentalById(int rentalId) {

        String sql = SELECT_RENTALS + " WHERE rt.rental_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, rentalId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRental(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find rental.", e);
        }
    }

    // UPDATE RENTAL STATUS
    public boolean updateRentalStatus(int rentalId, String status) {

        if (!status.equals("ACTIVE")
                && !status.equals("RETURNED")
                && !status.equals("CANCELLED")) {
            return false;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                Rental rental = getRentalUsingConnection(connection, rentalId);

                if (rental == null) {
                    connection.rollback();
                    return false;
                }

                String sql = """
                        UPDATE rental_transactions
                        SET status = ?
                        WHERE rental_id = ?
                        """;

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, status);
                    statement.setInt(2, rentalId);

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                if (status.equals("RETURNED") || status.equals("CANCELLED")) {
                    setClothingAvailable(connection, rental.getClothingId());
                } else {
                    String updateClothing = """
                            UPDATE clothing_items
                            SET availability = 'UNAVAILABLE'
                            WHERE clothing_id = ?
                            """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(updateClothing)) {
                        statement.setInt(1, rental.getClothingId());
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update rental.", e);
        }
    }

    // DELETE RENTAL
    public boolean deleteRental(int rentalId) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                Rental rental = getRentalUsingConnection(connection, rentalId);

                if (rental == null) {
                    connection.rollback();
                    return false;
                }

                String sql = "DELETE FROM rental_transactions WHERE rental_id = ?";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, rentalId);

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                setClothingAvailable(connection, rental.getClothingId());

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete rental.", e);
        }
    }

    private List<Rental> getRentals(String sql) {

        List<Rental> rentals = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                rentals.add(mapRental(rs));
            }

            return rentals;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to retrieve rentals.", e);
        }
    }

    private Rental getRentalUsingConnection(
            Connection connection, int rentalId) throws SQLException {

        String sql = SELECT_RENTALS + " WHERE rt.rental_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, rentalId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRental(rs);
                }
            }
        }

        return null;
    }

    private Rental mapRental(ResultSet rs) throws SQLException {

        return new Rental(
                rs.getInt("rental_id"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getInt("clothing_id"),
                rs.getString("clothing_name"),
                rs.getString("rental_date"),
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
