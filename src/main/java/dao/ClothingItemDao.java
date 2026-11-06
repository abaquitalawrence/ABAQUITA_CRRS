
package dao;

import model.ClothingItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClothingItemDao {

    // CREATE PRODUCT
    public boolean addClothingItem(
            String name,
            String category,
            String size,
            String color,
            double rentalPrice,
            String availability) {

        String sql = """
                INSERT INTO clothing_items
                (clothing_name, category, size, color,
                 rental_price, availability)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, category);
            statement.setString(3, size);
            statement.setString(4, color);
            statement.setDouble(5, rentalPrice);
            statement.setString(6, availability);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to add clothing item.", e);
        }
    }

    // READ ALL PRODUCTS
    public List<ClothingItem> getAllClothingItems() {

        List<ClothingItem> items = new ArrayList<>();

        String sql = """
                SELECT clothing_id, clothing_name, category,
                       size, color, rental_price, availability
                FROM clothing_items
                ORDER BY clothing_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                items.add(new ClothingItem(
                        resultSet.getInt("clothing_id"),
                        resultSet.getString("clothing_name"),
                        resultSet.getString("category"),
                        resultSet.getString("size"),
                        resultSet.getString("color"),
                        resultSet.getDouble("rental_price"),
                        resultSet.getString("availability")
                ));
            }

            return items;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to retrieve clothing items.", e);
        }
    }

    // FIND PRODUCT BY ID
    public ClothingItem getClothingItemById(int clothingId) {

        String sql = """
                SELECT clothing_id, clothing_name, category,
                       size, color, rental_price, availability
                FROM clothing_items
                WHERE clothing_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, clothingId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return new ClothingItem(
                            resultSet.getInt("clothing_id"),
                            resultSet.getString("clothing_name"),
                            resultSet.getString("category"),
                            resultSet.getString("size"),
                            resultSet.getString("color"),
                            resultSet.getDouble("rental_price"),
                            resultSet.getString("availability")
                    );
                }

                return null;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find clothing item.", e);
        }
    }

    // UPDATE PRODUCT
    public boolean updateClothingItem(
            int clothingId,
            String name,
            String category,
            String size,
            String color,
            double rentalPrice,
            String availability) {

        String sql = """
                UPDATE clothing_items
                SET clothing_name = ?,
                    category = ?,
                    size = ?,
                    color = ?,
                    rental_price = ?,
                    availability = ?
                WHERE clothing_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, category);
            statement.setString(3, size);
            statement.setString(4, color);
            statement.setDouble(5, rentalPrice);
            statement.setString(6, availability);
            statement.setInt(7, clothingId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update clothing item.", e);
        }
    }

    // DELETE PRODUCT
    public boolean deleteClothingItem(int clothingId) {

        String sql = """
                DELETE FROM clothing_items
                WHERE clothing_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, clothingId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete clothing item.", e);
        }
    }
}
