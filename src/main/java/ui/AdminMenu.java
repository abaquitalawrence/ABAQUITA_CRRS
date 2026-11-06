
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import dao.ClothingItemDao;

import model.User;
import model.ActivityLog;
import model.ClothingItem;

import java.util.List;
import java.util.Scanner;
import java.util.Locale;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final ClothingItemDao clothingItemDao;
    private final User currentAdmin;

    public AdminMenu(User currentAdmin, Scanner scanner) {
        this.currentAdmin = currentAdmin;
        this.scanner = scanner;

        this.userDao = new UserDao();
        this.activityLogDao = new ActivityLogDao();
        this.clothingItemDao = new ClothingItemDao();
    }

    // ==============================
    // ADMIN MAIN MENU
    // ==============================

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("             ADMIN MENU");
            System.out.println("========================================");
            System.out.println("[1] VIEW ALL SYSTEM USERS");
            System.out.println("[2] VIEW PENDING REGISTRATIONS");
            System.out.println("[3] APPROVE USER ACCOUNT");
            System.out.println("[4] UPDATE USER ROLE AND STATUS");
            System.out.println("[5] VIEW SYSTEM AUDIT LOGS");
            System.out.println("[6] MANAGE PRODUCTS");
            System.out.println("[7] LOGOUT");
            System.out.println("========================================");

            int choice = readIntInput("Enter your choice: ");

            switch (choice) {

                case 1:
                    viewAllUsers();
                    break;

                case 2:
                    viewPendingUsers();
                    break;

                case 3:
                    approveUser();
                    break;

                case 4:
                    updateUserRoleAndStatus();
                    break;

                case 5:
                    viewSystemAuditLogs();
                    break;

                case 6:
                    manageProducts();
                    break;

                case 7:
                    activityLogDao.logAction(
                            currentAdmin.getId(),
                            "ADMIN LOGOUT",
                            "Administrator logged out."
                    );

                    System.out.println("\nYou have successfully logged out.");
                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    // ==============================
    // VIEW ALL SYSTEM USERS
    // ==============================

    private void viewAllUsers() {

        System.out.println("\n========================================");
        System.out.println("          ALL SYSTEM USERS");
        System.out.println("========================================");

        List<User> users = userDao.getAllUsers();

        displayUserTable(users);
    }

    // ==============================
    // VIEW PENDING REGISTRATIONS
    // ==============================

    private void viewPendingUsers() {

        System.out.println("\n========================================");
        System.out.println("        PENDING REGISTRATIONS");
        System.out.println("========================================");

        List<User> users = userDao.getPendingUsers();

        displayUserTable(users);
    }

    // ==============================
    // APPROVE USER ACCOUNT
    // ==============================

    private void approveUser() {

        List<User> pendingUsers = userDao.getPendingUsers();

        System.out.println("\n========================================");
        System.out.println("        APPROVE USER ACCOUNT");
        System.out.println("========================================");

        if (pendingUsers.isEmpty()) {
            System.out.println("No pending registrations found.");
            return;
        }

        displayUserTable(pendingUsers);

        int targetUserId = readIntInput("\nEnter User ID to approve (0 to cancel): ");

        if (targetUserId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean found = false;

        for (User user : pendingUsers) {
            if (user.getId() == targetUserId) {
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Invalid User ID or account is not pending.");
            return;
        }

        boolean approved = userDao.approveUser(
                targetUserId,
                currentAdmin.getId()
        );

        if (approved) {

            activityLogDao.logAction(
                    currentAdmin.getId(),
                    "APPROVE USER",
                    "Approved user account ID: " + targetUserId
            );

            System.out.println("User account successfully approved.");

        } else {
            System.out.println("Failed to approve user account.");
        }
    }

    // ==============================
    // UPDATE USER ROLE AND STATUS
    // ==============================

    private void updateUserRoleAndStatus() {

        System.out.println("\n========================================");
        System.out.println("      UPDATE USER ROLE AND STATUS");
        System.out.println("========================================");

        List<User> users = userDao.getAllUsers();

        displayUserTable(users);

        if (users.isEmpty()) {
            return;
        }

        int targetUserId = readIntInput("\nEnter User ID to update (0 to cancel): ");

        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        User selectedUser = null;

        for (User user : users) {
            if (user.getId() == targetUserId) {
                selectedUser = user;
                break;
            }
        }

        if (selectedUser == null) {
            System.out.println("User ID not found.");
            return;
        }

        System.out.println("\nSelected Username: " + selectedUser.getUsername());

        String newRole = readValidRole();
        String newStatus = readValidStatus();

        boolean updated = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus,
                currentAdmin.getId()
        );

        if (updated) {

            activityLogDao.logAction(
                    currentAdmin.getId(),
                    "UPDATE USER",
                    "Updated user ID " + targetUserId
                            + " to role " + newRole
                            + " and status " + newStatus
            );

            System.out.println("User role and status successfully updated.");

        } else {
            System.out.println("Failed to update user.");
        }
    }

    // ==============================
    // USER ROLE VALIDATION
    // ==============================

    private String readValidRole() {

        while (true) {

            System.out.println("\nAvailable Roles:");
            System.out.println("[1] ADMIN");
            System.out.println("[2] STAFF");
            System.out.println("[3] CUSTOMER");

            int choice = readIntInput("Select new role: ");

            switch (choice) {
                case 1:
                    return "ADMIN";

                case 2:
                    return "STAFF";

                case 3:
                    return "CUSTOMER";

                default:
                    System.out.println("Invalid role. Please try again.");
            }
        }
    }

    // ==============================
    // USER STATUS VALIDATION
    // ==============================

    private String readValidStatus() {

        while (true) {

            System.out.println("\nAvailable Account Status:");
            System.out.println("[1] APPROVED");
            System.out.println("[2] PENDING");
            System.out.println("[3] DISABLED");

            int choice = readIntInput("Select new status: ");

            switch (choice) {
                case 1:
                    return "APPROVED";

                case 2:
                    return "PENDING";

                case 3:
                    return "DISABLED";

                default:
                    System.out.println("Invalid status. Please try again.");
            }
        }
    }

    // ==============================
    // SYSTEM AUDIT LOGS
    // ==============================

    private void viewSystemAuditLogs() {

        boolean viewing = true;

        while (viewing) {

            System.out.println("\n========================================");
            System.out.println("          SYSTEM AUDIT LOGS");
            System.out.println("========================================");
            System.out.println("[1] VIEW ALL ACTIVITY LOGS");
            System.out.println("[2] FILTER LOGS BY USER ID");
            System.out.println("[3] BACK TO ADMIN MENU");
            System.out.println("========================================");

            int choice = readIntInput("Enter your choice: ");

            switch (choice) {

                case 1:
                    List<ActivityLog> allLogs = activityLogDao.getAllLogs();
                    displayActivityLogs(allLogs);
                    break;

                case 2:
                    filterLogsByUserId();
                    break;

                case 3:
                    viewing = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ==============================
    // FILTER LOGS BY USER ID
    // ==============================

    private void filterLogsByUserId() {

        int userId = readIntInput("\nEnter User ID: ");

        List<ActivityLog> logs = activityLogDao.getLogsByUserId(userId);

        displayActivityLogs(logs);
    }

    // ==============================
    // DISPLAY ACTIVITY LOGS
    // ==============================

    private void displayActivityLogs(List<ActivityLog> logs) {

        System.out.println("\n====================================================================================================");
        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "LOG ID",
                "USER ID",
                "ACTION",
                "DESCRIPTION",
                "TIMESTAMP"
        );
        System.out.println("====================================================================================================");

        if (logs == null || logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        for (ActivityLog log : logs) {

            System.out.printf(
                    "%-5d | %-8d | %-18s | %-35s | %-20s%n",
                    log.getLogId(),
                    log.getUserId(),
                    truncate(log.getActionType(), 18),
                    truncate(log.getDescription(), 35),
                    truncate(log.getTimestamp(), 20)
            );
        }

        System.out.println("====================================================================================================");
    }

    // ==============================
    // MANAGE PRODUCTS MENU
    // ==============================

    private void manageProducts() {

        boolean managing = true;

        while (managing) {

            System.out.println("\n========================================");
            System.out.println("            MANAGE PRODUCTS");
            System.out.println("========================================");
            System.out.println("[1] ADD PRODUCT");
            System.out.println("[2] VIEW PRODUCTS");
            System.out.println("[3] UPDATE PRODUCT");
            System.out.println("[4] DELETE PRODUCT");
            System.out.println("[5] BACK TO ADMIN MENU");
            System.out.println("========================================");

            int choice = readIntInput("Enter your choice: ");

            switch (choice) {

                case 1:
                    createProduct();
                    break;

                case 2:
                    viewProducts();
                    break;

                case 3:
                    updateProduct();
                    break;

                case 4:
                    deleteProduct();
                    break;

                case 5:
                    managing = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ==============================
    // ADD PRODUCT
    // ==============================

    private void createProduct() {

        System.out.println("\n========================================");
        System.out.println("              ADD PRODUCT");
        System.out.println("========================================");

        String name = readNonBlank("Enter clothing name: ");
        String category = readNonBlank("Enter category: ");
        String size = readNonBlank("Enter size: ");
        String color = readNonBlank("Enter color: ");
        double rentalPrice = readRentalPrice();
        String availability = readAvailability();

        boolean added = clothingItemDao.addClothingItem(
                name,
                category,
                size,
                color,
                rentalPrice,
                availability
        );

        if (added) {

            activityLogDao.logAction(
                    currentAdmin.getId(),
                    "CREATE PRODUCT",
                    "Added clothing product: " + name
            );

            System.out.println("\nProduct successfully added.");

        } else {
            System.out.println("\nFailed to add product.");
        }
    }

    // ==============================
    // VIEW PRODUCTS
    // ==============================

    private void viewProducts() {

        System.out.println("\n========================================");
        System.out.println("             CLOTHING PRODUCTS");
        System.out.println("========================================");

        List<ClothingItem> products = clothingItemDao.getAllClothingItems();

        if (products == null || products.isEmpty()) {
            System.out.println("No clothing products found.");
            return;
        }

        System.out.println("================================================================================================");
        System.out.printf(
                "%-5s | %-20s | %-15s | %-8s | %-12s | %-12s | %-15s%n",
                "ID",
                "CLOTHING NAME",
                "CATEGORY",
                "SIZE",
                "COLOR",
                "PRICE",
                "AVAILABILITY"
        );
        System.out.println("================================================================================================");

        for (ClothingItem item : products) {

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

    // ==============================
    // UPDATE PRODUCT
    // ==============================

    private void updateProduct() {

        System.out.println("\n========================================");
        System.out.println("             UPDATE PRODUCT");
        System.out.println("========================================");

        viewProducts();

        int productId = readIntInput("\nEnter Product ID to update (0 to cancel): ");

        if (productId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        ClothingItem existingProduct =
                clothingItemDao.getClothingItemById(productId);

        if (existingProduct == null) {
            System.out.println("Product ID not found.");
            return;
        }

        System.out.println("\nEnter the new product information.");

        String name = readNonBlank("Enter new clothing name: ");
        String category = readNonBlank("Enter new category: ");
        String size = readNonBlank("Enter new size: ");
        String color = readNonBlank("Enter new color: ");
        double rentalPrice = readRentalPrice();
        String availability = readAvailability();

        boolean updated = clothingItemDao.updateClothingItem(
                productId,
                name,
                category,
                size,
                color,
                rentalPrice,
                availability
        );

        if (updated) {

            activityLogDao.logAction(
                    currentAdmin.getId(),
                    "UPDATE PRODUCT",
                    "Updated clothing product ID: " + productId
            );

            System.out.println("\nProduct successfully updated.");

        } else {
            System.out.println("\nFailed to update product.");
        }
    }

    // ==============================
    // DELETE PRODUCT
    // ==============================

    private void deleteProduct() {

        System.out.println("\n========================================");
        System.out.println("             DELETE PRODUCT");
        System.out.println("========================================");

        viewProducts();

        int productId = readIntInput("\nEnter Product ID to delete (0 to cancel): ");

        if (productId == 0) {
            System.out.println("Deletion cancelled.");
            return;
        }

        ClothingItem existingProduct =
                clothingItemDao.getClothingItemById(productId);

        if (existingProduct == null) {
            System.out.println("Product ID not found.");
            return;
        }

        System.out.println(
                "\nProduct selected: " + existingProduct.getClothingName()
        );

        System.out.print("Are you sure you want to delete this product? (Y/N): ");

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = clothingItemDao.deleteClothingItem(productId);

        if (deleted) {

            activityLogDao.logAction(
                    currentAdmin.getId(),
                    "DELETE PRODUCT",
                    "Deleted clothing product ID: " + productId
            );

            System.out.println("\nProduct successfully deleted.");

        } else {
            System.out.println(
                    "\nFailed to delete product. It may be connected to another record."
            );
        }
    }

    // ==============================
    // INPUT VALIDATION
    // ==============================

    private String readNonBlank(String message) {

        while (true) {

            System.out.print(message);

            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    private double readRentalPrice() {

        while (true) {

            System.out.print("Enter rental price: ");

            try {

                double price = Double.parseDouble(
                        scanner.nextLine().trim()
                );

                if (Double.isFinite(price) && price > 0) {
                    return price;
                }

                System.out.println("Price must be greater than zero.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid price. Please enter a valid number.");
            }
        }
    }

    private String readAvailability() {

        while (true) {

            System.out.println("\nProduct Availability:");
            System.out.println("[1] AVAILABLE");
            System.out.println("[2] UNAVAILABLE");

            int choice = readIntInput("Select availability: ");

            switch (choice) {

                case 1:
                    return "AVAILABLE";

                case 2:
                    return "UNAVAILABLE";

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ==============================
    // DISPLAY USER TABLE
    // ==============================

    private void displayUserTable(List<User> users) {

        if (users == null || users.isEmpty()) {
            System.out.println("No user records found.");
            return;
        }

        System.out.println("==============================================================");
        System.out.printf(
                "%-8s | %-20s | %-12s | %-12s%n",
                "USER ID",
                "USERNAME",
                "ROLE",
                "STATUS"
        );
        System.out.println("==============================================================");

        for (User user : users) {

            System.out.printf(
                    "%-8d | %-20s | %-12s | %-12s%n",
                    user.getId(),
                    truncate(user.getUsername(), 20),
                    truncate(user.getRole(), 12),
                    truncate(user.getStatus(), 12)
            );
        }

        System.out.println("==============================================================");
    }

    // ==============================
    // INTEGER INPUT VALIDATION
    // ==============================

    private int readIntInput(String message) {

        while (true) {

            System.out.print(message);

            try {

                return Integer.parseInt(scanner.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    // ==============================
    // TEXT LENGTH CONTROL
    // ==============================

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
