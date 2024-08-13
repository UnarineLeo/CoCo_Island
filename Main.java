import java.util.Scanner;

public class Main
{
    private static Node root = null;

    public static void main(String[] args) throws Exception
    {
        Lexer  lexer= new Lexer();
        String testString="sub oijuh pkoji okijuh okjiuv F_0001lkjh okg  ";
        int vindex=lexer.FindEndTheEndOfTheToken_Function_Variable(testString, 30+3);
        System.out.println(testString.substring(30, vindex));
//         Scanner scanner = new Scanner(System.in);
//         String response;
//         do
//         {
//             System.out.print("Enter file name: ");
//             String fileName = scanner.nextLine();
//             Parser parser = new Parser(fileName);
// //            root = parser.parse();
// //            Scoping scope = new Scoping();
// //            scope.Scope(root);
// //            Translation translation = new Translation();
// //            translation.Translate(root, scope);

//             System.out.print("Do you want to enter another file? (y/n): ");
//             response = scanner.nextLine();
//         }
//         while (response.equals("y") || response.equals("Y"));

//         scanner.close();
    }
}
