import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class Scrapper {

    public static void main(String[] args) {
        Scrapper1 s = new Scrapper1("star trek", 1);
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
            if (choice > 0 && choice <= 4) {
                System.out.print("enter the title to search: ");
                String title = scanner.nextLine();
                Scrapper1 Scrapper1 = new Scrapper1(title, choice);
            }
            else if (choice != 0)
                System.out.println("invalid choice");

        }while(choice != 0);
    }
}
