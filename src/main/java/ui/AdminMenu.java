
package ui;

import dao.UserDao;
import model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;

    public AdminMenu(Scanner scanner) {
        this.scanner = scanner;
        this.userDao = new UserDao();
    }

    public void start() {

        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> {
                    System.out.println("Logout successful!");
                    return;
                }
                default -> System.out.println(
                        "Invalid choice. Please try again.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");

        List<User> users = userDao.getAllUsers();
        displayUserTable(users);
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");

        List<User> users = userDao.getPendingUsers();
        displayUserTable(users);
    }

    private void approveUser() {

        System.out.println("\n--- APPROVE USER ACCOUNT ---");

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("No pending registrations found.");
            return;
        }

        displayUserTable(pendingUsers);

        System.out.print("Enter Target User ID (0 to cancel): ");
        String input = scanner.nextLine().trim();

        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Enter a numeric ID.");
            return;
        }

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

        if (userDao.approveUser(userId)) {
            System.out.println(
                    "User account approved successfully!");
        } else {
            System.out.println(
                    "Approval failed. Account may no longer be pending.");
        }
    }

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
                    "%-5s %-15s %-12s %-10s%n",
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getStatus()
            );
        }

        System.out.println(border);
    }
}
