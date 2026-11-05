package ui;

import dao.ActivityLogDao;
import model.User;

import java.util.Scanner;

public class UserMenu {

    private final User user;
    private final Scanner scanner;
    private final ActivityLogDao activityLogDao;

    public UserMenu(User user, Scanner scanner) {

        this.user = user;
        this.scanner = scanner;
        this.activityLogDao = new ActivityLogDao();
    }

    public void showMenu() {

        while (true) {

            System.out.println(
                    "\n=== " + user.getRole() + " MENU ===");

            if ("STAFF".equals(user.getRole())) {

                System.out.println("1. View Clothing");
                System.out.println("2. View Reservations");
                System.out.println("3. Logout");

            } else if ("CUSTOMER".equals(user.getRole())) {

                System.out.println("1. Browse Clothing");
                System.out.println("2. Reserve Clothing");
                System.out.println("3. My Reservations");
                System.out.println("4. Logout");

            } else {

                System.out.println("Invalid user role.");
                return;
            }

            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            if ("STAFF".equals(user.getRole())) {

                switch (choice) {

                    case "1", "2" -> System.out.println(
                            "This feature is not yet implemented.");

                    case "3" -> {

                        // RECORD STAFF LOGOUT
                        activityLogDao.logAction(
                                user.getId(),
                                "LOGOUT",
                                "STAFF user "
                                        + user.getUsername()
                                        + " logged out."
                        );

                        System.out.println("Logout successful!");
                        return;
                    }

                    default -> System.out.println(
                            "Invalid choice.");
                }

            } else {

                switch (choice) {

                    case "1", "2", "3" -> System.out.println(
                            "This feature is not yet implemented.");

                    case "4" -> {

                        // RECORD CUSTOMER LOGOUT
                        activityLogDao.logAction(
                                user.getId(),
                                "LOGOUT",
                                "CUSTOMER user "
                                        + user.getUsername()
                                        + " logged out."
                        );

                        System.out.println("Logout successful!");
                        return;
                    }

                    default -> System.out.println(
                            "Invalid choice.");
                }
            }
        }
    }
}