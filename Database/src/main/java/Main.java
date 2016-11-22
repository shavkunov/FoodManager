import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseCreator.createBase();
            Path newDatabase = Paths.get("content.db");
            Path oldDatabase = Paths.get("../app/src/main/assets/content.db");

            Files.copy(newDatabase, oldDatabase, REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return;
        }
    }
}
