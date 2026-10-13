package ui;

import dao.UserDao;
import model.User;

import java.util.Optional;
import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void start() {
        while (true) {
            System.out.println("\n=== CLOTHING RENTAL & RESERVATION SYSTEM ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- Register ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username already taken.");
            return;
        }
        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        String role = null;
        while (role == null) {
            System.out.print("Role (1 = TEACHER, 2 = STUDENT): ");
            switch (scanner.nextLine().trim()) {
                case "1" -> role = "TEACHER";
                case "2" -> role = "STUDENT";
                default -> System.out.println("Please enter 1 or 2.");
            }
        }

        if (userDao.register(username, password, role)) {
            System.out.println("Registered! Your account status is PENDING.");
        } else {
            System.out.println("Registration failed.");
        }
    }

    private void login() {
        System.out.println("\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> user = userDao.login(username, password);
        if (user.isPresent()) {
            new UserMenu(user.get(), scanner).show();
        } else {
            System.out.println("Invalid username or password.");
        }
    }
}
