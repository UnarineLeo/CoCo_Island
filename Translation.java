import java.io.*;

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
            for(String key: scope.scopeTable.keySet())
            {

                if(scope.scopeTable.get(key)[0].equals(funcName) && scope.scopeTable.get(key)[4].equals(currentProc) && scope.scopeTable.get(key)[3].equals(String.valueOf(currentScopeID)))
                {
                    funcName = scope.scopeTable.get(key)[0];
                    scopeNameID = key;
                    break;
                }
            }

            currScope[0] = funcName;
            currScope[1] = scopeNameID;
        }
        else if(node.getContent().equals("PROG"))
        {
            for(int i = 0; i < node.children.size(); i++)
            {
                if(node.children.get(i).getContent().equals("ALGO"))
                {
                    for(int j = 0; j < node.children.size(); j++)
                    {
                        translate(node.children.get(j), currentScopeID, currentProc);
                    }
                    code += String.valueOf(lineNumber) + " STOP";
                    lineNumber += 10;
                    break;
                    ///verify
                }
                else
                {
                    //hopefully assign.txt doesn't interrupt FUNCTIONS
                    translate(node.children.get(i), currentScopeID, currentProc);
                }
            }

        }
        else if(node.getContent().equals("FUNCTIONS"))
        {
            if(node.children.isEmpty())
            {
                code += String.valueOf(lineNumber) + " REM END\n";
                lineNumber += 10;
            }
            else
            {
                String funcName = scope.getName(node.children.getFirst().children.getFirst().children.get(1));
                for(String key: scope.scopeTable.keySet())
                {
                    if(scope.scopeTable.get(key)[0].equals(funcName) && scope.scopeTable.get(key)[4].equals(currentProc) && scope.scopeTable.get(key)[3].equals(String.valueOf(currentScopeID)))
                    {
                        funcName = scope.scopeTable.get(key)[5];
                        break;
                    }
                }

                code = code.replace("GOSUB " + funcName, "GOSUB " + String.valueOf(lineNumber));

                for(int i = 0; i < node.children.size(); i++)
                {
                    translate(node.children.get(i), currentScopeID, currentProc);
                }
            }
        }
        else if(node.getContent().equals("INSTRUC"))
        {
            if(node.children.isEmpty())
            {
                code += String.valueOf(lineNumber) + " REM END";
                lineNumber += 10;
            }
            else
            {
                for(int i = 0; i < node.children.size(); i++)
                {
                    translate(node.children.get(i), currentScopeID, currentProc);
                }
            }

        }
        else if(node.getContent().equals("BODY"))
        {
            for(int i = 0; i < node.children.size(); i++)
            {
                translate(node.children.get(i), Integer.parseInt(currScope[1]), currScope[0]);
            }
            code += String.valueOf(lineNumber) + " STOP\n";
            lineNumber += 10;
        }
        else if(node.getContent().equals("COMMAND"))
        {
            if(node.children.getFirst().getContent().equals("skip"))
            {
                code += String.valueOf(lineNumber) + " REM DO NOTHING\n";
                lineNumber += 10;
            }
            else if(node.children.getFirst().getContent().equals("halt"))
            {
                code += String.valueOf(lineNumber) + " STOP\n";
                lineNumber += 10;
            }
            else if(node.children.getFirst().getContent().equals("print"))
            {
                TransPrint(node, currentProc, currentScopeID);
            }
            else if(node.children.getFirst().getContent().equals("return"))
            {
                code += String.valueOf(lineNumber) + " RETURN" + "\n";
                lineNumber += 10;
            }
            else if(node.children.getFirst().getContent().equals("ASSIGN"))
            {
                TransASSIGN(node.children.getFirst(), currentProc, currentScopeID);
            }
            else if(node.children.getFirst().getContent().equals("CALL"))
            {
                TransCALL(node.children.getFirst(), currentProc, currentScopeID);
            }
            else if(node.children.getFirst().getContent().equals("BRANCH"))
            {
                branchCount++;
                TransBranch(node.children.getFirst(), currentProc, currentScopeID);
            }
            else
            {
                return;
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
        Node isItTerm = null;
        Node isItInput = node.children.get(1);
        if(node.children.size() >= 3)
        {
            isItTerm = node.children.get(2);
        }

        if(isItInput.getContent().equals("< input"))
        {
            String varName = scope.getName(node.children.getFirst());
            for(String key: scope.scopeTable.keySet())
            {
                if(scope.scopeTable.get(key)[0].equals(varName) && scope.scopeTable.get(key)[4].equals(currentProc) && scope.scopeTable.get(key)[3].equals(String.valueOf(currentScopeID)))
                {
                    varName = scope.scopeTable.get(key)[5];
                    break;
                }
            }

            code += String.valueOf(lineNumber) + " INPUT \"\";" + varName + "\n";
            lineNumber += 10;
        }
        else
        {
            String varName = scope.getName(node.children.getFirst());
            for(String key: scope.scopeTable.keySet())
            {
                if(scope.scopeTable.get(key)[0].equals(varName) && scope.scopeTable.get(key)[4].equals(currentProc) && scope.scopeTable.get(key)[3].equals(String.valueOf(currentScopeID)))
                {
                    varName = scope.scopeTable.get(key)[5];
                    break;
                }
            }

            Node isItCall = isItTerm.children.getFirst();
            if(isItCall.getContent().equals("CALL"))
            {
                TransCALL(isItCall, currentProc, currentScopeID);
            }
            else
            {
                code += String.valueOf(lineNumber) + " LET " + varName + " = " +  TransTERM(isItTerm, currentProc, currentScopeID) + "\n";
                lineNumber += 10;
            }
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
            value = TransOP(firstChild, currentProc, currentScopeID);
        }
        else
        {

            value = "";
        }

        return value;
    }

    private String TransOP(Node node, String currentProc, int currentScopeID)
    {
        Node firstChild = node.children.getFirst().children.getFirst();

        if(firstChild.getContent().equals("sqrt"))
        {
            return "SQR(" + TransOP(node.children.get(2).children.getFirst(), currentProc, currentScopeID) + ")";
        }
        else if(firstChild.getContent().equals("add"))
        {
            return TransOP(node.children.get(2).children.getFirst(), currentProc, currentScopeID) + " + " + TransOP(node.children.get(4).children.getFirst(), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("sub"))
        {
            return TransOP(node.children.get(2).children.getFirst(), currentProc, currentScopeID) + " - " + TransOP(node.children.get(4).children.getFirst(), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("mul"))
        {
            return TransOP(node.children.get(2).children.getFirst(), currentProc, currentScopeID) + " * " + TransOP(node.children.get(4).children.getFirst(), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("div"))
        {
            return TransOP(node.children.get(2).children.getFirst(), currentProc, currentScopeID) + " / " + TransOP(node.children.get(4).children.getFirst(), currentProc, currentScopeID);
        }
        else if(node.getContent().equals("ATOMIC"))
        {
            return TransATOMIC(node, currentProc, currentScopeID);
        }
        else
        {
            return "";
        }

    }

    private void TransCALL(Node node, String currentProc, int currentScopeID)
    {
        String procName = scope.getName(node.children.getFirst());
        for(String key: scope.scopeTable.keySet())
        {
            if(scope.scopeTable.get(key)[0].equals(procName) && scope.scopeTable.get(key)[4].equals(currentProc) && scope.scopeTable.get(key)[3].equals(String.valueOf(currentScopeID)))
            {
                procName = scope.scopeTable.get(key)[5];
                break;
            }
        }

        code += String.valueOf(lineNumber) + " GOSUB " + procName + "\n";
        lineNumber += 10;
    }

    private void TransPrint(Node node, String currentProc, int currentScopeID)
    {
        Node atomicNode = node.children.get(1);
        String value = TransATOMIC(atomicNode, currentProc, currentScopeID);
        code += String.valueOf(lineNumber) + " PRINT " + value + "\n";
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
            for(String key: scope.scopeTable.keySet())
            {
                if(scope.scopeTable.get(key)[0].equals(name) && scope.scopeTable.get(key)[4].equals(currentProc) && scope.scopeTable.get(key)[3].equals(String.valueOf(currentScopeID)))
                {
                    value = scope.scopeTable.get(key)[5];
                    break;
                }
            }
        }
        else
        {
            Node constChild = firstChild.children.getFirst();
            if(constChild.getContent().charAt(0) == '"')
            {
                value = constChild.getContent();
            }
            else
            {
                value = String.valueOf(constChild.getContent());
            }
        }

        return value;
    }

    private String TransCOND(Node node, String currentProc, int currentScopeID)
    {
        //Should be on SIMPLE/COMPOSIT

        Node firstChild = node.children.getFirst().children.getFirst();

        if(firstChild.getContent().equals("sqrt"))
        {
            return "SQR(" + TransOP(node.children.get(2), currentProc, currentScopeID) + ")";
        }
        else if(firstChild.getContent().equals("add"))
        {
            return TransOP(node.children.get(2), currentProc, currentScopeID) + " + " + TransOP(node.children.get(4), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("sub"))
        {
            return TransOP(node.children.get(2), currentProc, currentScopeID) + " - " + TransOP(node.children.get(4), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("mul"))
        {
            return TransOP(node.children.get(2), currentProc, currentScopeID) + " * " + TransOP(node.children.get(4), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("div"))
        {
            return TransOP(node.children.get(2), currentProc, currentScopeID) + " / " + TransOP(node.children.get(4), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("eq"))
        {
            return TransOP(node.children.get(2), currentProc, currentScopeID) + " = " + TransOP(node.children.get(4), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("grt"))
        {
            return TransOP(node.children.get(2), currentProc, currentScopeID) + " > " + TransOP(node.children.get(4), currentProc, currentScopeID);
        }
        else if(firstChild.getContent().equals("not"))
        {
            TransNot(node, currentProc, currentScopeID);
            return "P";
        }
        else if(firstChild.getContent().equals("or"))
        {
            TransOr(node, currentProc, currentScopeID);
            return "P";
        }
        else if(firstChild.getContent().equals("and"))
        {
            TransAnd(node, currentProc, currentScopeID);
            return "P";
        }
        else if(node.getContent().equals("ATOMIC"))
        {
            return TransATOMIC(node, currentProc, currentScopeID);
        }
        else
        {
            return "";
        }
    }

    private void TransOr(Node node,String currentProc, int currentScopeID)
    {
        //from SIMPLE or COMPOSIT node

        String BoolExpr1 = TransCOND(node.children.get(2),currentProc, currentScopeID);
        String BoolExpr2 = TransCOND(node.children.get(4),currentProc, currentScopeID);

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
        //from SIMPLE or COMPOSIT node

        String BoolExpr1 = TransCOND(node.children.get(2),currentProc, currentScopeID);
        String BoolExpr2 = TransCOND(node.children.get(4),currentProc, currentScopeID);

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
        //from SIMPLE or COMPOSIT node

        String BoolExpr = TransCOND(node.children.get(2),currentProc, currentScopeID);
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

    private void TransBranch(Node node, String currentProc, int currentScope)
    {
        String BooleanExpr = TransCOND(node.children.get(1).children.getFirst(),currentProc, currentScope);

        int branchNumber = branchCount;
        inBranch = true;

        code += String.valueOf(lineNumber) + " IF " + BooleanExpr + " THEN GOTO Branch" + branchNumber + "\n";
        lineNumber += 10;
        translate(node.children.get(3), currentScope, currentProc);
        code+= String.valueOf(lineNumber) + " GOTO exit" + branchNumber + "\n";
        lineNumber += 10;
        int otherLineNumber = lineNumber;
        translate(node.children.get(5), currentScope, currentProc);
        int exitLineNumber = lineNumber;

        code = code.replace("Branch" + branchNumber, String.valueOf(otherLineNumber));
        code = code.replace("exit" + branchNumber, String.valueOf(exitLineNumber));
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