import java.io.IOException;
import java.util.Scanner;


public class Scrapper {

    public static void main(String[] args) {
        try {
            Scrapper1 s = new Scrapper1();
            s.addQuery("star trek", "movie");
            Scanner scanner = new Scanner(System.in);
            int choice = 0;
            do {
                System.out.println("enter your choice");
                System.out.println("1) search movie");
                System.out.println("2) search TV");
                System.out.println("3) search TV episode");
                System.out.println("4) all option");
                System.out.println("0) exit");
                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1: case 2: case 3: case 4:
                        System.out.print("enter the title to search: ");
                        String title = scanner.nextLine();
                        switch (choice) {
                            case 1: s.addQuery(title, "movie"); break;
                            case 2: s.addQuery(title, "tv"); break;
                            case 3: s.addQuery(title, "tv episode"); break;
                            case 4: s.addQuery(title); break;
                        }
                        break;
                    case 0: break;
                    default: System.out.println("invalid choice");
                }
            } while (choice != 0);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
