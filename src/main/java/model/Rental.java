package model;

public class Rental {

    private final int rentalId;
    private final int customerId;
    private final String customerName;
    private final int clothingId;
    private final String clothingName;
    private final String rentalDate;
    private final String status;

    public Rental(
            int rentalId,
            int customerId,
            String customerName,
            int clothingId,
            String clothingName,
            String rentalDate,
            String status) {

        this.rentalId = rentalId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.clothingId = clothingId;
        this.clothingName = clothingName;
        this.rentalDate = rentalDate;
        this.status = status;
    }

    public int getRentalId() {
        return rentalId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getClothingId() {
        return clothingId;
    }

    public String getClothingName() {
        return clothingName;
    }

    public String getRentalDate() {
        return rentalDate;
    }

    public String getStatus() {
        return status;
    }
}
