//Based on page 27 & 28 of the textbook
//I used the longest prefix of the input string to determine the token type
//Visible on a,b,d,e,i,etc.

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


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
            else if(input.charAt(i) == ',')
            {
                Token token = new Token(id,"Symbol",",",row,col);
                tokens.add(token);

            }
            else if(input.charAt(i) == ';')
            {
                Token token = new Token(id,"Symbol",";",row,col);
                tokens.add(token);
            }
            else if(input.charAt(i) == '(')
            {
                Token token = new Token(id,"Symbol","(",row,col);
                tokens.add(token);
            }
            else if(input.charAt(i) == ')')
            {
                Token token = new Token(id,"Symbol",")",row,col);
                tokens.add(token);
            }
            else if(input.charAt(i) == '{')
            {
                Token token = new Token(id,"Symbol","{",row,col);
                tokens.add(token);
            }
            else if(input.charAt(i) == '}')
            {
                Token token = new Token(id,"Symbol","}",row,col);
                tokens.add(token);
            }
            else if(input.charAt(i) == '=')
            {
                Token token = new Token(id,"Symbol","=",row,col);
                tokens.add(token);
            }
            else if(input.charAt(i) == 'F' && i+1< input.length() && input.charAt(i+1) == '_' && i+2< input.length() && Character.isLowerCase(input.charAt(i+2)) )
            {
                String FunctionName=input.substring(i, FindEndTheEndOfTheToken_Function_Variable(input, i+3));
                Token token = new Token(id,"FunctionName",FunctionName,row,col);
                tokens.add(token);
                col+=i-FindEndTheEndOfTheToken_Function_Variable(input, i);
                i=FindEndTheEndOfTheToken_Function_Variable(input, i+3);
               ;
              
            }
            else if(input.charAt(i) == 'V' && i+1< input.length() && input.charAt(i+1) == '_' && i+2< input.length() && Character.isLowerCase(input.charAt(i+2)) )
            {
                String VariableName=input.substring(i, FindEndTheEndOfTheToken_Function_Variable(input, i+3));
                Token token = new Token(id,"VariableName",VariableName,row,col);
                tokens.add(token);
                col+=i-FindEndTheEndOfTheToken_Function_Variable(input, i);
                i=FindEndTheEndOfTheToken_Function_Variable(input, i+3)-1;
                //skip some  charactet
            }
            else if(CheckIfItIsAConstant_String(ExtractNextPossibleKey(input, i)))
            {
                String Constant=ExtractNextPossibleKey(input, i);
                Token token= new Token(id,"Constant", Constant, row, col);
                tokens.add(token);
                
                int index=input.indexOf(' ', i);
                if(input.indexOf("\n", i) < index)
                    index=input.indexOf("\n", i);
        
                col+=index-i;
                i=index;
                //skip some characters

            }
            else if(CheckIfItIsAConstant_String(ExtractNextPossibleKey_Number_Constant(input, i)))
            {
                String Constant=ExtractNextPossibleKey_Number_Constant(input,i);
                Token token= new Token(id,"Constant", Constant, row, col);
                tokens.add(token);
                int index=input.indexOf(' ', i);
                if(input.indexOf("\n", i) < index)
                    index=input.indexOf("\n", i);
        
                col+=index-i;
                i=index-1;
                //skip some characters

            }
            else if(CheckIfItIsAConstant_Number(ExtractNextPossibleKey(input, i)))
            {
                String Keyword=ExtractNextPossibleKey(input, i).trim();
                Token token = new Token(id, "Keyword",Keyword, row, col );
                tokens.add(token);
                int index=input.indexOf(' ', i);
                if(input.indexOf("\n", i) < index)
                    index=input.indexOf("\n", i);
                
                col+=index-i;
                i=index-1;
               
            }

        }

        return tokens;
    }

    public  int  FindTheEndOfConstant(String input, int StartIndex)
    { 
        int index=input.indexOf(' ', StartIndex);
        if(input.indexOf("\n", StartIndex) < index)
            index=input.indexOf("\n", StartIndex);

        return index;
    }

    public int FindEndTheEndOfTheToken_Function_Variable(String input, int startIndex)//test
    {
        int index=startIndex;
     
        while(Character.isLowerCase(input.charAt(index))  || Character.isDigit(input.charAt(index)))
        {

            index++;
        }

        
        return index;
    }

    public String ExtractNextPossibleKey(String input, int StartIndex)//testes
    {
        // int StopIndex=StartIndex;

        // while( Character.isLetter(input.charAt(StopIndex)) )
        // {
        //    StopIndex++;
        // }
        int index=input.indexOf(' ', StartIndex);
        if(input.indexOf("\n", index) < StartIndex)
            index=input.indexOf("\n", StartIndex);

        return input.substring(StartIndex,index );
    }
   
    public String ExtractNextPossibleKey_Number_Constant(String input, int StartIndex)//tested
    {
        //int StopIndex=StartIndex;
        // int OnePointSymbol=0;

        // while( (Character.isDigit(input.charAt(StopIndex))  || input.charAt(StopIndex)=='.') && ! )
        // {
        //    StopIndex++;
        // }
        int index=input.indexOf(' ', StartIndex);
        if(input.indexOf("\n", index) < StartIndex)
            index=input.indexOf("\n", index);

        return input.substring(StartIndex, index);
    }

    public boolean CheckIfItIsAKey(String literal)//tested
    {

        if(literal.trim()=="main"|| literal.trim()=="begin"|| literal.trim()=="end"|| literal.trim()=="num"||literal.trim()=="text"|| literal.trim()=="skip"|| literal.trim()=="halt"|| literal.trim()=="print"|| literal.trim()=="< input"|| literal.trim()=="if" || literal.trim()=="then"|| literal.trim()=="else"|| literal.trim()=="not"|| literal.trim()=="sqrt"|| literal.trim()=="or"|| literal.trim()=="and"|| literal.trim()=="eq"|| literal.trim()=="grt"|| literal.trim()=="add"|| literal.trim()=="sub"|| literal.trim()=="mul"|| literal.trim()=="div"|| literal.trim()=="void")
        {
            return true;
        }
        return false;
    }
   
    public boolean CheckIfItIsAConstant_Number(String input)//tested
    {
        String[] regex= new String[7];
        regex[0] = "0";
        regex[1]="0\\.([0-9])*[1-9]";
        regex[2] = "-0\\.([0-9])*[1-9]";
        regex[3]="[1-9]([0-9])*";
        regex[5]="-[1-9]([0-9])*";
        regex[6] = "[1-9]([0-9])*\\.([0-9])*[1-9]";
        regex[4] = "-[1-9]([0-9])*\\.([0-9])*[1-9]";

        Pattern pattern;
        Matcher matcher;
        for(int i=0; i<regex.length; i++)
        {
            pattern=Pattern.compile(regex[i]);
            matcher=pattern.matcher(input);
            if(matcher.matches())
            {
                return true;
            } 
        }
        return false;
    }

    public boolean CheckIfItIsAConstant_String(String input)//tested
    {

        String[] regex= new String[8];
        regex[0]= "\"[A-Z][a-z]{7}\"";
        regex[1]= "\"[A-Z][a-z]{6}\"";
        regex[2]= "\"[A-Z][a-z]{5}\"";
        regex[3]= "\"[A-Z][a-z]{4}\"";
        regex[4]= "\"[A-Z][a-z]{3}\"";
        regex[5]= "\"[A-Z][a-z]{2}\"";
        regex[6]= "\"[A-Z][a-z]\"";
        regex[7]= "\"[A-Z]\"";

        Pattern pattern;
        Matcher matcher;
        for(int i=0; i<regex.length; i++)
        {
            pattern=Pattern.compile(regex[i]);
            matcher=pattern.matcher(input);
            if(matcher.matches())
            {
                return true;
            } 
        }
        return false;


        /*int CountCharacters=0;
        int index=StartIndex;
        if(input.charAt(index)!='"')
        {
            return false;
        }
        index++;
        CountCharacters++;
        if(!Character.isUpperCase(input.charAt(index)))
        {
            return false;
        }
        index++;
        CountCharacters++;
        while(CountCharacters<10 && Character.isLetter(input.charAt(index)) )
        {
            if(!Character.isLowerCase(input.charAt(index)))
            {
                return false;
            }
            index++;
            CountCharacters++;
        }
        if(input.charAt(index)!='"' || input.charAt(index)!= ' ' )
        {
            return false;
        }*/

       // return true;
    }
}