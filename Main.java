import java.util.List;
import java.util.Scanner;

public class Main
{
    private static Node root = null;

    public static void main(String[] args) throws Exception
    {
        // Lexer  lexer= new Lexer();
        // String testString="num ";
        // boolean vindex=lexer.CheckIfThereIsAKeyword(testString, 0);
        // System.out.println(vindex);
        // System.out.println(lexer.CheckIfItIsAKey(testString));
       // System.out.println(testString.substring(9, vindex));
        //System.out.println(testString.substring(vindex));
        Scanner scanner = new Scanner(System.in);
        String response;
        do
        {
            System.out.print("Enter file name: ");
            String fileName = scanner.nextLine();
            Lexer lexer_yaka_Grunner= new Lexer();
            List<Token> tokens = lexer_yaka_Grunner.toTokens(fileName);
            //Parser parser = new Parser(fileName);
//            root = parser.parse();
//            Scoping scope = new Scoping();
//            scope.Scope(root);ba
//            Translation translation = new Translation();
//            translation.Translate(root, scope);
            for(int i=0; i<tokens.size(); i++)
            {
                System.out.println(tokens.get(i));
            }

            System.out.print("Do you want to enter another file? (y/n): ");
            response = scanner.nextLine();
        }
        while (response.equals("y") || response.equals("Y"));

        scanner.close();
    }
}
