//Based on page 27 & 28 of the textbook
//I used the longest prefix of the input string to determine the token type
//Visible on a,b,d,e,i,etc.

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Lexer {
    private int row = 1;
    private int col = 1;
    private int id = 0;

    public List<Token> toTokens(String filename) {
        List<Token> tokens = new ArrayList<Token>();

        String input = "";
        File file = new File(filename);
        Scanner reader = null;
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("\u001B[31mError\u001B[0m: Program file not found, please remove any spaces from the file name, and also include the extension(.txt) after the name and try again.");
            System.exit(0);
        }

        while (reader.hasNextLine()) {
            String input2 = reader.nextLine();
            input = input + input2 + "\n";
        }

        reader.close();

        if (input.isEmpty()) {
            System.out.println("\u001B[31mWarning\u001B[0m: Program file is empty, please write a program in the file and try again.");
            return tokens;
        }

        for(int i = 0;i < input.length();i++) {
            id++;
            col++;

            if (input.charAt(i) == ' ') {
                continue;
            } else if (input.charAt(i) == '\n') {
                row++;
                col = 1;
                continue;
            } else if (input.charAt(i) == '\t') {
                continue;
            }

            //continue from here
        }

        return tokens;
    }
}