
package ui;

import dao.ActivityLogDao;
import dao.ClothingItemDao;
import dao.ReservationDao;
import dao.RentalDao;

import model.User;
import model.ClothingItem;
import model.Reservation;
import model.Rental;

import java.util.List;
import java.util.Scanner;

public class UserMenu {

    private final User user;
    private final Scanner scanner;

    private final ActivityLogDao activityLogDao;
    private final ClothingItemDao clothingItemDao;
    private final ReservationDao reservationDao;
    private final RentalDao rentalDao;

    public UserMenu(User user, Scanner scanner) {

        this.user = user;
        this.scanner = scanner;

        this.activityLogDao = new ActivityLogDao();
        this.clothingItemDao = new ClothingItemDao();
        this.reservationDao = new ReservationDao();
        this.rentalDao = new RentalDao();
    }

    // ==========================================
    // MAIN MENU
    // ==========================================

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("       CLOTHING RENTAL SYSTEM");
            System.out.println("========================================");
            System.out.println("Welcome, " + user.getUsername());
            System.out.println("Role: " + user.getRole());

            if ("STAFF".equalsIgnoreCase(user.getRole())) {

                System.out.println("\n============= STAFF MENU =============");
                System.out.println("[1] VIEW CLOTHING");
                System.out.println("[2] MANAGE RENTALS");
                System.out.println("[3] MANAGE RESERVATION");
                System.out.println("[4] LOGOUT");

            } else if ("CUSTOMER".equalsIgnoreCase(user.getRole())) {

                System.out.println("\n=========== CUSTOMER MENU ============");
                System.out.println("[1] BROWSE CLOTHING");
                System.out.println("[2] RESERVE CLOTHING");
                System.out.println("[3] RENTAL CLOTHING");
                System.out.println("[4] VIEW MY RESERVATION");
                System.out.println("[5] VIEW MY RENTAL");
                System.out.println("[6] LOGOUT");

            } else {

                System.out.println("Invalid user role.");
                return;
            }

            int choice = readInt("\nEnter your choice: ");

            // STAFF MENU
            if ("STAFF".equalsIgnoreCase(user.getRole())) {

                if (!handleStaffChoice(choice)) {
                    running = false;
                }

            }

