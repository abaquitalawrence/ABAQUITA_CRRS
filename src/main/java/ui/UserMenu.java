package ui;

import model.User;

import java.util.Scanner;

/** Screen shown after a successful login. Add your rental/reservation features here. */
public class UserMenu {

    private final User user;
    private final Scanner scanner;

    public UserMenu(User user, Scanner scanner) {
        this.user = user;
        this.scanner = scanner;
    }

    public void show() {
        System.out.printf("%nWelcome, %s (%s) - status: %s%n", user.username(), user.roles(), user.status());

        while (true) {
            System.out.println("\n--- User Menu ---");
            // TODO: add your clothing rental / reservation options here
            System.out.println("1. Logout");
            System.out.print("Choose: ");

            if (scanner.nextLine().trim().equals("1")) {
                System.out.println("Logged out.");
                return; // back to main menu
            }
            System.out.println("Invalid choice.");
        }
    }
}
