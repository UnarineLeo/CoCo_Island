//For my implementation I used a Hashtable to store the symbol table.
//The key is the NodeID and the value is an array of Strings containing the variable name, type, scopeID and scopeName.
//I also used a List to store the variables that are called in the program.

//This implementation will first check if the variable is declared before it is used.
//It will then check if the variable is called after it is declared.
//so the error might displayed might be different from the expected output.

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Scoping
{
    public Hashtable<String, String[]> scopeTable = null;
    private List<String[]> calledList;
    public List<String[]> declaredList;
    private List<String[]> procList;
    private List<String[]> lastProc;
    private List<String[]> procArgs;

    public Scoping()
    {
        scopeTable = new Hashtable<String, String[]>();
        calledList = new ArrayList<String[]>();
        declaredList = new ArrayList<String[]>();
        procList = new ArrayList<String[]>();
        lastProc = new ArrayList<String[]>();
        procArgs = new ArrayList<String[]>();
    }

    public void Scope(Node root)
    {
        try
        {
            createTable(root, "global", 1, false);
            checkCalls(root, "global", 1, false);
            createHTMLTable();

            System.out.println();
            System.out.println("\u001B[32mSuccess\u001B[0m: Scoping Successful");
            System.out.println("View the SymbolTable.html file for the symbol table");

            TypeChecking(root, "global", 1, false);
            System.out.println();
            System.out.println("\u001B[32mSuccess\u001B[0m: Type Checking Successful");
            System.out.println("\u001B[32mSuccess\u001B[0m: Sementic Analysis Successful");

        } catch (Exception e) {
            System.out.println("\u001B[31mScoping Error: " + e.getMessage());
            System.exit(0);
        }
    }

    private void createTable(Node node, String currentScope, int currentScopeID, boolean isDeclaration)
    {
        if(node == null)
        {
            return;
        }

        if(node.getType().equals("Non-Terminal"))
        {
            if(node.getContent().equals("GLOBVARS") && !node.children.isEmpty())
            {
                String type = getType(node.children.getFirst());
                String var = getName(node.children.get(1));

                //disallow duplicate variable names in the same scope && type
                for(String[] val: declaredList)
                {
                    //Needs clarification if duplicates allowed in same scope but different types
                    if(val[0].equals(var) && val[2].equals(currentScope))
                    {
                        System.out.println("\u001B[31mSemantic Error: Variable \"" + var + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                }

                declaredList.add(new String[]{var, type, currentScope});
            }

            if(node.getContent().equals("LOCVARS"))
            {
                String type1 = getType(node.children.getFirst());
                String var1 = getName(node.children.get(1));

                String type2 = getType(node.children.get(3));
                String var2 = getName(node.children.get(4));

                String type3 = getType(node.children.get(6));
                String var3 = getName(node.children.get(7));

                //disallow duplicate variable names in the same scope && type
                for(String[] val: declaredList)
                {
                    //Needs clarification if duplicates allowed in same scope but different types
                    if(val[0].equals(var1) && val[2].equals(currentScope))
                    {
                        System.out.println("\u001B[31mSemantic Error: Variable \"" + var1 + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                    if(val[0].equals(var2) && val[2].equals(currentScope))
                    {
                        System.out.println("\u001B[31mSemantic Error: Variable \"" + var2 + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                    if(val[0].equals(var3) && val[2].equals(currentScope))
                    {
                        System.out.println("\u001B[31mSemantic Error: Variable \"" + var3 + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                }

                declaredList.add(new String[]{var1, type1, currentScope});
                declaredList.add(new String[]{var2, type2, currentScope});
                declaredList.add(new String[]{var3, type3, currentScope});
            }

            if(node.getContent().equals("VNAME"))
            {
                String var = getName(node);
                boolean found = false;
                for(String[] val: declaredList)
                {
                    if(val[0].equals(var))
                    {
                        if(isDeclaration)
                        {
                            //getLast() returns the last element in the list, so it's ready to accommodate situation where there are multiple variables with the same name
                            String[] value = {var, declaredList.getLast()[1], "Variable", String.valueOf(currentScopeID), currentScope};
                            scopeTable.put(String.valueOf(node.getId()), value);
                        }

                        found = true;
                        break;
                    }
                }

                if(!found)
                {
                    System.out.println("\u001B[31mSemantic Error: Variable \"" + var + "\" is being used before it is declared");
                    System.exit(0);
                }

            }
            else if(node.getContent().equals("CALL"))
            {
                //wait for spec, might have to remove it
                String var = getType(node.children.getFirst());

                if(calledList.isEmpty())
                {
                    calledList.add(new String[]{var});
                }
                else
                {
                    boolean found = false;
                    for(String[] val: calledList)
                    {
                        if(val[0].equals(var))
                        {
                            found = true;
                            break;
                        }
                    }

                    if(!found)
                    {
                        calledList.add(new String[]{var});
                    }
                }


                for(Node child: node.children)
                {
                    createTable(child, currentScope, currentScopeID, false);
                }
            }
            else if(node.getContent().equals("HEADER"))
            {
                String type = getType(node.children.getFirst());
                String var = getName(node.children.get(1));

                for(String[] val: procList)
                {
                    //are duplicates allowed? if in different scopes
                    if(val[0].equals(var) && val[2].equals(currentScope))
                    {
                        System.out.println("\u001B[31mSemantic Error: Function \"" + var + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                }

                addArgs(node, var);

                procList.add(new String[]{var, type, currentScope, String.valueOf(node.children.get(1).getId())});
                String[] value = {var, type, "Function", String.valueOf(currentScopeID), currentScope};
                scopeTable.put(String.valueOf(node.children.get(1).getId()), value);

                for(Node child: node.children)
                {
                    createTable(child, currentScope, currentScopeID, node.getContent().equals("LOCVARS"));
                }
            }
            else if(node.getContent().equals("BODY"))
            {
                String lastProc = procList.getLast()[0];
                int procId = Integer.parseInt(procList.getLast()[3]);

                for(Node child: node.children)
                {
                    createTable(child, lastProc, procId, node.getContent().equals("LOCVARS"));
                }
            }
            else
            {
                for(Node child: node.children)
                {
                    createTable(child, currentScope, currentScopeID, (node.getContent().equals("GLOBVARS") || node.getContent().equals("LOCVARS")));
                }
            }
        }
        else
        {
            createTable(null, currentScope, currentScopeID, false);
        }
    }

    private void checkCalls(Node node, String currentScope, int currentScopeID, boolean isDeclaration)
    {
        if(node == null)
        {
            return;
        }

        //might need a revamp, might have to move it up to createTable
        //to accomodate for situations whereby the call is inside a function
        //wait for spec
        if(node.getType().equals("Non-Terminal"))
        {
            if(node.getContent().equals("CALL"))
            {
                String var = getType(node.children.getFirst());

                boolean found = false;
                for(String[] val: procList)
                {
                    //consult
                    if(val[0].equals(var))
                    {
                        found = true;
                        break;
                    }
                }

                if(!found)
                {
                    System.out.println("\u001B[31mSemantic Error: Function \"" + var + "\" is being called while it is not defined");
                    System.exit(0);
                }

                for(Node child: node.children)
                {
                    createTable(child, currentScope, currentScopeID, false);
                }
            }
            else if(node.getContent().equals("HEADER"))
            {
                //might remove this
                String fun = getName(node.children.get(1));

                //consult
                boolean found = false;
                for(String[] val: calledList)
                {
                    if(val[0].equals(fun))
                    {
                        found = true;
                        break;
                    }
                }

                //wait for spec
                if(!found)
                {
                    System.out.println("\u001B[31mSemantic Error: Function \"" + fun + "\" is defined but is never called");
                    System.exit(0);
                }

                //caters for if duplicate function names are not allowed


                for(Node child: node.children)
                {
                    createTable(child, currentScope, currentScopeID, false);
                }
            }
            else
            {
                for(Node child: node.children)
                {
                    checkCalls(child, currentScope, currentScopeID, false);
                }
            }
        }
        else
        {
            checkCalls(null, currentScope, currentScopeID, false);
        }

    }

    private void TypeChecking(Node node,String currentScope, int currentScopeID, boolean isDeclaration)
    {
        if(node == null)
        {
            return;
        }

        if(node.getType().equals("Non-Terminal"))
        {
            if(node.getContent().equals("ASSIGN") && !node.children.get(1).getContent().equals("< input"))
            {
                Node variableNode = node.children.getFirst();
                Node termChild = node.children.get(2).children.getFirst();

                String leftType = "";

                for(String[] val: declaredList)
                {
                    if(val[0].equals(getName(variableNode)) && val[2].equals(currentScope))
                    {
                        leftType = val[1];
                        break;
                    }
                }

                if(termChild.getContent().equals("ATOMIC"))
                {
                    String rightType = "";
                    Node rightValue = termChild.children.getFirst().children.getFirst();

                    if(rightValue.getContent().charAt(0) == 'V')
                    {
                        for(String[] val: declaredList)
                        {
                            if(val[0].equals(rightValue.getContent()) && val[2].equals(currentScope))
                            {
                                rightType = val[1];
                                break;
                            }
                        }
                    }

                    if(rightValue.getContent().charAt(0) == '"' || rightType.equals("text"))
                    {
                        if(leftType.equals("num"))
                        {
                            System.out.println("\u001B[31mSemantic Error: The variable \"" + getName(variableNode) + "\" which is a num cannot be assigned to a text");
                            System.exit(0);
                        }
                    }
                    else
                    {
                        if(leftType.equals("text"))
                        {
                            System.out.println("\u001B[31mSemantic Error: The variable \"" + getName(variableNode) + "\" which is a text cannot be assigned to a num");
                            System.exit(0);
                        }
                    }

                }
                else if(termChild.getContent().equals("OP"))
                {

                }
                else if(termChild.getContent().equals("CALL"))
                {
                    Node functionNode = termChild.children.getFirst();
                    //Problem: Allow same function names???
                    if(leftType.equals("num"))
                    {
                        //look for function type
                        String functionType = "";
                        for(String[] val: procList)
                        {
                            if(val[0].equals(getName(functionNode)))
                            {
                                functionType = val[1];
                                break;
                            }
                        }

                        if(!functionType.equals("num"))
                        {
                            System.out.println("\u001B[31mSemantic Error: The function \"" + getName(functionNode) + "\" is supposed to return a num");
                            System.exit(0);
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mSemantic Error: The variable \"" + getName(variableNode) + "\" is supposed to be num so that we can assign it with a num function");
                        System.exit(0);
                    }

                    //verify the args
                    verifyArgs(getName(functionNode), termChild);
                }
                else
                {
                    for(Node child : node.children)
                    {
                        TypeChecking(child, currentScope, currentScopeID, false);
                    }
                }
            }
            else if(node.getContent().equals("COMMAND"))
            {
                Node callNode = node.children.getFirst();
                if(callNode.getContent().equals("CALL"))
                {
                    Node functionNode = callNode.children.getFirst();

                    String functionType = "";
                    for(String[] val: procList)
                    {
                        if(val[0].equals(getName(functionNode)))
                        {
                            functionType = val[1];
                            break;
                        }
                    }

                    if(!functionType.equals("void"))
                    {
                        System.out.println("\u001B[31mSemantic Error: The function \"" + getName(functionNode) + "\" is supposed to return a void");
                        System.exit(0);
                    }

                    //verify the args
                    verifyArgs(getName(functionNode), callNode);
                }
            }
            else if(node.getContent().equals("HEADER"))
            {
                String type = getType(node.children.getFirst());
                String var = getName(node.children.get(1));

                lastProc.add(new String[]{var, type, currentScope, String.valueOf(node.children.get(1).getId())});

                for(Node child: node.children)
                {
                    TypeChecking(child, currentScope, currentScopeID, false);
                }
            }
            else if(node.getContent().equals("BODY"))
            {
                String currScope = lastProc.getLast()[0];
                int procId = Integer.parseInt(lastProc.getLast()[3]);

                for(Node child: node.children)
                {
                    TypeChecking(child, currScope, procId, false);
                }
            }
            else
            {
                for(Node child : node.children)
                {
                    TypeChecking(child, currentScope, currentScopeID, false);
                }

            }
        }
        else
        {
            TypeChecking(null, currentScope, currentScopeID, false);
        }
    }

    public String getName(Node node)
    {
        //must be on VNAME OR FNAME
        return node.children.getFirst().getContent();
    }

    public String getType(Node node)
    {
        //must be on VTYPE OR FTYPE
        return node.children.getFirst().getContent();
    }

    private void addArgs(Node node, String fun)
    {
        //node must be call
        String var1 = getName(node.children.get(3));
        String var2 = getName(node.children.get(5));
        String var3 = getName(node.children.get(7));

        String scope = "";
        if(procList.isEmpty())
        {
            scope = "global";
        }
        else
        {
            scope = procList.getLast()[0];
        }

        procArgs.add(new String[]{var1, var2, var3, fun, scope});
    }

    private void verifyArgs(String func, Node callNode)
    {
        //node must be call
    }

    private void createHTMLTable()
    {
        try
        {
            FileWriter fileWriter = new FileWriter("SymbolTable.html");

            fileWriter.write("<!DOCTYPE html>\n");
            fileWriter.write("<html lang=\"en\">\n");
            fileWriter.write("<head>\n");
            fileWriter.write("<meta charset=\"UTF-8\">\n");
            fileWriter.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            fileWriter.write("<title>Symbol Table</title>\n");
            fileWriter.write("</head>\n");

            fileWriter.write("<style>\n");
            fileWriter.write("table, th, td {\n");
            fileWriter.write("border: 1px solid black;\n");
            fileWriter.write("}\n");
            fileWriter.write("</style>\n");

            fileWriter.write("<body>\n");
            fileWriter.write("<table>\n");
            fileWriter.write("<tr>\n");
            fileWriter.write("<th>NodeID</th>\n");
            fileWriter.write("<th>NodeName</th>\n");
            fileWriter.write("<th>NodeType</th>\n");
            fileWriter.write("<th>Class</th>\n");
            fileWriter.write("<th>ScopeID</th>\n");
            fileWriter.write("<th>ScopeName</th>\n");
            fileWriter.write("</tr>\n");

            for(String key: scopeTable.keySet()){
                String[] value = scopeTable.get(key);
                fileWriter.write("<tr>\n");
                fileWriter.write("<td>" + key + "</td>\n");
                fileWriter.write("<td>" + value[0] + "</td>\n");
                fileWriter.write("<td>" + value[1] + "</td>\n");
                fileWriter.write("<td>" + value[2] + "</td>\n");
                fileWriter.write("<td>" + value[3] + "</td>\n");
                fileWriter.write("<td>" + value[4] + "</td>\n");
                fileWriter.write("</tr>\n");
            }

            fileWriter.write("</table>\n");
            fileWriter.write("</body>\n");
            fileWriter.write("</html>\n");

            fileWriter.close();

        }catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

}