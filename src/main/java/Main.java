
import dao.DatabaseInitializer;
import ui.Authentication;

public class Main {

    public static void main(String[] args) {

        try {
            // Step 1: Initialize the database.
            DatabaseInitializer.initialize();

            // Step 2: Start authentication.
            new Authentication().start();

        } catch (Exception e) {
            System.err.println(
                    "Application startup failed: "
                            + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
