
package model;

public class Reservation {

    private final int reservationId;
    private final int customerId;
    private final String customerName;
    private final int clothingId;
    private final String clothingName;
    private final String reservationDate;
    private final String status;

    public Reservation(
            int reservationId,
            int customerId,
            String customerName,
            int clothingId,
            String clothingName,
            String reservationDate,
            String status) {

        this.reservationId = reservationId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.clothingId = clothingId;
        this.clothingName = clothingName;
        this.reservationDate = reservationDate;
        this.status = status;
    }

    public int getReservationId() {
        return reservationId;
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

    public String getReservationDate() {
        return reservationDate;
    }

    public String getStatus() {
        return status;
    }
}
