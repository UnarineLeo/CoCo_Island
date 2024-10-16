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

    public void Translate(Node root, Scoping scopeDefined)
    {
        currentProcId = 0;
        scope = scopeDefined;
        translate(root, scopeNumber, "global");
        createBASICFile();
        System.out.println("BASIC.txt file created successfully!");
    }

    private void translate(Node node, int currentScope, String currentProc)
    {
        if(node == null)
        {
            return;
        }

        if(node.getContent().equals("FUNCTIONS"))
        {

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
                TransPrint(node, currentProc);
            }
            else if(node.children.getFirst().getContent().equals("return"))
            {
//                TransPrint(node, currentProc);
            }
            else if(node.children.getFirst().getContent().equals("ASSIGN"))
            {
//                TransPrint(node, currentProc);
            }
            else if(node.children.getFirst().getContent().equals("CALL"))
            {
//                TransPrint(node, currentProc);
            }
            else if(node.children.getFirst().getContent().equals("BRANCH"))
            {
//                TransPrint(node, currentProc);
            }
            else
            {

            }
        }
        else
        {
            for(int i = 0; i < node.children.size(); i++)
            {
                translate(node.children.get(i), currentScope, currentProc);
            }
        }

    }

    private void TransPrint(Node node, String currentProc)
    {
        Node atomicNode = node.children.get(1);
        String value = TransATOMIC(atomicNode, currentProc);
        code += String.valueOf(lineNumber) + "PRINT " + value + "\n";
        lineNumber += 10;
    }

    private void TransInput(Node node, String currentScope)
    {
        //from ASSIGN
        Node VNameNode = node.children.getFirst().children.getFirst();
        String varName = scope.getName(VNameNode);
        varName = formatName(varName, currentScope);

        code += String.valueOf(lineNumber) + " INPUT \"\";" + varName + "\n";
        lineNumber += 10;
    }

    private String TransATOMIC(Node node, String currentScope)
    {
        //on ATOMIC
        Node firstChild = node.children.getFirst();
        String value = "";
        if(firstChild.getContent().equals("VNAME"))
        {

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

    private String TransBINOP(Node node, String currentScope)
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
        }
        else
        {
            //AND
        }

        return value;
    }

    private String TransUNOP(Node node, String currentScope)
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
        }

        return value;
    }

    private String formatName(String varName, String currentScope)
    {
        //doesn't work, must fix
        String varType = "";

        for(int i = 0; i < scope.declaredList.size(); i++)
        {
            if(scope.declaredList.get(i)[0].equals(varName) && scope.declaredList.get(i)[2].equals(currentScope))
            {
                varType = scope.declaredList.get(i)[1];
            }
        }

        if(varType.isEmpty())
        {
            for(int i = 0; i < scope.declaredList.size(); i++)
            {
                if(scope.declaredList.get(i)[0].equals(varName) && scope.declaredList.get(i)[2].equals("global"))
                {
                    varType = scope.declaredList.get(i)[1];
                }
            }
        }

        if(varType.equals("num") || varType.equals("bool"))
        {
            varName = varName.toUpperCase();
            varName = varName.charAt(0) + String.valueOf(varName.charAt(2));
        }

        if(varType.equals("text") || varType.equals("proc"))
        {
            varName = varName.toUpperCase();
            varName = varName.charAt(0) + "$";
        }

        return varName;
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
