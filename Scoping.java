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
    private List<String> scopeOrder;

    public Scoping()
    {
        scopeTable = new Hashtable<String, String[]>();
        calledList = new ArrayList<String[]>();
        declaredList = new ArrayList<String[]>();
        procList = new ArrayList<String[]>();
        lastProc = new ArrayList<String[]>();
        procArgs = new ArrayList<String[]>();
        scopeOrder = new ArrayList<String>();
    }

    public void Scope(Node root)
    {
        try
        {
            scopeOrder.add("main");
            procList.add(new String[]{"main", "void", "global", "0"});

            createTable(root, "main", 1, false);
            checkCalls(root, "main", 1, false);
            createHTMLTable();

            System.out.println();
            System.out.println("\u001B[32mSuccess\u001B[0m: Scoping Successful");
            System.out.println("View the SymbolTable.html file for the symbol table");

            TypeChecking(root, "main", 1, false);
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
                if(!var1.equals(var2) && !var1.equals(var3) && !var2.equals(var3))
                {
                    declaredList.add(new String[]{var1, type1, currentScope});
                    declaredList.add(new String[]{var2, type2, currentScope});
                    declaredList.add(new String[]{var3, type3, currentScope});
                }
                else
                {
                    if(var1.equals(var2))
                    {
                        System.out.println("\u001B[31mSemantic Error: Variable \"" + var1 + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                    else if(var1.equals(var3))
                    {
                        System.out.println("\u001B[31mSemantic Error: Variable \"" + var1 + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                    else
                    {
                        System.out.println("\u001B[31mSemantic Error: Variable \"" + var2 + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                }
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

//                for(int i = (declaredList.size()-1); i >= 0; i--)
//                {
//                    if(declaredList.get(i)[0].equals(var) && declaredList.get(i)[2].equals(currentScope))
//                    {
//                        if(isDeclaration)
//                        {
//                            //getLast() returns the last element in the list, so it's ready to accommodate situation where there are multiple variables with the same name
//                            String[] value = {var, declaredList.get(i)[1], "Variable", String.valueOf(currentScopeID), currentScope};
//                            scopeTable.put(String.valueOf(node.getId()), value);
//                        }
//
//                        found = true;
//                        break;
//                    }
//                }

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

                if(var.equals("main"))
                {
                    System.out.println("\u001B[31mSemantic Error: There should be no recursive calls to the main function");
                    System.exit(0);
                }

                if(var.equals(currentScope))
                {
                    System.out.println("\u001B[31mSemantic Error: Function \"" + var + "\" is being declared with the same name as the current scope");
                    System.exit(0);
                }

                for(String[] val: procList)
                {
                    //are duplicates allowed? if in different scopes
                    if(val[0].equals(var) && val[2].equals(currentScope))
                    {
                        System.out.println("\u001B[31mSemantic Error: Function \"" + var + "\" is being declared more than once in the same scope");
                        System.exit(0);
                    }
                }

                addFuncArgs(node, var);
                scopeOrder.add(var);

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
                boolean checkIfImmediate = false;
                for(String[] val: procList)
                {
                    if(var.equals(currentScope))
                    {
                        found = true;
                        break;
                    }

                    if(checkIfImmediate)
                    {
                        if(val[0].equals(var) && val[2].equals(currentScope))
                        {
                            found = true;
                            checkIfImmediate = false;
                            break;
                        }
                        else
                        {
                            System.out.println("\u001B[31mSemantic Error: Function call of \"" + var + "\" under the scope of \"" + currentScope + "\" should be to an immediate child function or recursive");
                            System.exit(0);
                        }
                    }

                    if(val[0].equals(currentScope))
                    {
                        checkIfImmediate = true;
                    }

                }

                if(!found)
                {
                    System.out.println("\u001B[31mSemantic Error: Function call of \"" + var + "\" under the scope of \"" + currentScope + "\" should be to an immediate child function or recursive");
                    System.exit(0);
                }

                for(Node child: node.children)
                {
                    createTable(child, currentScope, currentScopeID, false);
                }
            }
            else if(node.getContent().equals("BODY"))
            {
                int soonToBeScope = 0;
                for(int i = 0; i < procList.size(); i++)
                {
                    if(procList.get(i)[0].equals(currentScope))
                    {
                        soonToBeScope = i+1;
                        break;
                    }
                }

                String lastProc = procList.get(soonToBeScope)[0];
                int procId = Integer.parseInt(procList.get(soonToBeScope)[3]);

                for(Node child: node.children)
                {
                    checkCalls(child, lastProc, procId, false);
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

                    if(leftType.equals("text"))
                    {
                        System.out.println("\u001B[31mSemantic Error: The variable \"" + getName(variableNode) + "\" which is a text cannot be assigned using a numerical operation");
                        System.exit(0);
                    }

                    CheckARG(termChild, currentScope);

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
                    verifyFuncArgs(getName(functionNode), termChild, currentScope);

                }
                else
                {
                    for(Node child : node.children)
                    {
                        TypeChecking(child, currentScope, currentScopeID, false);
                    }
                }

            }
            else if(node.getContent().equals("COND"))
            {
                CheckCOND(node.children.getFirst(), currentScope);
            }
            else if(node.getContent().equals("COMMAND") && node.children.getFirst().getContent().equals("CALL"))
            {
                Node callNode = node.children.getFirst();

                Node functionNode = callNode.children.getFirst();

                String functionType = "";
                for(String[] val: procList)
                {
                    if(val[0].equals(getName(functionNode)) && val[3].equals(String.valueOf(currentScopeID)))
                    {
                        functionType = val[1];
                        break;
                    }
                }

                if(!functionType.equals("void"))
                {
                    System.out.println("\u001B[31mSemantic Error: The function \"" + getName(functionNode) + "\" is supposed to be a void function");
                    System.exit(0);
                }

                //verify the args
                verifyFuncArgs(getName(functionNode), callNode, currentScope);
            }
            else if(node.getContent().equals("COMMAND") && node.children.getFirst().getContent().equals("return"))
            {
                if(currentScope.equals("main"))
                {
                    System.out.println("\u001B[31mSemantic Error: The main function is not supposed to return anything");
                    System.exit(0);
                }

                String var = node.children.get(1).children.getFirst().getContent();
                Node grandchild = node.children.get(1).children.getFirst();
                if(!var.equals("CONST"))
                {
                    String name = getName(grandchild);
                    for(String[] val: declaredList)
                    {
                        if(val[0].equals(name) && val[2].equals(currentScope))
                        {
                            if(val[1].equals("text"))
                            {
                                System.out.println("\u001B[31mSemantic Error: The function \"" + currentScope + "\" is supposed to return a numeric value");
                                System.exit(0);
                            }
                            break;
                        }
                    }
                }
                else
                {
                    if (Character.isDigit(grandchild.children.getFirst().getContent().charAt(0)))
                    {
                        for(String[] val: procList)
                        {
                            //mbilu kuvhavha
                            if(val[0].equals(currentScope) && val[3].equals(String.valueOf(currentScopeID)))
                            {
                                if(!val[1].equals("num"))
                                {
                                    System.out.println("\u001B[31mSemantic Error: The function \"" + currentScope + "\" cannot have a return command since it is void function");
                                    System.exit(0);
                                }
                            }
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mSemantic Error: The function \"" + currentScope + "\" cannot have a return TEXT");
                        System.exit(0);
                    }
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
                String funcScope = lastProc.getLast()[0];
                int procId = Integer.parseInt(lastProc.getLast()[3]);

                for(Node child: node.children)
                {
                    TypeChecking(child, funcScope, procId, false);
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

    private void addFuncArgs(Node node, String fun)
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

    private void verifyFuncArgs(String func, Node callNode, String callScope)
    {
        //node must be call, this implementation assumes that only 1 unique name allowed
        String[] toVerify = {};

        Node call1 = callNode.children.get(2).children.getFirst().children.getFirst();
        Node call2 = callNode.children.get(4).children.getFirst().children.getFirst();
        Node call3 = callNode.children.get(6).children.getFirst().children.getFirst();

        String typeCall1 = "";
        String typeCall2 = "";
        String typeCall3 = "";

        Node[] calls = {call1, call2, call3};
        String[] callTypes = {typeCall1, typeCall2, typeCall3};

        int callIndex = 0;

        for(int i = (procArgs.size()-1); i >= 0; i--)
        {
            if(procArgs.get(i)[4].equals(callScope))
            {
                callIndex = i;
            }
        }

        for(int i = 0; i < calls.length; i++)
        {
            if(calls[i].getContent().charAt(0) == 'V')
            {
                for(int j = callIndex; j >= 0; j--)
                {
                    for(String[] val: declaredList)
                    {
                        if(val[0].equals(calls[i].getContent()) && val[2].equals(procArgs.get(j)[4]))
                        {
                            callTypes[i] = val[1];
                            break;
                        }
                    }

                    if(!callTypes[i].isEmpty())
                    {
                        break;
                    }
                }
            }
            else if(calls[i].getContent().charAt(0) == '"')
            {
                callTypes[i] = "text";
            }
            else
            {
                callTypes[i] = "num";
            }
        }

        int index = 0;
        for(String[] val: procArgs)
        {
            if(val[3].equals(func))
            {
                toVerify = val;
                index = procArgs.indexOf(val);
                break;
            }
        }

        String arg1 = toVerify[0];
        String arg2 = toVerify[1];
        String arg3 = toVerify[2];

        if(toVerify[4].equals("global"))
        {
            boolean pass1 = false;
            boolean pass2 = false;
            boolean pass3 = false;

            for(String[] val: declaredList)
            {
                if(val[0].equals(arg1) && val[2].equals("global"))
                {
                    pass1 = true;
                    if(!val[1].equals(callTypes[0]))
                    {
                        System.out.println("\u001B[31mSemantic Error: The function \"" + func + "\" is supposed to take a " + val[1] + " as the first argument");
                        System.exit(0);
                    }
                }

                if(val[0].equals(arg2) && val[2].equals("global"))
                {
                    pass2 = true;
                    if(!val[1].equals(callTypes[1]))
                    {
                        System.out.println("\u001B[31mSemantic Error: The function \"" + func + "\" is supposed to take a " + val[1] + " as the second argument");
                        System.exit(0);
                    }
                }

                if(val[0].equals(arg3) && val[2].equals("global"))
                {
                    pass3 = true;
                    if(!val[1].equals(callTypes[2]))
                    {
                        System.out.println("\u001B[31mSemantic Error: The function \"" + func + "\" is supposed to take a " + val[1] + " as the third argument");
                        System.exit(0);
                    }
                }

                if(pass1 && pass2 && pass3)
                {
                    break;
                }
            }
        }
        else
        {
            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            /*
            for(int i = index; i >= 0; i--)
            {
                for(String[] val: declaredList)
                {
                    if(val[0].equals(arg1) && val[2].equals(procArgs.get(i)[4]) && !found1)
                    {
                        if (!val[1].equals(callTypes[0]))
                        {
                            System.out.println("\u001B[31mSemantic Error: The function \"" + func + "\" is supposed to take a " + val[1] + " as the first argument");
                            System.exit(0);
                        }
                        found1 = true;
                    }

                    if(val[0].equals(arg2) && val[2].equals(procArgs.get(i)[4]) && !found2)
                    {
                        if (!val[1].equals(callTypes[1]))
                        {
                            System.out.println("\u001B[31mSemantic Error: The function \"" + func + "\" is supposed to take a " + val[1] + " as the second argument");
                            System.exit(0);
                        }
                        found2 = true;
                    }

                    if(val[0].equals(arg3) && val[2].equals(procArgs.get(i)[4]) && !found3)
                    {
                        if (!val[1].equals(callTypes[2]))
                        {
                            System.out.println("\u001B[31mSemantic Error: The function \"" + func + "\" is supposed to take a " + val[1] + " as the third argument");
                            System.exit(0);
                        }
                        found3 = true;
                    }
                }
            }

             */

        }
        boolean done = true;


    }

    private void CheckARG(Node node, String currentScope)
    {
        //Node must be OP || ATOMIC

        if(node.getContent().equals("OP"))
        {
            Node argChild = node.children.getFirst();

            if(argChild.getContent().equals("BINOP"))
            {
                Node exactBinop = argChild.children.getFirst();

                if(Objects.equals(exactBinop.getContent(), "add") || Objects.equals(exactBinop.getContent(), "sub") ||
                Objects.equals(exactBinop.getContent(), "mul") || Objects.equals(exactBinop.getContent(), "div")
                )
                {
                    CheckARG(node.children.get(2).children.getFirst(), currentScope);
                    CheckARG(node.children.get(4).children.getFirst(), currentScope);
                }
                else
                {
                    System.out.println("\u001B[31mSemantic Error: Expected \"add\", \"sub\", \"mul\" or \"div\" but got \"" + exactBinop.getContent() + "\" when assigning a value to a num variable");
                    System.exit(0);
                }


            }
            else
            {
                Node exactBinop = argChild.children.getFirst();

                if(Objects.equals(exactBinop.getContent(), "sqrt"))
                {
                    CheckARG(node.children.get(2).children.getFirst(), currentScope);
                }
                else
                {
                    System.out.println("\u001B[31mSemantic Error: Expected \"sqrt\" but got \"" + exactBinop.getContent() + "\" when assigning a value to a num variable");
                    System.exit(0);
                }

            }
        }

        if(node.getContent().equals("ATOMIC"))
        {
            Node value = node.children.getFirst().children.getFirst();
            String atomicType = "";
            if(value.getContent().charAt(0) == 'V')
            {
                int index = scopeOrder.indexOf(currentScope);
                for(int i = index; i >= 0; i--)
                {
                    for(String[] val: declaredList)
                    {
                        if(val[0].equals(value.getContent()) && val[2].equals(scopeOrder.get(i)))
                        {
                            atomicType = val[1];
                            break;
                        }
                    }

                    if(!atomicType.isEmpty())
                    {
                        break;
                    }
                }
            }
            else if(value.getContent().charAt(0) == '"')
            {
                atomicType = "text";
            }
            else
            {
                atomicType = "num";
            }

            if(atomicType.equals("text"))
            {
                System.out.println("\u001B[31mSemantic Error: Assignment of a numeric value only works with a num and/or numerical operations");
                System.exit(0);
            }
        }
    }

    private void CheckCOND(Node node, String currentScope)
    {
        //Node must be OP || ATOMIC

        if(node.getContent().equals("SIMPLE") || node.getContent().equals("COMPOSIT"))
        {
            Node argChild = node.children.getFirst();

            if(argChild.getContent().equals("BINOP"))
            {
                Node exactBinop = argChild.children.getFirst();

                if(Objects.equals(exactBinop.getContent(), "or") || Objects.equals(exactBinop.getContent(), "and") ||
                        Objects.equals(exactBinop.getContent(), "eq") || Objects.equals(exactBinop.getContent(), "grt")
                )
                {
                    CheckCOND(node.children.get(2), currentScope);
                    CheckCOND(node.children.get(4), currentScope);
                }
                else
                {
                    System.out.println("\u001B[31mSemantic Error: Expected \"or\", \"and\", \"eq\" or \"grt\" but got \"" + exactBinop.getContent() + "\" when looking for a boolean value");
                    System.exit(0);
                }


            }
            else
            {
                Node exactBinop = argChild.children.getFirst();

                if(Objects.equals(exactBinop.getContent(), "not"))
                {
                    CheckCOND(node.children.get(2), currentScope);
                }
                else
                {
                    System.out.println("\u001B[31mSemantic Error: Expected \"not\" but got \"" + exactBinop.getContent() + "\" when looking for a boolean value");
                    System.exit(0);
                }

            }
        }

        if(node.getContent().equals("ATOMIC"))
        {
            Node value = node.children.getFirst().children.getFirst();
            String atomicType = "";
            if(value.getContent().charAt(0) == 'V')
            {
                int index = scopeOrder.indexOf(currentScope);
                for(int i = index; i >= 0; i--)
                {
                    for(String[] val: declaredList)
                    {
                        if(val[0].equals(value.getContent()) && val[2].equals(scopeOrder.get(i)))
                        {
                            atomicType = val[1];
                            break;
                        }
                    }

                    if(!atomicType.isEmpty())
                    {
                        break;
                    }
                }
            }
            else if(value.getContent().charAt(0) == '"')
            {
                atomicType = "text";
            }
            else
            {
                atomicType = "num";
            }

            if(atomicType.equals("text"))
            {
                System.out.println("\u001B[31mSemantic Error: Expected a numerical value while doing a CONDITION of an if statement");
                System.exit(0);
            }
        }
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