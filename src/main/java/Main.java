
import dao.DatabaseInitializer;
import ui.Authentication;

public class Main {

    public static void main(String[] args) {

        try {
            DatabaseInitializer.initialize();
            new Authentication().start();

        } catch (Exception e) {
            System.err.println(
                    "Application startup failed: "
                            + e.getMessage());
            e.printStackTrace();
        }
    }
}
