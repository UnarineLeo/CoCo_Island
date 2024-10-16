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
            printTokens(tokens, writer, 0);
            writer.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void printTokens(List<Token> tokens, FileWriter writer, int level)
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
            for (Token token : tokens) {
                String content = token.getContent();

                if (Objects.equals(content, "< input")) {
                    content = "&lt; input";
                }

                writer.write("<Token>\n");
                writer.write("<ID> " + token.getId() + " </ID>\n");
                writer.write("<Type> " + token.getType() + " </Type>\n");
                writer.write("<Content> " + content + " </Content>\n");
                writer.write("</Token>\n");
            }
            writer.write("</TokenStream>\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTreeXML()
    {
        try
        {
            FileWriter writer = new FileWriter("Tree.xml");
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            printTree(root, writer, 0);
            writer.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void printTree(Node node, FileWriter writer, int level)
    {
        String content = "";
        try
        {
            for (int i = 0; i < level; i++) {
                writer.write("\t");
            }

            writer.write("<SyntaxTree>\n");

            writer.write("<Root>\n");

            writer.write("<UniqueID>" + node.getId() + "</UniqueID>\n");
            writer.write("<StartSymbol>" + node.getContent() + "</StartSymbol>\n");
            writer.write("<Children>\n");
            for(Node child : node.children)
            {
                content = child.getContent();
                if (Objects.equals(content, "< input")) {
                    content = "&lt; input";
                }

                writer.write("<Child>\n");
                writer.write("<UniqueID> " + child.getId() + " </UniqueID>\n");
                writer.write("<Type> " + child.getType() + " </Type>\n");
                writer.write("<Content> " + content + " </Content>\n");
                writer.write("</Child>\n");
            }
            writer.write("</Children>\n");

            writer.write("</Root>\n");

            writer.write("<InnerNodes>\n");
            // between the root and the leaf [Only Non-Terminals]

            writer.write("</InnerNodes>\n");

            writer.write("<LeafNodes>\n");
            //for only terminals also indicate parent
            writer.write("</LeafNodes>\n");

            writer.write("</SyntaxTree>\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Node parse()
    {
        if(parsePROG(null))
        {
            System.out.println("\u001B[32mSuccess\u001B[0m: Parsing successful");
//            System.out.println("Open Tree.xml file to view the tree" );
//            writeTreeXML();
            return root;
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
            if(Objects.equals(tokens.get(index).getContent(), "main"))
            {
                Node mainNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                startNode.children.add(mainNode);
                index++;

                Boolean global  = parseGLOBVARS(startNode);
                if(global)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO(\"begin INSTRUC end\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
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
                                   + tokens.get(index).getRow() + " col " + tokens.get(index-1).getCol() + ", token: "
                                   + tokens.get(index).getContent());
                                   System.exit(0);
                                   return false;
                               }
                           }
                           return true;
                       }
                       else
                       {
                           System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Expected ALGO(\"begin INSTRUC end\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                           System.exit(0);
                           return false;
                       }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected GLOBVARS at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }

            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"main\" at the first line of the textfile.");
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
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO(\"begin INSTRUC end\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else if(Objects.equals(tokens.get(index).getType(), "Keyword"))
        {
            if(Objects.equals(tokens.get(index).getContent(), "begin"))
            {
                //if GLOBVARS is nullable
                return true;
            }
            else if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "text"))
            {
                Boolean VType = parseVTYP(globalNode);
                if(VType)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }

                    if(Objects.equals(tokens.get(index).getType(), "VNAME"))
                    {
                        Boolean VName = parseVNAME(globalNode);
                        if(VName)
                        {
                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") after VNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }

                            if(Objects.equals(tokens.get(index).getContent(), ","))
                            {
                                Node commaNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                globalNode.children.add(commaNode);
                                index++;

                                if(index >= tokens.size())
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO(\"begin INSTRUC end\") after VNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                                else
                                {
                                    if(Objects.equals(tokens.get(index).getType(), "Keyword"))
                                    {
                                        if(Objects.equals(tokens.get(index).getContent(), "begin"))
                                        {
                                            //if GLOBVARS is nullable
                                            return true;
                                        }
                                        else if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "text"))
                                        {
                                            parseGLOBVARS(globalNode);
                                            return true;
                                        }
                                        else
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") after VNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }

                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseVTYP(Node parent)
    {
        Node VTypNode = new Node(id++, "Non-Terminal", "VTYP");
        parent.children.add(VTypNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" or \"num\" or \"text\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

        if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "text"))
        {
            Node TypeNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            VTypNode.children.add(TypeNode);
            index++;

            if(index >= tokens.size())
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME after VTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
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
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"num\" or \"text\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseVNAME(Node parent)
    {
        Node NameNode = new Node(id++, "Non-Terminal", "VNAME");
        parent.children.add(NameNode);

        if(Objects.equals(tokens.get(index).getType(), "VNAME"))
        {
            Node Name = new Node(id++, "Terminal", tokens.get(index).getContent());
            NameNode.children.add(Name);
            index++;

            if(index >= tokens.size())
            {
                //  , |  ; | < input | = | )
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") or \"< input \" or \"=\" or \";\" or \")\" after VNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }

            if(Objects.equals(tokens.get(index).getContent(), ",") || Objects.equals(tokens.get(index).getContent(), "< input") || Objects.equals(tokens.get(index).getContent(), "=") || Objects.equals(tokens.get(index).getContent(), ";") || Objects.equals(tokens.get(index).getContent(), ")"))
            {
                return true;
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") or \"< input \" or \"=\" or \";\" or \")\" after VNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseALGO(Node parent)
    {
        Node AlgoNode = new Node(id++, "Non-Terminal", "ALGO");
        parent.children.add(AlgoNode);

        if(Objects.equals(tokens.get(index).getContent(), "begin"))
        {
            Node beginNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            AlgoNode.children.add(beginNode);
            index++;

            if(index >= tokens.size())
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected INSTRUC at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
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
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected end at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), "end"))
                        {
                            Node endNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            AlgoNode.children.add(endNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                //confirm if parent or AlgoNode
                                Boolean functions = parseFUNCTIONS(parent); //shares same parent as Algo
                                if(functions)
                                {
                                    return true;
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FUNCTIONS at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }

                            }
                            else if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void")
                                    || Objects.equals(tokens.get(index).getContent(), "}") || Objects.equals(tokens.get(index).getContent(), "else") ||
                                    Objects.equals(tokens.get(index).getContent(), ";"))
                            {
                                if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void"))
                                {
                                    //confirm if parent or AlgoNode
                                    Boolean functions = parseFUNCTIONS(parent); //shares same parent as Algo
                                    if(functions)
                                    {
                                        return true;
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FUNCTIONS at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                                // } | else | ;
                                return true;
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"num\" or \"void\" or \"}\" or \"else\" or \";\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected INSTRUC at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseINSTRUC(Node parent)
    {
        Node InstrucNode = new Node(id++, "Non-Terminal", "INSTRUC");
        parent.children.add(InstrucNode);

        if(Objects.equals(tokens.get(index).getContent(), "print") || Objects.equals(tokens.get(index).getContent(), "return") || Objects.equals(tokens.get(index).getContent(), "skip") || Objects.equals(tokens.get(index).getContent(), "halt") ||
        Objects.equals(tokens.get(index).getContent(), "if") || Objects.equals(tokens.get(index).getType(), "VNAME") || Objects.equals(tokens.get(index).getType(), "FNAME") || Objects.equals(tokens.get(index).getContent(), "end"))
        {
            if(Objects.equals(tokens.get(index).getContent(), "end"))
            {
                return true;
            }
            else
            {
                Boolean command = parseCOMMAND(InstrucNode);
                if(command)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else if(Objects.equals(tokens.get(index).getContent(), ";"))
                    {
                        Node colonNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                        InstrucNode.children.add(colonNode);
                        index++;

                        if(index >= tokens.size())
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected end at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                        else if(Objects.equals(tokens.get(index).getContent(), "end"))
                        {
                            return true;
                        }
                        else
                        {
                            parseINSTRUC(InstrucNode); //calling itself
                            return true;//consult
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected COMMAND at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }

        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"print\" or \"skip\" or \"halt\" or \"if\" or \"VNAME\" or \"FNAME\" or \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
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
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"print\" or \"skip\" or \"halt\" or \"if\" or \"VNAME\" or \"FNAME\" or \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "skip") || Objects.equals(tokens.get(index).getContent(), "halt"))
            {
                Node nextNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                commandNode.children.add(nextNode);
                index++;

                return true;
            }
            else if(Objects.equals(tokens.get(index).getContent(), "print") || Objects.equals(tokens.get(index).getContent(), "return"))
            {
                //print ATOMIC || return ATOMIC
                Node printNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                commandNode.children.add(printNode);
                index++;

                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ATOMIC at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
                else
                {
                    if(Objects.equals(tokens.get(index).getType(), "VNAME") || Objects.equals(tokens.get(index).getType(), "CONST"))
                    {
                        Boolean atomic = parseATOMIC(commandNode);
                        if(atomic)
                        {
                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                            else if(Objects.equals(tokens.get(index).getContent(), ";"))
                            {
                                return true;
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ATOMIC at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VNAME or CONST after \"print\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
            }
            else if(Objects.equals(tokens.get(index).getContent(), "if"))
            {
                //Branch
                Boolean branch = parseBRANCH(commandNode);
                if(branch)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"BRANCH\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), ";"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"BRANCH\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"BRANCH\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else if(Objects.equals(tokens.get(index).getType(), "VNAME"))
            {
                //ASSIGN
                Boolean assign = parseASSIGN(commandNode);
                if(assign)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"ASSIGN\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), ";"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"ASSIGN\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ASSIGN\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else if(Objects.equals(tokens.get(index).getType(), "FNAME"))
            {
                //CALL
                Boolean call = parseCALL(commandNode);
                if(call)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"CALL\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), ";"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"CALL\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"CALL\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"print\" or \"skip\" or \"halt\" or \"if\" or \"VNAME\" or \"FNAME\" or \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }

    }

    private Boolean parseASSIGN(Node parent)
    {
        Node assignNode = new Node(id++, "Non-Terminal", "ASSIGN");
        parent.children.add(assignNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getType(), "VNAME"))
            {
                Boolean vname = parseVNAME(assignNode);
                if(vname)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"< input\" or \"=\" after \"VNAME\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), "< input"))
                        {
                            Node inputNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            assignNode.children.add(inputNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"< input\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                            else
                            {
                                if(Objects.equals(tokens.get(index).getContent(), ";"))
                                {
                                    return true;
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"< input\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                            }
                        }
                        else if(Objects.equals(tokens.get(index).getContent(), "="))
                        {
                            Node equalNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            assignNode.children.add(equalNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"TERM\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                            else
                            {
                                //TERM = CALL | OP | ATOMIC
                                if(Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
                                        Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div") ||
                                        Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
                                        Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
                                        Objects.equals(tokens.get(index).getType(), "FNAME") || Objects.equals(tokens.get(index).getType(), "VNAME") ||
                                        Objects.equals(tokens.get(index).getType(), "CONST") || Objects.equals(tokens.get(index).getContent(), "sqrt") ||
                                        Objects.equals(tokens.get(index).getContent(), "not")
                                )
                                {
                                    Boolean term = parseTERM(assignNode);
                                    if(term)
                                    {
                                        if(index >= tokens.size())
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"TERM\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                        else
                                        {
                                            if(Objects.equals(tokens.get(index).getContent(), ";"))
                                            {
                                                return true;
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"TERM\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"TERM\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"TERM\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"< input\" or \"=\" after \"VNAME\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }

    }

    private Boolean parseTERM(Node parent)
    {
        Node termNode = new Node(id++, "Non-Terminal", "TERM");
        parent.children.add(termNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"TERM\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
                    Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div") ||
                    Objects.equals(tokens.get(index).getContent(), "sqrt") || Objects.equals(tokens.get(index).getContent(), "or") ||
                    Objects.equals(tokens.get(index).getContent(), "and") ||
                    Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
                    Objects.equals(tokens.get(index).getContent(), "not")
            )
            {
                Boolean op = parseOP(termNode);
                if(op)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), ";"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"OP\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else if(Objects.equals(tokens.get(index).getType(), "FNAME"))
            {
                Boolean call = parseCALL(termNode);
                if(call)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"CALL\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), ";"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"CALL\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else if(Objects.equals(tokens.get(index).getType(), "VNAME") || Objects.equals(tokens.get(index).getType(), "CONST"))
            {
                Boolean atomic = parseATOMIC(termNode);
                if(atomic)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"TERM\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), ";"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"TERM\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseOP(Node parent)
    {
        Node opNode = new Node(id++, "Non-Terminal", "OP");
        parent.children.add(opNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"OP\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub")
            || Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div")
            || Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
            Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt")
            )
            {
                Boolean binop = parseBINOP(opNode);
                if(binop)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"(\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), "("))
                        {
                            Node openBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            opNode.children.add(openBracketNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \"(\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }

                            Boolean arg = parseARG(opNode);
                            if(arg)
                            {
                                if(index >= tokens.size())
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ARG\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                                else
                                {
                                    if(Objects.equals(tokens.get(index).getContent(), ","))
                                    {
                                        Node commaNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                        opNode.children.add(commaNode);
                                        index++;

                                        if(index >= tokens.size())
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \",\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }

                                        Boolean arg2 = parseARG(opNode);
                                        if(arg2)
                                        {
                                            if(index >= tokens.size())
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \")\" after \"ARG\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }

                                            if(Objects.equals(tokens.get(index).getContent(), ")"))
                                            {
                                                Node closingBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                opNode.children.add(closingBracketNode);
                                                index++;

                                                if(index >= tokens.size())
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \")\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }
                                                else
                                                {
                                                    //confirm
                                                    if(Objects.equals(tokens.get(index).getContent(), ";") || Objects.equals(tokens.get(index).getContent(), ")") || Objects.equals(tokens.get(index).getContent(), ","))
                                                    {
                                                        return true;
                                                    }
                                                    else
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \")\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \")\" after \"ARG\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                        }
                                        else
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \",\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ARG\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \"(\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"BINOP\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else if(Objects.equals(tokens.get(index).getContent(), "sqrt") || Objects.equals(tokens.get(index).getContent(), "not"))
            {
                Boolean unop = parseUNOP(opNode);
                if(unop)
                {
                    if(index >= tokens.size())
                    {
                       System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"(\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), "("))
                        {
                            Node openBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            opNode.children.add(openBracketNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \"(\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }

                            Boolean arg = parseARG(opNode);
                            if(arg)
                            {
                                if(index >= tokens.size())
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \")\" after \"ARG\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                                else
                                {
                                    if(Objects.equals(tokens.get(index).getContent(), ")"))
                                    {
                                        Node closingBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                        opNode.children.add(closingBracketNode);
                                        index++;

                                        if(index >= tokens.size())
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \")\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                        else
                                        {
                                            //consult
                                            if(Objects.equals(tokens.get(index).getContent(), ";") || Objects.equals(tokens.get(index).getContent(), ")") || Objects.equals(tokens.get(index).getContent(), ","))
                                            {
                                                return true;
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \")\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \")\" after \"ARG\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \"(\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }


                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"UNOP\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"add\" or \"sub\" or \"mul\" or \"div\" or \"sqrt\" after \"=\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseARG(Node parent)
    {
        Node argNode = new Node(id++, "Non-Terminal", "ARG");
        parent.children.add(argNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"OP\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
            Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div") ||
            Objects.equals(tokens.get(index).getContent(), "sqrt") || Objects.equals(tokens.get(index).getContent(), "or") ||
            Objects.equals(tokens.get(index).getContent(), "and") || Objects.equals(tokens.get(index).getContent(), "not") ||
            Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt")
            )
            {
                Boolean opnode2 = parseOP(argNode);
                if(opnode2)
                {
                    ///
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" or \")\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        //consult
                        if(Objects.equals(tokens.get(index).getContent(), ",") || Objects.equals(tokens.get(index).getContent(), ")"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" or \")\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"OP\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else if(Objects.equals(tokens.get(index).getType(), "VNAME") || Objects.equals(tokens.get(index).getType(), "CONST"))
            {
                Boolean atomic = parseATOMIC(argNode);
                if(atomic)
                {
                    ///
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" or \")\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), ",") || Objects.equals(tokens.get(index).getContent(), ")"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" or \")\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"OP\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ARG\" after \"(\" or \",\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseCALL(Node parent)
    {
        Node callNode = new Node(id++, "Non-Terminal", "CALL");
        parent.children.add(callNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"if\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            Boolean fname = parseFNAME(callNode);
            if(fname)
            {
                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"(\" after \"FNAME\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
                else
                {
                    if(Objects.equals(tokens.get(index).getContent(), "("))
                    {
                        Node openingBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                        callNode.children.add(openingBracketNode);
                        index++;

                        if(index >= tokens.size())
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"(\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                        else
                        {
                            Boolean atomic = parseATOMIC(callNode);
                            if(atomic)
                            {
                                if(index >= tokens.size())
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ATOMIC\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                                else
                                {
                                    if(Objects.equals(tokens.get(index).getContent(), ","))
                                    {
                                        Node commaNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                        callNode.children.add(commaNode);
                                        index++;

                                        if(index >= tokens.size())
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \",\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                        else
                                        {
                                            Boolean atomic2 = parseATOMIC(callNode);
                                            if(atomic2)
                                            {
                                                if(index >= tokens.size())
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ATOMIC\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }
                                                else
                                                {
                                                    if(Objects.equals(tokens.get(index).getContent(), ","))
                                                    {
                                                        Node commaNode2 = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                        callNode.children.add(commaNode2);
                                                        index++;

                                                        if(index >= tokens.size())
                                                        {
                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \",\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                            System.exit(0);
                                                            return false;
                                                        }
                                                        else
                                                        {
                                                            Boolean atomic3 = parseATOMIC(callNode);
                                                            if(atomic3)
                                                            {
                                                                if(index >= tokens.size())
                                                                {
                                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \")\" after \"ATOMIC\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                    System.exit(0);
                                                                    return false;
                                                                }
                                                                else
                                                                {
                                                                    if(Objects.equals(tokens.get(index).getContent(), ")"))
                                                                    {
                                                                        Node closingBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                                        callNode.children.add(closingBracketNode);
                                                                        index++;

                                                                        if(index >= tokens.size())
                                                                        {
                                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \")\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                            System.exit(0);
                                                                            return false;
                                                                        }
                                                                        else
                                                                        {
                                                                            if(Objects.equals(tokens.get(index).getContent(), ";"))
                                                                            {
                                                                                return true;
                                                                            }
                                                                            else
                                                                            {
                                                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \";\" after \"COMMAND\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                                System.exit(0);
                                                                                return false;
                                                                            }
                                                                        }
                                                                    }
                                                                    else
                                                                    {
                                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \")\" after \"ATOMIC\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                        System.exit(0);
                                                                        return false;
                                                                    }
                                                                }
                                                            }
                                                            else
                                                            {
                                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \",\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                System.exit(0);
                                                                return false;
                                                            }
                                                        }
                                                    }
                                                    else
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ATOMIC\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \",\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ATOMIC\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"(\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"(\" after \"FNAME\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FNAME\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseFNAME(Node parent)
    {
        Node FNameNode = new Node(id++, "Non-Terminal", "FNAME");
        parent.children.add(FNameNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

        if(Objects.equals(tokens.get(index).getType(), "FNAME"))
        {
            Node Name = new Node(id++, "Terminal", tokens.get(index).getContent());
            FNameNode.children.add(Name);
            index++;

            if(index >= tokens.size())
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"(\" after FNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }

            if(Objects.equals(tokens.get(index).getContent(), "("))
            {
                return true;
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"(\" after FNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FNAME at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseBRANCH(Node parent)
    {
        Node branchNode = new Node(id++, "Non-Terminal", "BRANCH");
        parent.children.add(branchNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"if\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            Node ifNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            branchNode.children.add(ifNode);
            index++;

            if(index >= tokens.size())
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"COND\" after \"if\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
            else
            {
                Boolean cond = parseCOND(branchNode);
                if(cond)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), "then"))
                        {
                            Node thenNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            branchNode.children.add(thenNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO after \"then\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                            else
                            {
                                Boolean algo = parseALGO(branchNode);
                                if (algo)
                                {
                                    if (index >= tokens.size())
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"else\" or Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                    else
                                    {
                                        if (Objects.equals(tokens.get(index).getContent(), "else"))
                                        {
                                            Node elseNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                            branchNode.children.add(elseNode);
                                            index++;
//
                                            if (index >= tokens.size())
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO after \"else\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                            else
                                            {
                                                Boolean algo2 = parseALGO(branchNode);
                                                if (algo2)
                                                {
                                                    if (index >= tokens.size())
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") after ALGO at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                    else if (Objects.equals(tokens.get(index).getContent(), ";"))
                                                    {
                                                        return true;
                                                    }
                                                    else
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Colon(\";\") at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                }
                                                else
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO after \"else\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }
                                            }
                                        }
                                        else
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"else\" after \"ALGO\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                    }
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected ALGO after \"then\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"COND\" after \"if\" word at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }

        }
    }

    private Boolean parseCOND(Node parent)
    {
        Node condNode = new Node(id++, "Non-Terminal", "COND");
        parent.children.add(condNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected COND after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            //consult
            int test = index + 2;
            if(test >= tokens.size())
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected at least 1 argument(s) in \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
            else if(Objects.equals(tokens.get(test).getType(), "VNAME") || Objects.equals(tokens.get(test).getType(), "CONST"))
            {
                if(Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
                Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
                Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
                Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div"))
                {
                    //Simple
                    Boolean simple = parseSIMPLE(condNode);
                    if(simple)
                    {
                        if(index >= tokens.size())
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                        //consult
                        else if(Objects.equals(tokens.get(index).getContent(), "then"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"COND\" after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"COND\" after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }

            }
            else
            {
                if(Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
                Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
                Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
                Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div") ||
                Objects.equals(tokens.get(index).getContent(), "not") || Objects.equals(tokens.get(index).getContent(), "sqrt"))
                {
                    //Composite
                    Boolean composite = parseCOMPOSIT(condNode);
                    if(composite)
                    {
                        if(index >= tokens.size())
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                        else if(Objects.equals(tokens.get(index).getContent(), "then"))
                        {
                            return true;
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"COND\" after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"COND\" after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }

            }
        }
    }

    private Boolean parseCOMPOSIT(Node parent)
    {
        Node compositeNode = new Node(id++, "Non-Terminal", "COMPOSIT");
        parent.children.add(compositeNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"or\" or \"and\" or \"eq\" or \"grt\" or \"not\" or \"sqrt\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
            Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
            Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
            Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div"))
            {
                Boolean binop = parseBINOP(compositeNode);
                if(binop)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"BINOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), "("))
                        {
                            Node openBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            compositeNode.children.add(openBracketNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SIMPLE\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                            else
                            {
                                if(Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
                                Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
                                Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
                                Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div"))
                                {
                                    Boolean simple = parseSIMPLE(compositeNode);
                                    if(simple)
                                    {
                                        if(index >= tokens.size())
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ATOMIC\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                        else
                                        {
                                            if(Objects.equals(tokens.get(index).getContent(), ","))
                                            {
                                                Node commaNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                compositeNode.children.add(commaNode);
                                                index++;

                                                if(index >= tokens.size())
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"Comma(,)\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }
                                                else
                                                {
                                                    if(Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
                                                    Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
                                                    Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
                                                    Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div"))
                                                    {
                                                        Boolean simple2 = parseSIMPLE(compositeNode);
                                                        if(simple2)
                                                        {
                                                            if(index >= tokens.size())
                                                            {
                                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Closing Bracket ')' \" after \"ATOMIC\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                System.exit(0);
                                                                return false;
                                                            }
                                                            else
                                                            {
                                                                if(Objects.equals(tokens.get(index).getContent(), ")"))
                                                                {
                                                                    Node closingBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                                    compositeNode.children.add(closingBracketNode);
                                                                    index++;

                                                                    if(index >= tokens.size())
                                                                    {
                                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"Closing Bracket ')' \" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                        System.exit(0);
                                                                        return false;
                                                                    }
                                                                    else
                                                                    {
                                                                        if(Objects.equals(tokens.get(index).getContent(), "then"))
                                                                        {
                                                                            return true;
                                                                        }
                                                                        else
                                                                        {
                                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                            System.exit(0);
                                                                            return false;
                                                                        }
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Closing Bracket ')' \" after \"SIMPLE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                    System.exit(0);
                                                                    return false;
                                                                }
                                                            }
                                                        }
                                                        else
                                                        {
                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SIMPLE\" after \"Comma(,)\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                            System.exit(0);
                                                            return false;
                                                        }
                                                    }
                                                    else
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SIMPLE\" after \"Comma(,)\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"SIMPLE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SIMPLE\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SIMPLE\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"BINOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected BINOP after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            //consult
            else if(Objects.equals(tokens.get(index).getContent(), "not") || Objects.equals(tokens.get(index).getContent(), "sqrt"))
            {
                Boolean unop = parseUNOP(compositeNode);
                if(unop)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"BINOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if (Objects.equals(tokens.get(index).getContent(), "("))
                        {
                            Node openBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            compositeNode.children.add(openBracketNode);
                            index++;

                            if (index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SIMPLE\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                            else
                            {
                                //Composite Unop(Simple)
                                //Simple => BINOP(ATOMIC,ATOMIC)
                                if(Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
                                Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
                                Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
                                Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div"))
                                {
                                    Boolean simple = parseSIMPLE(compositeNode);
                                    if (simple)
                                    {
                                        if(index >= tokens.size())
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Closing Bracket ')' \" after \"ATOMIC\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                        else
                                        {
                                            if(Objects.equals(tokens.get(index).getContent(), ")"))
                                            {
                                                Node closingBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                compositeNode.children.add(closingBracketNode);
                                                index++;

                                                if(index >= tokens.size())
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"Closing Bracket ')' \" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }
                                                else
                                                {
                                                    if(Objects.equals(tokens.get(index).getContent(), "then"))
                                                    {
                                                        return true;
                                                    }
                                                    else
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Closing Bracket ')' \" after \"SIMPLE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SIMPLE\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SIMPLE\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"UNOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"UNOP\" after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"or\" or \"and\" or \"eq\" or \"grt\" or \"not\" or \"sqrt\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseUNOP(Node parent)
    {
        Node unopNode = new Node(id++, "Non-Terminal", "UNOP");
        parent.children.add(unopNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"not\" or \"sqrt\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "not") || Objects.equals(tokens.get(index).getContent(), "sqrt"))
            {
                Node unaryNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                unopNode.children.add(unaryNode);
                index++;

                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"UNOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
                else
                {
                    if(Objects.equals(tokens.get(index).getContent(), "("))
                    {
                        return true;
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"UNOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"not\" or \"sqrt\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseSIMPLE(Node parent)
    {
        Node simpleNode = new Node(id++, "Non-Terminal", "SIMPLE");
        parent.children.add(simpleNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected COND after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
            Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
            Objects.equals(tokens.get(index).getContent(), "add") || Objects.equals(tokens.get(index).getContent(), "sub") ||
            Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div"))
            {
                Boolean binop = parseBINOP(simpleNode);
                if(binop)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"BINOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                    else
                    {
                        if(Objects.equals(tokens.get(index).getContent(), "("))
                        {
                            Node openBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            simpleNode.children.add(openBracketNode);
                            index++;

                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                            else
                            {
                                if(Objects.equals(tokens.get(index).getType(), "VNAME") || Objects.equals(tokens.get(index).getType(), "CONST"))
                                {
                                    Boolean atomic = parseATOMIC(simpleNode);
                                    if(atomic)
                                    {
                                        if(index >= tokens.size())
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ATOMIC\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                        else
                                        {
                                            if(Objects.equals(tokens.get(index).getContent(), ","))
                                            {
                                                Node commaNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                simpleNode.children.add(commaNode);
                                                index++;

                                                if(index >= tokens.size())
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"Comma(,)\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }
                                                else
                                                {
                                                    if(Objects.equals(tokens.get(index).getType(), "VNAME") || Objects.equals(tokens.get(index).getType(), "CONST"))
                                                    {
                                                        Boolean atomic2 = parseATOMIC(simpleNode);
                                                        if(atomic2)
                                                        {
                                                            if(index >= tokens.size())
                                                            {
                                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Closing Bracket ')' \" after \"ATOMIC\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                System.exit(0);
                                                                return false;
                                                            }
                                                            else
                                                            {
                                                                if(Objects.equals(tokens.get(index).getContent(), ")"))
                                                                {
                                                                    Node closingBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                                    simpleNode.children.add(closingBracketNode);
                                                                    index++;

                                                                    if(index >= tokens.size())
                                                                    {
                                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"Closing Bracket ')' \" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                        System.exit(0);
                                                                        return false;
                                                                    }
                                                                    else
                                                                    {
                                                                        //consult
                                                                        if(Objects.equals(tokens.get(index).getContent(), "then") ||
                                                                        Objects.equals(tokens.get(index).getContent(), ")")
                                                                        || Objects.equals(tokens.get(index).getContent(), ",")
                                                                        )
                                                                        {
                                                                           return true;
                                                                        }
                                                                        else
                                                                        {
                                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"then\" after \"COND\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                            System.exit(0);
                                                                            return false;
                                                                        }
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Closing Bracket ')' \" after \"ATOMIC\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                    System.exit(0);
                                                                    return false;
                                                                }
                                                            }
                                                        }
                                                        else
                                                        {
                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"Comma(,)\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                            System.exit(0);
                                                            return false;
                                                        }
                                                    }
                                                    else
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"Comma(,)\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"ATOMIC\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ATOMIC\" after \"Opening Bracket : (\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"BINOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected BINOP after \"if\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"or\" or \"and\" or \"eq\" or \"grt\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseBINOP(Node parent)
    {
        Node binopNode = new Node(id++, "Non-Terminal", "BINOP");
        parent.children.add(binopNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"or\" or \"and\" or \"eq\" or \"grt\" or \"sub\" or \"add\" or \"mul\" or \"div\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "or") || Objects.equals(tokens.get(index).getContent(), "and") ||
            Objects.equals(tokens.get(index).getContent(), "eq") || Objects.equals(tokens.get(index).getContent(), "grt") ||
            Objects.equals(tokens.get(index).getContent(), "sub") || Objects.equals(tokens.get(index).getContent(), "add") ||
            Objects.equals(tokens.get(index).getContent(), "mul") || Objects.equals(tokens.get(index).getContent(), "div"))
            {
                Node binaryNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                binopNode.children.add(binaryNode);
                index++;

                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"BINOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
                else
                {
                    if(Objects.equals(tokens.get(index).getContent(), "("))
                    {
                        return true;
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"Opening Bracket : (\" after \"BINOP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"or\" or \"and\" or \"eq\" or \"grt\" or \"sub\" or \"add\" or \"mul\" or \"div\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseATOMIC(Node parent)
    {
        Node atomicNode = new Node(id++, "Non-Terminal", "ATOMIC");
        parent.children.add(atomicNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" or \"CONST\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else if(Objects.equals(tokens.get(index).getType(), "VNAME"))
        {
            return parseVNAME(atomicNode);//consult
        }
        else if(Objects.equals(tokens.get(index).getType(), "CONST"))
        {
            Boolean constant = parseCONST(atomicNode);
            if(constant)
            {
                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") or \";\" or \")\" after CONST at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
                else if(Objects.equals(tokens.get(index).getContent(), ",") || Objects.equals(tokens.get(index).getContent(), ";") || Objects.equals(tokens.get(index).getContent(), ")"))
                {
                    return true;//consult
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\" ,\") or \";\" or \")\" after CONST at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"CONST\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" or \"CONST\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseCONST(Node parent)
    {
        Node constNode = new Node(id++, "Non-Terminal", "CONST");
        parent.children.add(constNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"CONST\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getType(), "CONST"))
            {
                Node printNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                constNode.children.add(printNode);
                index++;

                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") or \";\" or \")\" after CONST at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }

                if(Objects.equals(tokens.get(index).getContent(), ",") || Objects.equals(tokens.get(index).getContent(), ";") || Objects.equals(tokens.get(index).getContent(), ")"))
                {
                    return true;
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected Comma(\",\") or \";\" or \")\" after CONST at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }

            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"CONST\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseFUNCTIONS(Node parent)
    {
        Node functionsNode = new Node(id++, "Non-Terminal", "FUNCTIONS");
        parent.children.add(functionsNode);

        if(index >= tokens.size())
        {
            return true;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "end"))
            {
                return true;
            }

            Boolean decl = parseDECL(functionsNode);
            if(decl)
            {
                if(index >= tokens.size())
                {
                    return true;
                }
                else if(Objects.equals(tokens.get(index).getContent(), "end"))
                {
                    return true;
                }
                else if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void"))
                {
                    return parseFUNCTIONS(parent);
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"DECL\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"DECL\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseDECL(Node parent)
    {
        Node DeclNode = new Node(id++, "Non-Terminal", "DECL");
        parent.children.add(DeclNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"DECL\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void"))
            {
                Boolean header = parseHEADER(DeclNode);
                if(header)
                {
                    if(index >= tokens.size())
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"BODY\" after \"HEADER\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }

                    if(Objects.equals(tokens.get(index).getContent(), "{"))
                    {
                        Boolean body = parseBODY(DeclNode);
                        if(body)
                        {
                            if(index >= tokens.size())
                            {
                                return true;
                            }
                            else if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void")
                            || Objects.equals(tokens.get(index).getContent(), "end"))
                            {
                               return true;
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"num\",\"void\" or end of program at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"BODY\" after \"HEADER\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"{\" after \"HEADER\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }

                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"HEADER\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"num\" or \"void\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseBODY(Node parent)
    {
        Node BodyNode = new Node(id++, "Non-Terminal", "BODY");
        parent.children.add(BodyNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            Boolean prolog = parsePROLOG(BodyNode);
            if(prolog)
            {
                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }

                if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "text"))
                {
                    Boolean localvars = parseLOCALVARS(BodyNode);
                    if(localvars)
                    {
                        if(index >= tokens.size())
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ALGO\" after \"LOCALVARS\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }

                        if(Objects.equals(tokens.get(index).getContent(), "begin"))
                        {
                            Boolean algo = parseALGO(BodyNode);
                            if(algo)
                            {
                                if(index >= tokens.size())
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"EPILOG\" after \"ALGO\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }

                                if(Objects.equals(tokens.get(index).getContent(), "}"))
                                {
                                  Boolean epilog = parseEPILOG(BodyNode);
                                  if(epilog)
                                  {
                                      if(index >= tokens.size())
                                      {
                                          System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SUBFUNCT\" after \"ALGO\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                          System.exit(0);
                                          return false;
                                      }

                                      if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void") || Objects.equals(tokens.get(index).getContent(), "end"))
                                      {
                                          Boolean subfuncs = parseSUBFUNCTS(BodyNode);
                                          if(subfuncs)
                                          {
                                              if(index >= tokens.size())
                                              {
                                                  System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"end\" after \"SUBFUNCS\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                  System.exit(0);
                                                  return false;
                                              }

                                              if(Objects.equals(tokens.get(index).getContent(), "end"))
                                              {
                                                  Node endNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                  BodyNode.children.add(endNode);
                                                  index++;

                                                  if(index >= tokens.size())
                                                  {
                                                      return true;
                                                  }

                                                  if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void")
                                                  || Objects.equals(tokens.get(index).getContent(), "end"))
                                                  {
                                                      return true;
                                                  }
                                                  else
                                                  {
                                                      System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"num\" or \"void\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                      System.exit(0);
                                                      return false;
                                                  }
                                              }
                                              else
                                              {
                                                  System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                  System.exit(0);
                                                  return false;
                                              }
                                          }
                                          else
                                          {
                                              System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SUBFUNCS\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                              System.exit(0);
                                              return false;
                                          }
                                      }
                                      else
                                      {
                                          System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"void\" or \"num\" or \"end\"  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                          System.exit(0);
                                          return false;
                                      }
                                  }
                                  else
                                  {
                                      System.out.println("\u001B[31mParsing Error\u001B[0m: Expected EPILOG: } at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                      System.exit(0);
                                      return false;
                                  }
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected EPILOG: } at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }

                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"ALGO\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"begin\" after comma: \",\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"LOCALVARS\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }

                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"PROLOG: {\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
    }

    private Boolean parseSUBFUNCTS(Node parent)
    {
        Node subfuncs = new Node(id++, "Non-Terminal", "SUBFUNCS");
        parent.children.add(subfuncs);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FUNCS\" or \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

        if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void") ||
        Objects.equals(tokens.get(index).getContent(), "end"))
        {
            Boolean functions = parseFUNCTIONS(subfuncs);
            if(functions)
            {
                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FUNCS\" or \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
                else
                {
                    if(Objects.equals(tokens.get(index).getContent(), "end"))
                    {
                        return true;
                    }
                    else if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void"))
                    {
                        parseSUBFUNCTS(subfuncs);
                        return true;
                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FUNCS\" or \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FUNCTIONS at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FUNCS\" or \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseLOCALVARS(Node parent)
    {
        Node localVars = new Node(id++, "Non-Terminal", "LOCVARS");
        parent.children.add(localVars);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

        if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "text"))
        {
            Boolean vtype = parseVTYP(localVars);
            if(vtype)
            {
                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }

                if(Objects.equals(tokens.get(index).getType(), "VNAME"))
                {
                    Boolean vname = parseVNAME(localVars);
                    if(vname)
                    {
                        if(index >= tokens.size())
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }

                        if(Objects.equals(tokens.get(index).getContent(), ","))
                        {
                            Node commaNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                            localVars.children.add(commaNode);
                            index++;

                            ///
                            if(index >= tokens.size())
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }

                            if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "text"))
                            {
                                Boolean vtype2 = parseVTYP(localVars);
                                if(vtype2)
                                {
                                    if(index >= tokens.size())
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }

                                    if(Objects.equals(tokens.get(index).getType(), "VNAME"))
                                    {
                                        Boolean vname2 = parseVNAME(localVars);
                                        if(vname2)
                                        {
                                            if(index >= tokens.size())
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }

                                            if(Objects.equals(tokens.get(index).getContent(), ","))
                                            {
                                                Node commaNode2 = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                localVars.children.add(commaNode2);
                                                index++;

                                                ///
                                                if(index >= tokens.size())
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }

                                                if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "text"))
                                                {
                                                    Boolean vtype3 = parseVTYP(localVars);
                                                    if(vtype3)
                                                    {
                                                        if(index >= tokens.size())
                                                        {
                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                            System.exit(0);
                                                            return false;
                                                        }

                                                        if(Objects.equals(tokens.get(index).getType(), "VNAME"))
                                                        {
                                                            Boolean vname3 = parseVNAME(localVars);
                                                            if(vname3)
                                                            {
                                                                if(index >= tokens.size())
                                                                {
                                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                    System.exit(0);
                                                                    return false;
                                                                }

                                                                if(Objects.equals(tokens.get(index).getContent(), ","))
                                                                {
                                                                    Node commaNode3 = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                                    localVars.children.add(commaNode3);
                                                                    index++;

                                                                    ///
                                                                    if(index >= tokens.size())
                                                                    {
                                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"algo(begin)\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                        System.exit(0);
                                                                        return false;
                                                                    }

                                                                    if(Objects.equals(tokens.get(index).getContent(), "begin"))
                                                                    {
                                                                        return true;
                                                                    }
                                                                    else
                                                                    {
                                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"algo(begin)\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                        System.exit(0);
                                                                        return false;
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                    System.exit(0);
                                                                    return false;
                                                                }

                                                            }
                                                            else
                                                            {
                                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                System.exit(0);
                                                                return false;
                                                            }
                                                        }
                                                        else
                                                        {
                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                            System.exit(0);
                                                            return false;
                                                        }
                                                    }
                                                    else
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                }
                                                else
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }

                                        }
                                        else
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }
                        else
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }

                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
                else
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"VTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected VTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseHEADER(Node parent)
    {
        Node HeaderNode = new Node(id++, "Non-Terminal", "HEADER");
        parent.children.add(HeaderNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
        else
        {
            Boolean ftype = parseFTYP(HeaderNode);
            if(ftype)
            {
                if(index >= tokens.size())
                {
                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FNAME\" after \"FTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                    System.exit(0);
                    return false;
                }
                else
                {
                    Boolean fname = parseFNAME(HeaderNode);
                    if(fname)
                    {
                        if(index >= tokens.size())
                        {
                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FNAME\" after \"FTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                            System.exit(0);
                            return false;
                        }
                        else
                        {
                            if(Objects.equals(tokens.get(index).getContent(), "("))
                            {
                                Node openingBraceNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                HeaderNode.children.add(openingBraceNode);
                                index++;

                                if(index >= tokens.size())
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"FNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }

                                if(Objects.equals(tokens.get(index).getType(), "VNAME"))
                                {
                                    Boolean vname = parseVNAME(HeaderNode);
                                    if(vname)
                                    {
                                        if(index >= tokens.size())
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }

                                        if(Objects.equals(tokens.get(index).getContent(), ","))
                                        {
                                            Node commaNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                            HeaderNode.children.add(commaNode);
                                            index++;

                                            if(index >= tokens.size())
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \",\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }

                                            if(Objects.equals(tokens.get(index).getType(), "VNAME"))
                                            {
                                                Boolean vname2 = parseVNAME(HeaderNode);
                                                if(vname2)
                                                {
                                                    if(index >= tokens.size())
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }

                                                    if(Objects.equals(tokens.get(index).getContent(), ","))
                                                    {
                                                        Node commaNode2 = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                        HeaderNode.children.add(commaNode2);
                                                        index++;

                                                        if(index >= tokens.size())
                                                        {
                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \",\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                            System.exit(0);
                                                            return false;
                                                        }

                                                        if(Objects.equals(tokens.get(index).getType(), "VNAME"))
                                                        {
                                                            Boolean vname3 = parseVNAME(HeaderNode);
                                                            if(vname3)
                                                            {
                                                                if(index >= tokens.size())
                                                                {
                                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                    System.exit(0);
                                                                    return false;
                                                                }

                                                                if(Objects.equals(tokens.get(index).getContent(), ")"))
                                                                {
                                                                    Node closeBracketNode = new Node(id++, "Terminal", tokens.get(index).getContent());
                                                                    HeaderNode.children.add(closeBracketNode);
                                                                    index++;

                                                                    if(index >= tokens.size())
                                                                    {
                                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \",\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                        System.exit(0);
                                                                        return false;
                                                                    }

                                                                    if(Objects.equals(tokens.get(index).getContent(), "{"))
                                                                    {
                                                                        return true;
                                                                    }
                                                                    else
                                                                    {
                                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"{\" after \"Function DECL\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                        System.exit(0);
                                                                        return false;
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \")\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                    System.exit(0);
                                                                    return false;
                                                                }
                                                            }
                                                            else
                                                            {
                                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \",\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                                System.exit(0);
                                                                return false;
                                                            }
                                                        }
                                                        else
                                                        {
                                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \",\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                            System.exit(0);
                                                            return false;
                                                        }
                                                    }
                                                    else
                                                    {
                                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                        System.exit(0);
                                                        return false;
                                                    }
                                                }
                                                else
                                                {
                                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"FNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                    System.exit(0);
                                                    return false;
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"FNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                                System.exit(0);
                                                return false;
                                            }
                                        }
                                        else
                                        {
                                            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \",\" after \"VNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                            System.exit(0);
                                            return false;
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \",\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                        System.exit(0);
                                        return false;
                                    }
                                }
                                else
                                {
                                    System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VNAME\" after \"FNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                            }
                            else
                            {
                                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"(\" after \"FNAME\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                                System.exit(0);
                                return false;
                            }
                        }

                    }
                    else
                    {
                        System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FNAME\" after \"FTYPE\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                        System.exit(0);
                        return false;
                    }
                }
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FTYP\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }

    }

    private Boolean parsePROLOG(Node parent)
    {
        Node prologNode = new Node(id++, "Non-Terminal", "PROLOG");
        parent.children.add(prologNode);

        if (index >= tokens.size()) {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected PROLOG: { at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

        if(Objects.equals(tokens.get(index).getContent(), "{"))
        {
            Node openingBraceNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            prologNode.children.add(openingBraceNode);
            index++;

            if (index >= tokens.size()) {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected LOCALVARS  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }

            if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "text"))
            {
                return true;
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"VTYPE\" after \"{\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected PROLOG: { at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

    }

    private Boolean parseEPILOG(Node parent)
    {
        Node epilogNode = new Node(id++, "Non-Terminal", "EPILOG");
        parent.children.add(epilogNode);

        if (index >= tokens.size()) {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected EPILOG: }  at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

        if(Objects.equals(tokens.get(index).getContent(), "}"))
        {
            Node openingBraceNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            epilogNode.children.add(openingBraceNode);
            index++;

            if (index >= tokens.size()) {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"SUBFUNCTION\" or \"end\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }

            if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void") || Objects.equals(tokens.get(index).getContent(), "end"))
            {
                return true;
            }
            else
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"FUNCTIONS\" or \"end\" after \"}\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
                System.exit(0);
                return false;
            }
        }
        else
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected EPILOG: } at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

    }

    private Boolean parseFTYP(Node parent)
    {
        Node FTypNode = new Node(id++, "Non-Terminal", "FTYP");
        parent.children.add(FTypNode);

        if(index >= tokens.size())
        {
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }

        if(Objects.equals(tokens.get(index).getContent(), "num") || Objects.equals(tokens.get(index).getContent(), "void"))
        {
            Node TypeNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            FTypNode.children.add(TypeNode);
            index++;

            if(index >= tokens.size())
            {
                System.out.println("\u001B[31mParsing Error\u001B[0m: Expected FNAME after FTYPE at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
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
            System.out.println("\u001B[31mParsing Error\u001B[0m: Expected \"num\" or \"void\" at line " + tokens.get(index-1).getRow() + " col " + tokens.get(index-1).getCol());
            System.exit(0);
            return false;
        }
    }
}