// Used FIRST AND FOLLOW when parsing using LL(1)
// Pages 65-67
// https://www.youtube.com/watch?v=iddRD8tJi44

import java.util.*;
import java.io.*;

public class Parser
{
    private List<Token> tokens;
    private int index = 0;
    private int id = 1;
    private Node root = null;

    public Parser(String filename)
    {
        Lexer lex = new Lexer();
        tokens = lex.toTokens(filename);

        System.out.println("\u001B[32mSuccess\u001B[0m: Lexing Successful");
        System.out.println("Tokens" + tokens);
        System.out.println();
        System.out.println("Open Tokens.xml file to view tokens");
        writeXML();
    }

    private void writeXML()
    {
        try
        {
            FileWriter writer = new FileWriter("Tokens.xml");
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            printTree(tokens, writer, 0);
            writer.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void printTree(List<Token> tokens, FileWriter writer, int level)
    {
        if (tokens.isEmpty())
        {
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

    public Node parse()
    {
        if(parsePROG(null))
        {
            System.out.println("\u001B[32mSuccess\u001B[0m: Parsing successful");
            return root;
//            System.out.println("Tree: " );
//            System.out.println(root.toStringTree());
        }
        else
        {
            System.out.println("Parsing failed");
            return null;
        }
    }

    private Boolean parsePROG(Node parent)
    {
        Node startNode = new Node(id++, "Non-Terminal", "PROG");
        if(parent != null)
        {
            parent.children.add(startNode);
        }
        else
        {
            root = startNode;
        }

        if(tokens.isEmpty())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: The program is empty, the program is supposed to have atleast \"main\"");
            System.exit(0);
            return false;
        }
        else
        {
            if(tokens.get(index).getContent() == "main")
            {
                Node mainNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                startNode.children.add(mainNode);
                index++;

                Boolean global  = parseGLOBVARS(startNode);
                if(global)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO(\"begin INSTRUC end\") at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        //parseALGO will call parseFUNCTIONS
                       Boolean algo = parseALGO(startNode);
                       if(algo)
                       {
                           //Program should be done now
                           if(parent == null)
                           {
                               if(index < tokens.size())
                               {
                                   System.out.println("\u001B[31mParsing Error\u001B[0m: invalid symbol at line "
                                   + tokens.get(index).getRow() + " col " + tokens.get(index).getCol() + ", token: "
                                   + tokens.get(index).getContent());
                                   System.exit(0);
                                   return false;
                               }
                           }
                           return true;
                       }
                       else
                       {
                           System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Expected ALGO(\"begin INSTRUC end\") at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                           System.exit(0);
                           return false;
                       }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected GLOBVARS at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                    System.exit(0);
                    return false;
                }

            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"main\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                System.exit(0);
                return false;
            }

        }
    }

    private Boolean parseGLOBVARS(Node parent)
    {
        Node globalNode = new Node(id++, "Non-Terminal", "GLOBVARS");
        parent.children.add(globalNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO(\"begin INSTRUC end\") at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }
        else if(tokens.get(index).getType() == "Keyword")
        {
            if(tokens.get(index).getContent() == "begin")
            {
                //if GLOBVARS is nullable
                return true;
            }
            else if(tokens.get(index).getContent() == "num" || tokens.get(index).getContent() == "text")
            {
                Boolean VType = parseVTYP(globalNode);
                if(VType)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                        System.exit(0);
                        return false;
                    }

                    if(tokens.get(index).getType() == "VNAME")
                    {
                        Boolean VName = parseVNAME(globalNode);
                        if(VName)
                        {
                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") after VNAME at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                System.exit(0);
                                return false;
                            }

                            if(tokens.get(index).getContent() == ",")
                            {
                                Node commaNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                parent.children.add(commaNode);
                                index++;

                                if(index >= tokens.size())
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO(\"begin INSTRUC end\") after VNAME at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                    System.exit(0);
                                    return false;
                                }
                                else
                                {
                                    if(tokens.get(index).getType() == "Keyword")
                                    {
                                        if(tokens.get(index).getContent() == "begin")
                                        {
                                            //if GLOBVARS is nullable
                                            return true;
                                        }
                                        else if(tokens.get(index).getContent() == "num" || tokens.get(index).getContent() == "text")
                                        {
                                            parseGLOBVARS(parent);
                                            return true;
                                        }
                                        else
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") after VNAME at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                System.exit(0);
                                return false;
                            }

                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                        System.exit(0);
                        return false;
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseVTYP(Node parent)
    {
        Node VTypNode = new Node(id++, "Non-Terminal", "VTYP");
        parent.children.add(VTypNode);

        if(tokens.get(index).getContent() == "num" || tokens.get(index).getContent() == "text")
        {
            Node TypeNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            parent.children.add(TypeNode);
            index++;

            if(index >= tokens.size())
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                System.exit(0);
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"num\" or \"text\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }

    }

    private Boolean parseVNAME(Node parent)
    {
        Node NameNode = new Node(id++, "Non-Terminal", "VNAME");
        parent.children.add(NameNode);

        if(tokens.get(index).getType() == "VNAME")
        {
            Node Name = new Node(id++, "Terminal", tokens.get(index).getContent());
            NameNode.children.add(Name);
            index++;

            if(index >= tokens.size())
            {
                //  , |  ; | < input | = | )
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") or \"< input \" or \"=\" or \";\" or \")\" after VNAME at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                System.exit(0);
                return false;
            }

            if(tokens.get(index).getContent() == "," || tokens.get(index).getContent() == "< input" || tokens.get(index).getContent() == "=" || tokens.get(index).getContent() == ";" || tokens.get(index).getContent() == ")")
            {
                return true;
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") or \"< input \" or \"=\" or \";\" or \")\" after VNAME at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseALGO(Node parent)
    {
        Node AlgoNode = new Node(id++, "Non-Terminal", "ALGO");
        parent.children.add(AlgoNode);

        if(tokens.get(index).getContent() == "begin")
        {
            Node beginNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            parent.children.add(beginNode);
            index++;

            if(index >= tokens.size())
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected INSTRUC at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                System.exit(0);
                return false;
            }
            else
            {
                Boolean instruc = parseINSTRUC(AlgoNode);
                if(instruc)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected end at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(tokens.get(index).getContent() == "end")
                        {
                            Node endNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            parent.children.add(endNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                //confirm if parent or AlgoNode
                                Boolean functions = parseFUNCTIONS(parent);
                                if(functions)
                                {
                                    return true;
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FUNCTIONS at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                    System.exit(0);
                                    return false;
                                }

                            }
                            else if(tokens.get(index).getContent() == "num" || tokens.get(index).getContent() == "void" || tokens.get(index).getContent() == "}" || tokens.get(index).getContent() == "else" || tokens.get(index).getContent() == ";")
                            {
                                if(tokens.get(index).getContent() == "num" || tokens.get(index).getContent() == "void")
                                {
                                    //confirm if parent or AlgoNode
                                    Boolean functions = parseFUNCTIONS(parent);
                                    if(functions)
                                    {
                                        return true;
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FUNCTIONS at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                                // } | else | ;
                                return true;
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"num\" or \"void\" or \"}\" or \"else\" or \";\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"end\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected INSTRUC at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                    System.exit(0);
                    return false;
                }
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseINSTRUC(Node parent)
    {
        Node InstrucNode = new Node(id++, "Non-Terminal", "INSTRUC");
        parent.children.add(InstrucNode);

        if(tokens.get(index).getContent() == "print" || tokens.get(index).getContent() == "skip" || tokens.get(index).getContent() == "halt" ||
        tokens.get(index).getContent() == "if" || tokens.get(index).getType() == "VNAME" || tokens.get(index).getType() == "FNAME" || tokens.get(index).getContent() == "end")
        {
            if(tokens.get(index).getContent() == "end")
            {
                return true;
            }
            else
            {
                Boolean command = parseCOMMAND(parent);
                if(command)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                        System.exit(0);
                        return false;
                    }
                    else if(tokens.get(index).getContent() == ";")
                    {
                        Node colonNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                        parent.children.add(colonNode);
                        index++;

                        if(index >= tokens.size())
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected end at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                            System.exit(0);
                            return false;
                        }
                        else if(tokens.get(index).getContent() == "end")
                        {
                            return true;
                        }
                        else
                        {
                            parseINSTRUC(parent);
                            return true;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                        System.exit(0);
                        return false;
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected COMMAND at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                    System.exit(0);
                    return false;
                }
            }

        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"print\" or \"skip\" or \"halt\" or \"if\" or \"VNAME\" or \"FNAME\" or \"end\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }

    }

    private Boolean parseCOMMAND(Node parent)
    {
        Node commandNode = new Node(id++, "Non-Terminal", "COMMAND");
        parent.children.add(commandNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"print\" or \"skip\" or \"halt\" or \"if\" or \"VNAME\" or \"FNAME\" or \"end\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(tokens.get(index).getContent() == "skip" || tokens.get(index).getContent() == "halt")
            {
                return true;
            }
            else if(tokens.get(index).getContent() == "print")
            {
                //print ATOMIC
                Node printNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                parent.children.add(printNode);
                index++;

                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ATOMIC at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                    System.exit(0);
                    return false;
                }
                else
                {
                    if(tokens.get(index).getType() == "VNAME" || tokens.get(index).getType() == "CONST")
                    {
                        Boolean atomic = parseATOMIC(parent);
                        if(atomic)
                        {
                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                System.exit(0);
                                return false;
                            }
                            else if(tokens.get(index).getContent() == ";")
                            {
                                return true;
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ATOMIC at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME or FNAME after \"print\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                        System.exit(0);
                        return false;
                    }
                }
            }
            else if(tokens.get(index).getContent() == "if")
            {
                //Branch
                return true;
            }
            else if(tokens.get(index).getType() == "VNAME")
            {
                //ASSIGN
                return true;

            }
            else if(tokens.get(index).getType() == "FNAME")
            {
                //CALL
                return true;
            }
            else
            {
                return true;
            }
        }

    }

    private Boolean parseATOMIC(Node parent)
    {
        Node atomicNode = new Node(id++, "Non-Terminal", "ATOMIC");
        parent.children.add(atomicNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" or \"CONST\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }
        else if(tokens.get(index).getType() == "VNAME")
        {
            return parseVNAME(atomicNode);//consult
        }
        else if(tokens.get(index).getType() == "CONST")
        {
            Boolean constant = parseCONST(parent);
            if(constant)
            {
                return true;//consult
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"CONST\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" or \"CONST\" at line " + tokens.get(index).getRow() + " col " + tokens.get(index).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseCONST(Node parent) {
        return true;
    }

    private Boolean parseFUNCTIONS(Node parent) {
        return true;
    }
}