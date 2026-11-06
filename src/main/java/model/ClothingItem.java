
package model;

public class ClothingItem {

    private final int clothingId;
    private final String clothingName;
    private final String category;
    private final String size;
    private final String color;
    private final double rentalPrice;
    private final String availability;

    public ClothingItem(
            int clothingId,
            String clothingName,
            String category,
            String size,
            String color,
            double rentalPrice,
            String availability) {

        this.clothingId = clothingId;
        this.clothingName = clothingName;
        this.category = category;
        this.size = size;
        this.color = color;
        this.rentalPrice = rentalPrice;
        this.availability = availability;
    }

    public int getClothingId() {
        return clothingId;
    }

    public String getClothingName() {
        return clothingName;
    }

    public String getCategory() {
        return category;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public String getAvailability() {
        return availability;
    }
}
