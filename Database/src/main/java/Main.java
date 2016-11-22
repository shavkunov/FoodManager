public class Main {
    public static void main(String[] args) {
        try {
            DatabaseCreator.createBase();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return;
        }
    }
}
