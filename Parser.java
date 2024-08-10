// Used FIRST AND FOLLOW when parsing using LL(1)
// Pages 65-67
// https://www.youtube.com/watch?v=iddRD8tJi44

import java.util.*;
import java.io.*;

public class Parser {
    private List<Token> tokens;
    private int index = 0;
    private int id = 1;
    private Node root = null;

    public Parser(String filename) {
        Lexer lex = new Lexer();
        tokens = lex.toTokens(filename);

        System.out.println("\u001B[32mSuccess\u001B[0m: Lexing Successful");
        System.out.println("Tokens" + tokens);
        System.out.println();
    }

    public Node parse() {
        return null;
    }
}