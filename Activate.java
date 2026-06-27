import java.util.Scanner;

public class Activate {
    public static void main(String[] args) throws Exception {
        try (//user input to select server or client mode
        Scanner scanner = new Scanner(System.in)) {
            System.out.println("Select mode: (1) Server, (2) Client");
            if (scanner.hasNextInt()) {
                int mode = scanner.nextInt();
                if (mode == 1) {
                    Server.main(args);
                } else if (mode == 2) {
                    Client.main(args);
                } else {
                    System.out.println("Invalid selection. Exiting.");
                }
            } else {
                System.out.println("Invalid input. Exiting.");
            }
        }
    }
    
}
