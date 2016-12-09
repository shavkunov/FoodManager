public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try {
            DatabaseCreator.createBase();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        long endTime   = System.currentTimeMillis();
        double elapsedTime = (endTime - startTime) / 60 * 1000; // in minutes

        System.out.println(elapsedTime);
    }
}
