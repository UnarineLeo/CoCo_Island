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
        System.out.println("Open Tokens.xml file to view tokens");
        writeXML();
    }

    private void writeXML() {
        try {
            FileWriter writer = new FileWriter("Tokens.xml");
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            printTree(tokens, writer, 0);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printTree(List<Token> tokens, FileWriter writer, int level) {

        if (tokens.isEmpty()) {
            try {
                for (int i = 0; i < level; i++) {
                    writer.write("\t");
                }
                writer.write("<Empty/>\n");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            for (int i = 0; i < level; i++) {
                writer.write("\t");
            }

            writer.write("<TokenStream>\n");
            for(int i = 0; i < tokens.size(); i++)
            {
                writer.write("<Token>\n");
                writer.write("<ID> " + tokens.get(i).getId() + " </ID>\n");
                writer.write("<Type> " + tokens.get(i).getType() + " </Type>\n");
                writer.write("<Content> " + tokens.get(i).getContent() + " </Content>\n");
                writer.write("</Token>\n");
            }
            writer.write("</TokenStream>\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Node parse() {
        return null;
    }
}