import java.io.*;
import java.util.*;

public class Translation
{
    private String code="";
    int lineNumber = 10;
    Scoping scope = null;
    int currentProcId;
    boolean inLoop = false;
    boolean inBranch = false;
    int branchCount = 0;
    int loopCount = 0;
    int scopeNumber = 1;
    String logicExpr = "";
    String currScope[] = new String[2];

    public void Translate(Node root, Scoping scopeDefined)
    {
        currentProcId = 0;
        scope = scopeDefined;
        translate(root, scopeNumber, "main");
        createBASICFile();
        System.out.println("BASIC.txt file created successfully!");
    }

    private void translate(Node node, int currentScopeID, String currentProc)
    {
        if(node == null)
        {
            return;
        }

        if(node.getContent().equals("FNAME"))
        {
            String funcName = scope.getName(node);
            String scopeNameID = "";
            for(int i = 0; i < scope.scopeTable.size(); i++)
            {
                if(!scopeNameID.isEmpty())
                {
                    break;
                }

                if(scope.scopeTable.get(i)[0].equals(funcName) && scope.scopeTable.get(i)[4].equals(currentProc) && scope.scopeTable.get(i)[3].equals(String.valueOf(currentScopeID)))
                {
                    funcName = scope.scopeTable.get(i)[5];
                    for(String key: scope.scopeTable.keySet())
                    {
                        if(scope.scopeTable.get(key)[0].equals(funcName) && scope.scopeTable.get(key)[4].equals(currentProc) && scope.scopeTable.get(key)[3].equals(String.valueOf(currentScopeID)))
                        {
                            scopeNameID = key;
                            break;
                        }
                    }
                }
            }

            currScope[0] = funcName;
            currScope[1] = scopeNameID;
        }
        else if(node.getContent().equals("BODY"))
        {
            for(int i = 0; i < node.children.size(); i++)
            {
                translate(node.children.get(i), Integer.parseInt(currScope[1]), currScope[0]);
            }
        }
        else if(node.getContent().equals("COMMAND"))
        {
            if(node.children.getFirst().getContent().equals("skip"))
            {
                code += String.valueOf(lineNumber) + "REM DO NOTHING";
                lineNumber += 10;
            }
            else if(node.children.getFirst().getContent().equals("halt"))
            {
                code += String.valueOf(lineNumber) + "STOP";
                lineNumber += 10;
            }
            else if(node.children.getFirst().getContent().equals("print"))
            {
                TransPrint(node, currentProc, currentScopeID);
            }
            else if(node.children.getFirst().getContent().equals("return"))
            {
                code += String.valueOf(lineNumber) + "RETURN" + "\n";
                lineNumber += 10;
            }
            else if(node.children.getFirst().getContent().equals("ASSIGN"))
            {
                TransASSIGN(node, currentProc, currentScopeID);
            }
            else if(node.children.getFirst().getContent().equals("CALL"))
            {
//                TransPrint(node, currentProc, currentScopeID);
            }
            else if(node.children.getFirst().getContent().equals("BRANCH"))
            {
//                TransPrint(node, currentProc, currentScopeID);
            }
            else
            {

            }
        }
        else
        {
            for(int i = 0; i < node.children.size(); i++)
            {
                translate(node.children.get(i), currentScopeID, currentProc);
            }
        }

    }

    private void TransASSIGN(Node node, String currentProc, int currentScopeID)
    {
        Node isItInput = node.children.get(1);
        Node isItTerm = node.children.get(2);
        if(isItInput.getContent().equals("< input"))
        {
            String varName = scope.getName(node.children.getFirst());
            for(int i = 0; i < scope.scopeTable.size(); i++)
            {
                if(scope.scopeTable.get(i)[0].equals(varName) && scope.scopeTable.get(i)[4].equals(currentProc) && scope.scopeTable.get(i)[3].equals(String.valueOf(currentScopeID)))
                {
                    varName = scope.scopeTable.get(i)[5];
                }
            }

            code += String.valueOf(lineNumber) + " INPUT \"\";" + varName + "\n";
            lineNumber += 10;
        }
        else
        {
            String varName = scope.getName(node.children.getFirst());
            for(int i = 0; i < scope.scopeTable.size(); i++)
            {
                if(scope.scopeTable.get(i)[0].equals(varName) && scope.scopeTable.get(i)[4].equals(currentProc) && scope.scopeTable.get(i)[3].equals(String.valueOf(currentScopeID)))
                {
                    varName = scope.scopeTable.get(i)[5];
                }
            }

            code += String.valueOf(lineNumber) + " LET " + varName + " = " +  TransTERM(isItTerm, currentProc, currentScopeID) + "\n";
            lineNumber += 10;
        }
    }

