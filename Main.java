import java.util.Scanner;

public class Main
{
    private static Node root = null;

    public static void main(String[] args) throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        String response;
        do
        {
            System.out.print("Enter file name: ");
            String fileName = scanner.nextLine();
            Parser parser = new Parser(fileName);
//            root = parser.parse();
//            Scoping scope = new Scoping();
//            scope.Scope(root);
//            Translation translation = new Translation();
//            translation.Translate(root, scope);

            System.out.print("Do you want to enter another file? (y/n): ");
            response = scanner.nextLine();
        }
        while (response.equals("y") || response.equals("Y"));

        scanner.close();
    }
}
