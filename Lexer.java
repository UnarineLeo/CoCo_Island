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
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                Token token = new Token(id,"Symbol",",",row,col);
                tokens.add(token);
                System.out.print("Next Index "+  (i+1));

            }
            else if(input.charAt(i) == ';')
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                Token token = new Token(id,"Symbol",";",row,col);
                tokens.add(token);
                System.out.println("Next Index "+ (i+1));
            }
            else if(input.charAt(i) == '(')
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                Token token = new Token(id,"Symbol","(",row,col);
                tokens.add(token);
                System.out.println("Next Index "+ (i+1));
            }
            else if(input.charAt(i) == ')')
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                Token token = new Token(id,"Symbol",")",row,col);
                tokens.add(token);
                System.out.println("Next Index "+ (i+1));
            }
            else if(input.charAt(i) == '{')
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                Token token = new Token(id,"Symbol","{",row,col);
                tokens.add(token);
                System.out.println("Next Index "+ (i+1));
            }
            else if(input.charAt(i) == '}')
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                Token token = new Token(id,"Symbol","}",row,col);
                tokens.add(token);
                System.out.println("Next Index "+ (i+1));
            }
            else if(input.charAt(i) == '=')
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                Token token = new Token(id,"Symbol","=",row,col);
                tokens.add(token);
                System.out.println("next Index "+ (i+1));
            }
            else if(input.charAt(i) == 'F' && i+1< input.length() && input.charAt(i+1) == '_' && i+2< input.length() && Character.isLowerCase(input.charAt(i+2)) )
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                String FunctionName=input.substring(i, FindEndTheEndOfTheToken_Function_Variable(input, i+3));
                Token token = new Token(id,"FunctionName",FunctionName,row,col);
                tokens.add(token);
                col+=i-FindEndTheEndOfTheToken_Function_Variable(input, i);
                i=FindEndTheEndOfTheToken_Function_Variable(input, i+3);
                System.out.println("Next Index "+ i);
              
            }
            else if(input.charAt(i) == 'V' && i+1< input.length() && input.charAt(i+1) == '_' && i+2< input.length() && Character.isLowerCase(input.charAt(i+2)) )
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                String VariableName=input.substring(i, FindEndTheEndOfTheToken_Function_Variable(input, i+3));
                Token token = new Token(id,"VariableName",VariableName,row,col);
                tokens.add(token);
                col+=i-FindEndTheEndOfTheToken_Function_Variable(input, i);
                i=FindEndTheEndOfTheToken_Function_Variable(input, i+3);
                System.out.println("Next Index "+ i);
                //skip some  charactet
            }
            else if(input.charAt(i)=='"'  /*&& CheckIfThereIsStringConstant(input, i)*/)
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                String Constant=input.substring(i, input.indexOf("\"", i+1)+1);
               
                Token token= new Token(id,"Constant", Constant, row, col);
                tokens.add(token);
                
                int index= input.indexOf("\"", i+1);
                col+=index-i;
                i=index+1;//test
                System.out.println("next Index "+ (i));
                

            }
            else if(input.charAt(i)=='-' || Character.isDigit(input.charAt(i)))
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                String Constant=ExtractNextPossibleKey_Number_Constant(input,i);
                Token token= new Token(id,"Constant", Constant, row, col);
                tokens.add(token);
                int index=input.indexOf(' ', i);
                if(input.indexOf("\n", i) < index)
                    index=input.indexOf("\n", i);
        
                col+=index-i;
                i=index;
                System.out.println("next Index "+ i);
      

            }
            else if((input.charAt(i)=='m'|| input.charAt(i)=='b'||input.charAt(i)=='e'||input.charAt(i)=='n'||input.charAt(i)=='s'||input.charAt(i)=='h'||input.charAt(i)=='p'||input.charAt(i)=='i'||input.charAt(i)=='t'||input.charAt(i)=='e'||input.charAt(i)=='v') && CheckIfThereIsAKeyword(input, i))
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                int index=input.indexOf(' ', i);
                if(input.indexOf("\n", i) < index)
                    index=input.indexOf("\n", i);
                String Keyword=input.substring(i,index).trim();

                Token token= new Token(id,"Keyword", Keyword, row, col);
                tokens.add(token);

                col+=index-i;
                i=index;
                System.out.println("Next Index "+ (i));
    
            }
            else if((input.charAt(i)=='n'|| input.charAt(i)=='s'||input.charAt(i)=='o'||input.charAt(i)=='a'||input.charAt(i)=='e'||input.charAt(i)=='g'||input.charAt(i)=='s'||input.charAt(i)=='m'||input.charAt(i)=='d') && CheckIfThereIsAKeyword2(input, i))
            {
                System.out.println("Entry Index "+ i);
                System.out.println(input.charAt(i));
                int index=input.indexOf('(', i);
       
                String Keyword=input.substring(i,index).trim();

                Token token= new Token(id,"Keyword", Keyword, row, col);
                tokens.add(token);

                col+=index-i;
                i=index;
                System.out.println("Next Index "+ i);
            }
            else if(input.charAt(i)=='<')
            {
                
            }
           
            else{
                //error
                
                System.out.println("Error case");
                System.out.println("Entry Index "+ i);
                System.out.println("Next Index" + (i+1));
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

   
    public Boolean CheckIfThereIsStringConstant(String input, int startIndex)
    {
        int NextDoubleColon=input.indexOf("\"", startIndex+1);


        return CheckIfItIsAConstant_String(input.substring(startIndex, NextDoubleColon));

    }

    public Boolean CheckIfThereIsAKeyword(String input, int StartIndex)
    {
        int index=input.indexOf(' ', StartIndex);
     
       
        if(input.indexOf("\n", StartIndex) < index && input.indexOf("\n", StartIndex)>0){
            index=input.indexOf("\n", StartIndex);
        }

        // System.out.println(input.charAt(index-1));
        // System.out.println(input.charAt(StartIndex));
        // System.out.println(input.substring(StartIndex, index).length());
        // System.out.println(input.substring(StartIndex, index));    

        return CheckIfItIsAKey(input.substring(StartIndex, index));
    }
    public Boolean CheckIfThereIsAKeyword2(String input, int StartIndex)
    {
        int index=input.indexOf('(', StartIndex);
        return CheckIfItIsAKey(input.substring(StartIndex, index));
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
        if(input.indexOf("\n", StartIndex) < index)
            index=input.indexOf("\n", StartIndex);

        return input.substring(StartIndex, index);
    }

    public boolean CheckIfItIsAKey(String literal)//tested
    {
        String trimmedLiteral = literal.trim();
  
    
        String[] keywords = {
            "main", "begin", "end", "num", "text", "skip", "halt", "print", "< input", 
            "if", "then", "else", "not", "sqrt", "or", "and", "eq", "grt", "add", "sub", 
            "mul", "div", "void"
        };
    
        for (String keyword : keywords) {
            if (trimmedLiteral.equals(keyword)) {
                return true;
            }
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