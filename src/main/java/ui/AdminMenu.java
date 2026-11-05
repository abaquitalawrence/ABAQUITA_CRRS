package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import model.User;
import model.ActivityLog;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final User currentAdmin;

    public AdminMenu(User currentAdmin, Scanner scanner) {

        this.currentAdmin = currentAdmin;
        this.scanner = scanner;
        this.userDao = new UserDao();
        this.activityLogDao = new ActivityLogDao();
    }

    // MAIN ADMIN MENU
    public void start() {

        while (true) {

            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewAllUsers();

                case "2" -> viewPendingUsers();

                case "3" -> approveUser();

                case "4" -> updateUserRoleAndStatus();

                case "5" -> viewSystemAuditLogs();

                case "6" -> {

                    activityLogDao.logAction(
                            currentAdmin.getId(),
                            "LOGOUT",
                            "ADMIN user "
                                    + currentAdmin.getUsername()
                                    + " logged out."
                    );

                    System.out.println("Logout successful!");
                    return;
                }

                default -> System.out.println(
                        "Invalid choice. Please try again.");
            }
        }
    }

    // VIEW ALL USERS
    private void viewAllUsers() {

        System.out.println("\n--- ALL SYSTEM USERS ---");

        List<User> users = userDao.getAllUsers();

        displayUserTable(users);
    }

    // VIEW PENDING USERS
    private void viewPendingUsers() {

        System.out.println("\n--- PENDING REGISTRATIONS ---");

        List<User> users = userDao.getPendingUsers();

        displayUserTable(users);
    }

    // APPROVE USER ACCOUNT
    private void approveUser() {

        System.out.println("\n--- APPROVE USER ACCOUNT ---");

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {

            System.out.println("No pending registrations found.");
            return;
        }

        displayUserTable(pendingUsers);

        int userId = readIntInput(
                "Enter Target User ID (0 to cancel): ");

        if (userId == 0) {

            System.out.println("Approval cancelled.");
            return;
        }

        boolean exists = pendingUsers.stream()
                .anyMatch(user -> user.getId() == userId);

        if (!exists) {

            System.out.println(
                    "User ID not found in pending registrations.");

            return;
        }

        if (userDao.approveUser(
                userId,
                currentAdmin.getId())) {

            System.out.println(
                    "User account approved successfully!");

        } else {

            System.out.println(
                    "Approval failed. Account may no longer be pending.");
        }
    }

    // UPDATE USER ROLE AND STATUS
    private void updateUserRoleAndStatus() {

        System.out.println("\n--- UPDATE USER ROLE AND STATUS ---");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {

            System.out.println("No users found.");
            return;
        }

        displayUserTable(users);

        int targetUserId = readIntInput(
                "Enter Target User ID (0 to cancel): ");

        if (targetUserId == 0) {

            System.out.println("Update cancelled.");
            return;
        }

        // SELF-EDIT RESTRICTION
        if (targetUserId == currentAdmin.getId()) {

            System.out.println(
                    "WARNING: Modifying the currently active admin account is restricted.");

            return;
        }

        boolean userExists = users.stream()
                .anyMatch(user -> user.getId() == targetUserId);

        if (!userExists) {

            System.out.println("User ID not found.");
            return;
        }

        String newRole = readValidRole();

        if (newRole == null) {

            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if (newStatus == null) {

            System.out.println("Update cancelled.");
            return;
        }

        boolean updated = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus,
                currentAdmin.getId()
        );

        if (updated) {

            System.out.println(
                    "\nUser role and account status updated successfully!");

            System.out.println("Updated User ID: " + targetUserId);
            System.out.println("New Role: " + newRole);
            System.out.println("New Status: " + newStatus);

        } else {

            System.out.println(
                    "Update failed. Please check the user record.");
        }
    }

    // SYSTEM AUDIT LOG MENU
    private void viewSystemAuditLogs() {

        while (true) {

            System.out.println("\n=== SYSTEM AUDIT LOGS ===");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Menu");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> {

                    System.out.println(
                            "\n--- ALL SYSTEM ACTIVITY LOGS ---");

                    List<ActivityLog> logs =
                            activityLogDao.getAllLogs();

                    displayActivityLogs(logs);
                }

                case "2" -> filterLogsByUserId();

                case "3" -> {
                    return;
                }

                default -> System.out.println(
                        "Invalid choice. Please try again.");
            }
        }
    }

    // FILTER LOGS BY USER ID
    private void filterLogsByUserId() {

        System.out.println("\n--- FILTER LOGS BY USER ID ---");

        int userId = readIntInput(
                "Enter User ID (0 to cancel): ");

        if (userId == 0) {

            System.out.println("Log filtering cancelled.");
            return;
        }

        List<ActivityLog> logs =
                activityLogDao.getLogsByUserId(userId);

        System.out.println(
                "\n--- ACTIVITY LOGS FOR USER ID "
                        + userId + " ---");

        displayActivityLogs(logs);
    }

    // DISPLAY ACTIVITY LOGS IN ALIGNED TABLE FORMAT
    private void displayActivityLogs(List<ActivityLog> logs) {

        if (logs.isEmpty()) {

            System.out.println("No activity logs found.");
            return;
        }

        String border = "-".repeat(104);

        System.out.println(border);

        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "LOG ID",
                "USER ID",
                "ACTION TYPE",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(border);

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

        System.out.println(border);
        System.out.println("Total Records: " + logs.size());
    }

    // PREVENT LONG TEXT FROM BREAKING TABLE ALIGNMENT
    private String truncate(String value, int maxLength) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }

    // READ INTEGER INPUT
    private int readIntInput(String prompt) {

        while (true) {

            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            try {

                int number = Integer.parseInt(input);

                if (number < 0) {

                    System.out.println(
                            "Invalid input. Enter 0 or a positive number.");

                    continue;
                }

                return number;

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input. Please enter numbers only.");
            }
        }
    }

    // VALIDATE USER ROLE
    private String readValidRole() {

        while (true) {

            System.out.println("\n--- SELECT NEW USER ROLE ---");
            System.out.println("ADMIN");
            System.out.println("STAFF");
            System.out.println("CUSTOMER");
            System.out.println("Enter 0 to cancel.");
            System.out.print("Enter New Role: ");

            String role = scanner.nextLine()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if ("0".equals(role)) {
                return null;
            }

            if (role.equals("ADMIN")
                    || role.equals("STAFF")
                    || role.equals("CUSTOMER")) {

                return role;
            }

            System.out.println(
                    "Invalid role. Enter ADMIN, STAFF, or CUSTOMER.");
        }
    }

    // VALIDATE ACCOUNT STATUS
    private String readValidStatus() {

        while (true) {

            System.out.println("\n--- SELECT NEW ACCOUNT STATUS ---");
            System.out.println("APPROVED");
            System.out.println("PENDING");
            System.out.println("Enter 0 to cancel.");
            System.out.print("Enter New Status: ");

            String status = scanner.nextLine()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if ("0".equals(status)) {
                return null;
            }

            if (status.equals("APPROVED")
                    || status.equals("PENDING")) {

                return status;
            }

            System.out.println(
                    "Invalid status. Enter APPROVED or PENDING.");
        }
    }

    // DISPLAY USERS IN TABLE FORMAT
    private void displayUserTable(List<User> users) {

        if (users.isEmpty()) {

            System.out.println("No users found.");
            return;
        }

        String border = "-".repeat(52);

        System.out.println(border);

        System.out.printf(
                "%-5s %-15s %-12s %-10s%n",
                "ID", "USERNAME", "ROLE", "STATUS"
        );

        System.out.println(border);

        for (User user : users) {

            System.out.printf(
                    "%-5d %-15s %-12s %-10s%n",
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getStatus()
            );
        }

        System.out.println(border);
    }
}