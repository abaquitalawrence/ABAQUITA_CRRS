
package ui;

import dao.UserDao;
import model.User;

import java.util.Optional;
import java.util.Scanner;

public class Authentication {

    private final Scanner scanner;
    private final UserDao userDao;

    public Authentication() {
        this.scanner = new Scanner(System.in);
        this.userDao = new UserDao();
    }

    public void start() {
        while (true) {
            System.out.println("\n=== CLOTHING RENTAL & RESERVATION SYSTEM ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
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
            System.out.println("\nSelect your role:");
            System.out.println("1. ADMIN");
            System.out.println("2. STAFF");
            System.out.println("3. CUSTOMER");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> role = "ADMIN";
                case "2" -> role = "STAFF";
                case "3" -> role = "CUSTOMER";
                default -> System.out.println(
                        "Invalid choice. Enter 1, 2, or 3."
                );
            }
        }

        if (userDao.register(username, password, role)) {
            System.out.println(
                    "Registration successful. Account status: PENDING."
            );
        } else {
            System.out.println(
                    "Registration failed. Please check the database."
            );
        }
    }

    private void login() {
        System.out.println("\n--- Login ---");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println(
                    "Username and password cannot be empty."
            );
            return;
        }

        Optional<User> result = userDao.login(username, password);

        if (result.isEmpty()) {
            System.out.println("Invalid username or password.");
            return;
        }

        User user = result.get();

        // Check account status before allowing login
        if (!user.isActive()) {
            System.out.println(
                    "Login denied. Your account status is: "
                            + user.getStatus()
            );
            System.out.println(
                    "Only ACTIVE accounts can access the system."
            );
            return;
        }

        // Successful login
        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.getUsername() + "!");
        System.out.println("Role: " + user.getRole());

        // Open the main menu based on user role
        new MainMenu(user, scanner).start();
    }
}