    private String TransTERM(Node node, String currentProc, int currentScopeID)
    {
        String value = "";
        Node firstChild = node.children.getFirst();
        if(firstChild.getContent().equals("ATOMIC"))
        {
            value = TransATOMIC(firstChild, currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("OP"))
        {
            //fixing the issue
            value = TransTERM(firstChild, currentProc, currentScopeID);
        }
        else
        {
            //fixing the issue[CALL]
            value = TransTERM(firstChild, currentProc, currentScopeID);
        }

        return value;
    }

    private void TransPrint(Node node, String currentProc, int currentScopeID)
    {
        Node atomicNode = node.children.get(1);
        String value = TransATOMIC(atomicNode, currentProc, currentScopeID);
        code += String.valueOf(lineNumber) + "PRINT " + value + "\n";
        lineNumber += 10;
    }

    private String TransATOMIC(Node node, String currentProc, int currentScopeID)
    {
        //on ATOMIC
        Node firstChild = node.children.getFirst();
        String value = "";
        if(firstChild.getContent().equals("VNAME"))
        {
            String name = scope.getName(firstChild);
            for(int i = 0; i < scope.scopeTable.size(); i++)
            {
                if(scope.scopeTable.get(i)[0].equals(name) && scope.scopeTable.get(i)[4].equals(currentProc) && scope.scopeTable.get(i)[3].equals(String.valueOf(currentScopeID)))
                {
                    value = scope.scopeTable.get(i)[5];
                }
            }
        }
        else
        {
            Node constChild = firstChild.children.getFirst();
            if(constChild.getContent().charAt(0) == '"')
            {
                value = "\"" + constChild.getContent() + "\"";
            }
            else
            {
                value = String.valueOf(constChild.getContent());
            }
        }

        return value;
    }

    private void TransCOND(Node node, String currentProc, int currentScopeID)
    {
        //on COND
        Node firstChild = node.children.getFirst();
        if(firstChild.getContent().equals("SIMPLE"))
        {
//            TransSIMPLE(firstChild, currentProc);
        }
        else
        {
//            TransCOMPOSIT(firstChild, currentProc);
        }

    }

    private String TransBINOP(Node node, String currentProc, int currentScopeID)
    {
        //on the symbol : > + - * /
        String value = "";
        if(node.getContent().equals("eq"))
        {
            value = " = ";
        }
        else if(node.getContent().equals("grt"))
        {
            value = " > ";
        }
        else if(node.getContent().equals("add"))
        {
            value = " + ";
        }
        else if(node.getContent().equals("sub"))
        {
            value = " - ";
        }
        else if(node.getContent().equals("mul"))
        {
            value = " * ";
        }
        else if(node.getContent().equals("div"))
        {
            value = " / ";
        }
        else if(node.getContent().equals("or"))
        {
            //OR
            TransOr(node, currentProc, currentScopeID);
            return "P";
        }
        else
        {
            //AND
            TransAnd(node, currentProc, currentScopeID);
            return "P";
        }

        return value;
    }

    private String TransUNOP(Node node, String currentProc, int currentScopeID)
    {
        //on the symbol
        String value = "";
        if(node.getContent().equals("sqrt"))
        {
            value = " SQR ";
        }
        else
        {
            //not
            TransNot(node, currentProc, currentScopeID);
            return "P";
        }

        return value;
    }

    private void TransOr(Node node,String currentProc, int currentScopeID)
    {
        String BoolExpr1 = TransUNOP(node.children.get(2),currentProc, currentScopeID);
        String BoolExpr2 = TransUNOP(node.children.get(4),currentProc, currentScopeID);

        code += String.valueOf(lineNumber) + " IF " + BoolExpr1 + " THEN GOTO success \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " IF " + BoolExpr2 + " THEN GOTO success \n";
        lineNumber += 10;

        code += String.valueOf(lineNumber) + " GOTO failed \n";
        lineNumber += 10;
        int successLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 1 \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO exit \n";
        lineNumber += 10;
        int failedLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 0 \n";
        lineNumber += 10;
        int exitLineNumber = lineNumber;

        code = code.replace("success", String.valueOf(successLineNumber));
        code = code.replace("failed", String.valueOf(failedLineNumber));
        code = code.replace("exit", String.valueOf(exitLineNumber));
    }

    private void TransAnd(Node node,String currentProc, int currentScopeID)
    {
        String BoolExpr1 = TransUNOP(node.children.get(2),currentProc, currentScopeID);
        String BoolExpr2 = TransUNOP(node.children.get(4),currentProc, currentScopeID);

        code += String.valueOf(lineNumber) + " IF " + BoolExpr1 + " THEN GOTO otherCond \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO failed \n";
        lineNumber += 10;
        int otherCondLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " IF " + BoolExpr2 + " THEN GOTO success \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO failed \n";
        lineNumber += 10;
        int successLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 1 \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO exit \n";
        lineNumber += 10;
        int failedLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 0 \n";
        lineNumber += 10;
        int exitLineNumber = lineNumber;

        code = code.replace("otherCond", String.valueOf(otherCondLineNumber));
        code = code.replace("success", String.valueOf(successLineNumber));
        code = code.replace("failed", String.valueOf(failedLineNumber));
        code = code.replace("exit", String.valueOf(exitLineNumber));

    }

    private void TransNot(Node node,String currentProc, int currentScopeID)
    {
        String BoolExpr = TransUNOP(node.children.get(2),currentProc, currentScopeID);
        logicExpr = BoolExpr;

        code += String.valueOf(lineNumber) + " IF " + BoolExpr + " THEN GOTO failed \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " LET P = 1 \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO exit \n";
        lineNumber += 10;
        int failedLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 0 \n";
        lineNumber += 10;
        int exitLineNumber = lineNumber;

        code = code.replace("failed", String.valueOf(failedLineNumber));
        code = code.replace("exit", String.valueOf(exitLineNumber));
    }

    private void createBASICFile()
    {
        try
        {
            FileWriter fileWriter = new FileWriter("BASIC.txt");
            fileWriter.write(code);
            fileWriter.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