            // CUSTOMER MENU
            else {

                if (!handleCustomerChoice(choice)) {
                    running = false;
                }
            }
        }

        System.out.println("\nReturning to the authentication menu...");
    }

    // ==========================================
    // STAFF MENU OPTIONS
    // Return false when STAFF logs out.
    // ==========================================

    private boolean handleStaffChoice(int choice) {

        switch (choice) {

            case 1:
                viewClothing();
                return true;

            case 2:
                manageRentals();
                return true;

            case 3:
                manageReservations();
                return true;

            case 4:
                logout();
                return false;

            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }

    // ==========================================
    // CUSTOMER MENU OPTIONS
    // Return false when CUSTOMER logs out.
    // ==========================================

    private boolean handleCustomerChoice(int choice) {

        switch (choice) {

            case 1:
                browseClothing();
                return true;

            case 2:
                reserveClothing();
                return true;

            case 3:
                rentClothing();
                return true;

            case 4:
                viewMyReservations();
                return true;

            case 5:
                viewMyRentals();
                return true;

            case 6:
                logout();
                return false;

            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }

    // ==========================================
    // VIEW CLOTHING - STAFF
    // ==========================================

    private void viewClothing() {

        System.out.println("\n========== CLOTHING INVENTORY ==========");

        List<ClothingItem> items = clothingItemDao.getAllClothingItems();

        displayClothingTable(items);
    }

    // ==========================================
    // BROWSE CLOTHING - CUSTOMER
    // ==========================================

    private void browseClothing() {

        System.out.println("\n========== AVAILABLE CLOTHING ==========");

        List<ClothingItem> items = clothingItemDao.getAllClothingItems();

        if (items == null || items.isEmpty()) {
            System.out.println("No clothing products found.");
            return;
        }

        boolean found = false;

        System.out.println("================================================================================");
        System.out.printf(
                "%-5s | %-20s | %-15s | %-8s | %-12s | %-12s%n",
                "ID", "CLOTHING NAME", "CATEGORY",
                "SIZE", "COLOR", "PRICE"
        );
        System.out.println("================================================================================");

        for (ClothingItem item : items) {

            if ("AVAILABLE".equalsIgnoreCase(item.getAvailability())) {

                found = true;

                System.out.printf(
                        "%-5d | %-20s | %-15s | %-8s | %-12s | %-12.2f%n",
                        item.getClothingId(),
                        truncate(item.getClothingName(), 20),
                        truncate(item.getCategory(), 15),
                        truncate(item.getSize(), 8),
                        truncate(item.getColor(), 12),
                        item.getRentalPrice()
                );
            }
        }

        System.out.println("================================================================================");

        if (!found) {
            System.out.println("There are currently no available clothing products.");
        }
    }

    // ==========================================
    // RESERVE CLOTHING - CUSTOMER
    // ==========================================

    private void reserveClothing() {

        System.out.println("\n========== RESERVE CLOTHING ==========");

        browseClothing();

        int clothingId = readInt("\nEnter Clothing ID to reserve (0 to cancel): ");

        if (clothingId == 0) {
            System.out.println("Reservation cancelled.");
            return;
        }

        ClothingItem item = clothingItemDao.getClothingItemById(clothingId);

        if (item == null) {
            System.out.println("Clothing product not found.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(item.getAvailability())) {
            System.out.println("This clothing product is not available.");
            return;
        }

        System.out.println("\nSelected Product: " + item.getClothingName());
        System.out.println("Rental Price: " + item.getRentalPrice());

        if (!confirm("Confirm reservation? (Y/N): ")) {
            System.out.println("Reservation cancelled.");
            return;
        }

        boolean reserved = reservationDao.createReservation(
                user.getId(),
                clothingId
        );

        if (reserved) {

            activityLogDao.logAction(
                    user.getId(),
                    "CREATE RESERVATION",
                    "Reserved clothing: " + item.getClothingName()
            );

            System.out.println("\nReservation submitted successfully.");
            System.out.println("Your reservation is now PENDING staff approval.");

        } else {
            System.out.println("Reservation failed. The product may no longer be available.");
        }
    }

    // ==========================================
    // RENT CLOTHING - CUSTOMER
    // ==========================================

    private void rentClothing() {

        System.out.println("\n========== RENTAL CLOTHING ==========");

        browseClothing();

        int clothingId = readInt("\nEnter Clothing ID to rent (0 to cancel): ");

        if (clothingId == 0) {
            System.out.println("Rental cancelled.");
            return;
        }

        ClothingItem item = clothingItemDao.getClothingItemById(clothingId);

        if (item == null) {
            System.out.println("Clothing product not found.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(item.getAvailability())) {
            System.out.println("This clothing product is not available.");
            return;
        }

        System.out.println("\nSelected Product: " + item.getClothingName());
        System.out.println("Rental Price: " + item.getRentalPrice());

        if (!confirm("Confirm rental? (Y/N): ")) {
            System.out.println("Rental cancelled.");
            return;
        }

        boolean rented = rentalDao.createRental(
                user.getId(),
                clothingId
        );

        if (rented) {

            activityLogDao.logAction(
                    user.getId(),
                    "CREATE RENTAL",
                    "Rented clothing: " + item.getClothingName()
            );

            System.out.println("\nRental record successfully created.");
            System.out.println("Your clothing is now marked as UNAVAILABLE.");

        } else {
            System.out.println("Rental failed. The product may no longer be available.");
        }
    }

    // ==========================================
    // VIEW MY RESERVATIONS - CUSTOMER
    // ==========================================

    private void viewMyReservations() {

        System.out.println("\n========== MY RESERVATIONS ==========");

        List<Reservation> reservations =
                reservationDao.getReservationsByCustomer(user.getId());

        displayReservationTable(reservations);
    }

    // ==========================================
    // VIEW MY RENTALS - CUSTOMER
    // ==========================================

    private void viewMyRentals() {

        System.out.println("\n========== MY RENTALS ==========");

        List<Rental> rentals =
                rentalDao.getRentalsByCustomer(user.getId());

        displayRentalTable(rentals);
    }

    // ==========================================
    // STAFF - MANAGE RENTALS
    // ==========================================

    private void manageRentals() {

        boolean managing = true;

        while (managing) {

            System.out.println("\n========== MANAGE RENTALS ==========");
            System.out.println("[1] CREATE RENTAL");
            System.out.println("[2] VIEW ALL RENTALS");
            System.out.println("[3] UPDATE RENTAL STATUS");
            System.out.println("[4] DELETE RENTAL");
            System.out.println("[5] BACK TO STAFF MENU");

            int choice = readInt("Enter your choice: ");

            switch (choice) {

                case 1:
                    staffCreateRental();
                    break;

                case 2:
                    viewAllRentals();
                    break;

                case 3:
                    updateRental();
                    break;

                case 4:
                    deleteRental();
                    break;

                case 5:
                    managing = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ==========================================
    // STAFF - CREATE RENTAL
    // ==========================================

    private void staffCreateRental() {

        System.out.println("\n========== CREATE RENTAL ==========");

        viewClothing();

        int customerId = readInt("\nEnter Customer User ID: ");

        if (customerId <= 0) {
            System.out.println("Invalid Customer ID.");
            return;
        }

        int clothingId = readInt("Enter Clothing ID: ");

        ClothingItem item = clothingItemDao.getClothingItemById(clothingId);

        if (item == null) {
            System.out.println("Clothing product not found.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(item.getAvailability())) {
            System.out.println("This product is not available.");
            return;
        }

        boolean created = rentalDao.createRental(customerId, clothingId);

        if (created) {

            activityLogDao.logAction(
                    user.getId(),
                    "STAFF CREATE RENTAL",
                    "Created rental for customer ID: " + customerId
                            + ", clothing ID: " + clothingId
            );

            System.out.println("Rental successfully created.");

        } else {
            System.out.println("Failed to create rental. Check the customer ID and product availability.");
        }
    }

    // ==========================================
    // STAFF - VIEW ALL RENTALS
    // ==========================================

    private void viewAllRentals() {

        System.out.println("\n========== ALL RENTALS ==========");

        List<Rental> rentals = rentalDao.getAllRentals();

        displayRentalTable(rentals);
    }

    // ==========================================
    // STAFF - UPDATE RENTAL
    // ==========================================

    private void updateRental() {

        System.out.println("\n========== UPDATE RENTAL STATUS ==========");

        viewAllRentals();

        int rentalId = readInt("\nEnter Rental ID to update (0 to cancel): ");

        if (rentalId == 0) {
            return;
        }

        Rental rental = rentalDao.getRentalById(rentalId);

        if (rental == null) {
            System.out.println("Rental record not found.");
            return;
        }

        System.out.println("\n[1] ACTIVE");
        System.out.println("[2] RETURNED");
        System.out.println("[3] CANCELLED");

        int choice = readInt("Select new rental status: ");

        String status;

        switch (choice) {
            case 1:
                status = "ACTIVE";
                break;
            case 2:
                status = "RETURNED";
                break;
            case 3:
                status = "CANCELLED";
                break;
            default:
                System.out.println("Invalid status.");
                return;
        }

        boolean updated = rentalDao.updateRentalStatus(rentalId, status);

        if (updated) {

            activityLogDao.logAction(
                    user.getId(),
                    "UPDATE RENTAL",
                    "Updated rental ID " + rentalId + " to " + status
            );

            System.out.println("Rental status successfully updated.");

        } else {
            System.out.println("Failed to update rental.");
        }
    }

    // ==========================================
    // STAFF - DELETE RENTAL
    // ==========================================

    private void deleteRental() {

        System.out.println("\n========== DELETE RENTAL ==========");

        viewAllRentals();

        int rentalId = readInt("\nEnter Rental ID to delete (0 to cancel): ");

        if (rentalId == 0) {
            return;
        }

        Rental rental = rentalDao.getRentalById(rentalId);

        if (rental == null) {
            System.out.println("Rental record not found.");
            return;
        }

        if (!confirm("Are you sure you want to delete this rental? (Y/N): ")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = rentalDao.deleteRental(rentalId);

        if (deleted) {

            activityLogDao.logAction(
                    user.getId(),
                    "DELETE RENTAL",
                    "Deleted rental ID: " + rentalId
            );

            System.out.println("Rental successfully deleted.");

        } else {
            System.out.println("Failed to delete rental.");
        }
    }

    // ==========================================
    // STAFF - MANAGE RESERVATIONS
    // ==========================================

    private void manageReservations() {

        boolean managing = true;

        while (managing) {

            System.out.println("\n========== MANAGE RESERVATIONS ==========");
            System.out.println("[1] CREATE RESERVATION");
            System.out.println("[2] VIEW ALL RESERVATIONS");
            System.out.println("[3] UPDATE RESERVATION STATUS");
            System.out.println("[4] DELETE RESERVATION");
            System.out.println("[5] BACK TO STAFF MENU");

            int choice = readInt("Enter your choice: ");

            switch (choice) {

                case 1:
                    staffCreateReservation();
                    break;

                case 2:
                    viewAllReservations();
                    break;

                case 3:
                    updateReservation();
                    break;

                case 4:
                    deleteReservation();
                    break;

                case 5:
                    managing = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ==========================================
    // STAFF - CREATE RESERVATION
    // ==========================================

    private void staffCreateReservation() {

        System.out.println("\n========== CREATE RESERVATION ==========");

        viewClothing();

        int customerId = readInt("\nEnter Customer User ID: ");

        if (customerId <= 0) {
            System.out.println("Invalid Customer ID.");
            return;
        }

        int clothingId = readInt("Enter Clothing ID: ");

        ClothingItem item = clothingItemDao.getClothingItemById(clothingId);

        if (item == null) {
            System.out.println("Clothing product not found.");
            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(item.getAvailability())) {
            System.out.println("This product is not available.");
            return;
        }

        boolean created = reservationDao.createReservation(customerId, clothingId);

        if (created) {

            activityLogDao.logAction(
                    user.getId(),
                    "STAFF CREATE RESERVATION",
                    "Created reservation for customer ID: " + customerId
                            + ", clothing ID: " + clothingId
            );

            System.out.println("Reservation successfully created.");

        } else {
            System.out.println("Failed to create reservation. Check the customer ID and product availability.");
        }
    }

    // ==========================================
    // STAFF - VIEW ALL RESERVATIONS
    // ==========================================

    private void viewAllReservations() {

        System.out.println("\n========== ALL RESERVATIONS ==========");

        List<Reservation> reservations = reservationDao.getAllReservations();

        displayReservationTable(reservations);
    }

    // ==========================================
    // STAFF - UPDATE RESERVATION
    // ==========================================

    private void updateReservation() {

        System.out.println("\n========== UPDATE RESERVATION STATUS ==========");

        viewAllReservations();

        int reservationId =
                readInt("\nEnter Reservation ID to update (0 to cancel): ");

        if (reservationId == 0) {
            return;
        }

        Reservation reservation =
                reservationDao.getReservationById(reservationId);

        if (reservation == null) {
            System.out.println("Reservation record not found.");
            return;
        }

        System.out.println("\n[1] APPROVED");
        System.out.println("[2] REJECTED");
        System.out.println("[3] CANCELLED");
        System.out.println("[4] COMPLETED");
        System.out.println("[5] PENDING");

        int choice = readInt("Select new reservation status: ");

        String status;

        switch (choice) {
            case 1:
                status = "APPROVED";
                break;
            case 2:
                status = "REJECTED";
                break;
            case 3:
                status = "CANCELLED";
                break;
            case 4:
                status = "COMPLETED";
                break;
            case 5:
                status = "PENDING";
                break;
            default:
                System.out.println("Invalid status.");
                return;
        }

        boolean updated =
                reservationDao.updateReservationStatus(reservationId, status);

        if (updated) {

            activityLogDao.logAction(
                    user.getId(),
                    "UPDATE RESERVATION",
                    "Updated reservation ID " + reservationId + " to " + status
            );

            System.out.println("Reservation status successfully updated.");

        } else {
            System.out.println("Failed to update reservation.");
        }
    }

    // ==========================================
    // STAFF - DELETE RESERVATION
    // ==========================================

    private void deleteReservation() {

        System.out.println("\n========== DELETE RESERVATION ==========");

        viewAllReservations();

        int reservationId =
                readInt("\nEnter Reservation ID to delete (0 to cancel): ");

        if (reservationId == 0) {
            return;
        }

        Reservation reservation =
                reservationDao.getReservationById(reservationId);

        if (reservation == null) {
            System.out.println("Reservation record not found.");
            return;
        }

        if (!confirm("Are you sure you want to delete this reservation? (Y/N): ")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = reservationDao.deleteReservation(reservationId);

        if (deleted) {

            activityLogDao.logAction(
                    user.getId(),
                    "DELETE RESERVATION",
                    "Deleted reservation ID: " + reservationId
            );

            System.out.println("Reservation successfully deleted.");

        } else {
            System.out.println("Failed to delete reservation.");
        }
    }

    // ==========================================
    // DISPLAY CLOTHING TABLE
    // ==========================================

    private void displayClothingTable(List<ClothingItem> items) {

        if (items == null || items.isEmpty()) {
            System.out.println("No clothing products found.");
            return;
        }

        System.out.println("================================================================================================");
        System.out.printf(
                "%-5s | %-20s | %-15s | %-8s | %-12s | %-12s | %-15s%n",
                "ID", "CLOTHING NAME", "CATEGORY",
                "SIZE", "COLOR", "PRICE", "AVAILABILITY"
        );
        System.out.println("================================================================================================");

        for (ClothingItem item : items) {

            System.out.printf(
                    "%-5d | %-20s | %-15s | %-8s | %-12s | %-12.2f | %-15s%n",
                    item.getClothingId(),
                    truncate(item.getClothingName(), 20),
                    truncate(item.getCategory(), 15),
                    truncate(item.getSize(), 8),
                    truncate(item.getColor(), 12),
                    item.getRentalPrice(),
                    truncate(item.getAvailability(), 15)
            );
        }

        System.out.println("================================================================================================");
    }

    // ==========================================
    // DISPLAY RESERVATION TABLE
    // ==========================================

    private void displayReservationTable(List<Reservation> reservations) {

        if (reservations == null || reservations.isEmpty()) {
            System.out.println("No reservation records found.");
            return;
        }

        System.out.println("================================================================================================");
        System.out.printf(
                "%-6s | %-10s | %-18s | %-10s | %-20s | %-20s | %-12s%n",
                "ID", "CUSTOMER ID", "CUSTOMER",
                "CLOTHING ID", "CLOTHING", "DATE", "STATUS"
        );
        System.out.println("================================================================================================");

        for (Reservation reservation : reservations) {

            System.out.printf(
                    "%-6d | %-10d | %-18s | %-10d | %-20s | %-20s | %-12s%n",
                    reservation.getReservationId(),
                    reservation.getCustomerId(),
                    truncate(reservation.getCustomerName(), 18),
                    reservation.getClothingId(),
                    truncate(reservation.getClothingName(), 20),
                    truncate(reservation.getReservationDate(), 20),
                    truncate(reservation.getStatus(), 12)
            );
        }

        System.out.println("================================================================================================");
    }

    // ==========================================
    // DISPLAY RENTAL TABLE
    // ==========================================

    private void displayRentalTable(List<Rental> rentals) {

        if (rentals == null || rentals.isEmpty()) {
            System.out.println("No rental records found.");
            return;
        }

        System.out.println("================================================================================================");
        System.out.printf(
                "%-6s | %-10s | %-18s | %-10s | %-20s | %-20s | %-12s%n",
                "ID", "CUSTOMER ID", "CUSTOMER",
                "CLOTHING ID", "CLOTHING", "DATE", "STATUS"
        );
        System.out.println("================================================================================================");

        for (Rental rental : rentals) {

            System.out.printf(
                    "%-6d | %-10d | %-18s | %-10d | %-20s | %-20s | %-12s%n",
                    rental.getRentalId(),
                    rental.getCustomerId(),
                    truncate(rental.getCustomerName(), 18),
                    rental.getClothingId(),
                    truncate(rental.getClothingName(), 20),
                    truncate(rental.getRentalDate(), 20),
                    truncate(rental.getStatus(), 12)
            );
        }

        System.out.println("================================================================================================");
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    private void logout() {

        activityLogDao.logAction(
                user.getId(),
                "LOGOUT",
                user.getRole() + " user " + user.getUsername() + " logged out."
        );

        System.out.println("\n========================================");
        System.out.println("          LOGOUT SUCCESSFUL");
        System.out.println("========================================");
        System.out.println("Goodbye, " + user.getUsername() + "!");
    }

    // ==========================================
    // INPUT VALIDATION
    // ==========================================

    private int readInt(String message) {

        while (true) {

            System.out.print(message);

            try {
                return Integer.parseInt(scanner.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    private boolean confirm(String message) {

        System.out.print(message);

        String answer = scanner.nextLine().trim();

        return answer.equalsIgnoreCase("Y");
    }

    private String truncate(String value, int maxLength) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }
}
