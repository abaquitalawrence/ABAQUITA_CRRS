
package ui;

import model.User;
import java.util.Scanner;

public class MainMenu {

    private final User user;
    private final Scanner scanner;

    public MainMenu(User user, Scanner scanner) {
        this.user = user;
        this.scanner = scanner;
    }

    public void start() {

        if (!user.isApproved()) {
            System.out.println(
                    "Access denied. Account is not APPROVED.");
            return;
        }

        System.out.println(
                "\n====================================");
        System.out.println(
                " CLOTHING RENTAL & RESERVATION SYSTEM");
        System.out.println(
                "====================================");

        System.out.println("Welcome, " + user.getUsername() + "!");
        System.out.println("Role: " + user.getRole());
        System.out.println("Status: " + user.getStatus());

        switch (user.getRole()) {
            case "ADMIN" -> new AdminMenu(scanner).start();
            case "STAFF", "CUSTOMER" ->
                    new UserMenu(user, scanner).showMenu();
            default -> System.out.println("Unknown user role.");
        }
    }
}
