
package ui;

import model.User;
import java.util.Scanner;

public class UserMenu {

    private final User user;
    private final Scanner scanner;

    public UserMenu(User user, Scanner scanner) {
        this.user = user;
        this.scanner = scanner;
    }

    public void showMenu() {

        while (true) {

            System.out.println(
                    "\n=== " + user.getRole() + " MENU ===");

            switch (user.getRole()) {

                case "ADMIN" -> {
                    System.out.println("1. Manage Users");
                    System.out.println("2. Manage Clothing");
                    System.out.println("3. View Reservations");
                    System.out.println("4. Logout");
                }

                case "STAFF" -> {
                    System.out.println("1. View Clothing");
                    System.out.println("2. View Reservations");
                    System.out.println("3. Logout");
                }

                case "CUSTOMER" -> {
                    System.out.println("1. Browse Clothing");
                    System.out.println("2. Reserve Clothing");
                    System.out.println("3. My Reservations");
                    System.out.println("4. Logout");
                }

                default -> {
                    System.out.println("Invalid user role.");
                    return;
                }
            }

            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            int logoutOption =
                    user.getRole().equals("STAFF") ? 3 : 4;

            if (choice.equals(
                    String.valueOf(logoutOption))) {
                System.out.println("Logout successful!");
                return;
            }

            boolean validOption = switch (user.getRole()) {
                case "ADMIN", "CUSTOMER" ->
                        choice.matches("[1-3]");
                case "STAFF" -> choice.matches("[1-2]");
                default -> false;
            };

            if (validOption) {
                System.out.println(
                        "This feature is not yet implemented.");
            } else {
                System.out.println(
                        "Invalid choice. Please try again.");
            }
        }
    }
}
