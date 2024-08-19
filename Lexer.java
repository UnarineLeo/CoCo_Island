//Based on page 27 & 28 of the textbook
//I used the longest prefix of the input string to determine the token type
//Visible on a,b,d,e,i,etc.

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class Lexer
{
    private int row = 1;
    private int col = 1;
    private int id = 0;

    public List<Token> toTokens(String filename)
    {
        List<Token> tokens = new ArrayList<Token>();

        String input = "";
        File file = new File(filename);
        Scanner reader = null;
        try
        {
            reader = new Scanner(file);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("\u001B[31mError\u001B[0m: Program file not found, please remove any spaces from the file name, and also include the extension(.txt) after the name and try again.");
            System.exit(0);
        }

        while (reader.hasNextLine()) {
            String input2 = reader.nextLine();
            input = input + input2 + "\n";
        }

        reader.close();

        if(input.isEmpty())
        {
            System.out.println("\u001B[31mWarning\u001B[0m: Program file is empty, please write a program in the file and try again.");
            return tokens;
        }

        for(int i = 0;i < input.length();i++)
        {
            id++;
            col++;

            if(input.charAt(i) == ' ')
            {
                continue;
            }
            else if(input.charAt(i) == '\n')
            {
                row++;
                col = 1;
                continue;
            }
            else if(input.charAt(i) == '\t')
            {
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

                String FunctionName = input.substring(i, FindEndTheEndOfTheToken_Function_Variable(input, i+3));
                Token token = new Token(id,"FNAME",FunctionName,row,col);
                tokens.add(token);

                col += i-FindEndTheEndOfTheToken_Function_Variable(input, i)-1; ///
                i = FindEndTheEndOfTheToken_Function_Variable(input, i+3)-1;

            }
            else if(input.charAt(i) == 'V' && i+1< input.length() && input.charAt(i+1) == '_' && i+2< input.length() && Character.isLowerCase(input.charAt(i+2)) )
            {

                String VariableName=input.substring(i, FindEndTheEndOfTheToken_Function_Variable(input, i+3));
                Token token = new Token(id,"VNAME",VariableName,row,col);
                tokens.add(token);

                col+=i-FindEndTheEndOfTheToken_Function_Variable(input, i)-1;
                i = FindEndTheEndOfTheToken_Function_Variable(input, i+3)-1;

            }
            else if(input.charAt(i)=='"' )
            {

                String Constant=input.substring(i, input.indexOf("\"", i+1)+1);
                if(!CheckIfItIsAConstant_String(Constant.trim()))
                {
                    System.out.println("Error Constant To Long");
                    System.out.println("\u001B[31mError\u001B[0m: Failed at index "+ i);
                    System.exit(0);
                }

                Token token= new Token(id,"CONST", Constant, row, col);
                tokens.add(token);

                int index= input.indexOf("\"", i+1);
                col+=index-i;
                i = index;

            }
            else if(input.charAt(i) == '-')
            {
                if(i+1 < input.length() && Character.isDigit(input.charAt(i+1)) && (input.charAt(i+1) != '0'))
                {
                    StringBuilder temp = new StringBuilder(Character.toString(input.charAt(i)));
                    for(int j = i+1;j < input.length();j++)
                    {
                        if(!Character.isDigit(input.charAt(j)))
                        {
                            if(input.charAt(j) == '.')
                            {
                                temp.append(Character.toString(input.charAt(j)));
                                i++;
                            }
                            break;
                        }
                        else
                        {
                            temp.append(Character.toString(input.charAt(j)));
                            i = j;
                        }
                    }

                    if(input.charAt(i) == '.')
                    {
                        Boolean added = false;
                        for(int j = i+1;j < input.length();j++)
                        {
                            if(!Character.isDigit(input.charAt(j)))
                            {
                                break;
                            }
                            else
                            {
                                added = true;
                                temp.append(Character.toString(input.charAt(j)));
                                i = j;
                            }
                        }

                        if(temp.charAt(temp.length()-1) == '0')
                        {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: The last digit of a decimal number must be [1-9]. Error at line " + row + " at column " + (col+i) + ".");
                            System.exit(0);
                        }

                        if(!added)
                        {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the decimal point. Error at line " + row + " at column " + (col+i) + ".");
                            System.exit(0);
                        }
                    }

                    if(temp.length() > 1)
                    {
                        Token token = new Token(id,"CONST", temp.toString(),row,(col));
                        tokens.add(token);
                        col = (col + temp.length() - 1);
                    }
                    else
                    {
                        System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the minus sign. Error at line " + row + " at column " + (col+i) + ".");
                        System.exit(0);
                    }
                }
                else if(i+1 < input.length() && Character.isDigit(input.charAt(i+1)) && (input.charAt(i+1) == '0'))
                {
                    if(i+2 < input.length() && input.charAt(i+2) == '.')
                    {
                        StringBuilder temp = new StringBuilder(Character.toString(input.charAt(i)));
                        temp.append(Character.toString(input.charAt(i+1)));
                        temp.append(Character.toString(input.charAt(i+2)));
                        i += 2;

                        if(i+1 < input.length() && Character.isDigit(input.charAt(i+1)))
                        {
                            for(int j = i+1;j < input.length();j++)
                            {
                                if(!Character.isDigit(input.charAt(j)))
                                {
                                    break;
                                }
                                else
                                {
                                    temp.append(Character.toString(input.charAt(j)));
                                    i = j;
                                }
                            }

                            if(temp.charAt(temp.length()-1) == '0')
                            {
                                System.out.println("\u001B[31mLexing Error\u001B[0m: The last digit of a decimal number must be [1-9]. Error at line " + row + " at column " + (col+i) + ".");
                                System.exit(0);
                            }

                            Token token = new Token(id,"CONST",temp.toString(),row,(col));
                            tokens.add(token);
                            col = col + temp.length();

                        }
                        else
                        {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the decimal point. Error at line " + row + " at column " + (col+i) + ".");
                            System.exit(0);
                        }
                    }
                    else
                    {
                        System.out.println("\u001B[31mLexing Error\u001B[0m: \"-0\" is not a valid number. Error at line " + row + " at column " + (col+i) + ".");
                        System.exit(0);
                    }
                }
                else
                {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit[1-9] after the minus sign. Error at line " + row + " at column " + (col+i) + ".");
                    System.exit(0);
                }
            }
            else if(Character.isDigit(input.charAt(i)))
            {
                if(input.charAt(i) == '0')
                {
                    if(i+1 < input.length() && Character.isDigit(input.charAt(i+1)))
                    {
                        System.out.println("\u001B[31mLexing Error\u001B[0m: No number can start with 0 and be followed by another digit. Error at line " + row + " at column " + (col+i) + ".");
                        System.exit(0);
                    }
                    else if(i+1 < input.length() && input.charAt(i+1) == '.')
                    {
                        StringBuilder temp = new StringBuilder(Character.toString(input.charAt(i)));
                        temp.append(Character.toString(input.charAt(i+1)));
                        i++;

                        if(i+1 < input.length() && Character.isDigit(input.charAt(i+1)))
                        {
                            temp.append(Character.toString(input.charAt(i+1)));
                            i++;
                            for(int j = i+1;j < input.length();j++)
                            {
                                if(!Character.isDigit(input.charAt(j)))
                                {
                                    break;
                                }
                                else
                                {
                                    temp.append(Character.toString(input.charAt(j)));
                                    i = j;
                                }
                            }

                            if(temp.charAt(temp.length()-1) == '0')
                            {
                                System.out.println("\u001B[31mLexing Error\u001B[0m: The last digit of a decimal number must be [1-9]. Error at line " + row + " at column " + (col+i) + ".");
                                System.exit(0);
                            }

                            Token token = new Token(id,"CONST",temp.toString(),row,(col));
                            tokens.add(token);
                            col = col + temp.length();

                        }
                        else
                        {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the decimal point. Error at line " + row + " at column " + (col+i) + ".");
                            System.exit(0);
                        }
                    }
                    else
                    {
                        Token token2 = new Token(id,"CONST","0",row,col);
                        tokens.add(token2);
                    }
                }
                else
                {
                    StringBuilder temp = new StringBuilder(Character.toString(input.charAt(i)));
                    for(int j = i+1;j < input.length();j++)
                    {
                        if(!Character.isDigit(input.charAt(j)))
                        {
                            if(input.charAt(j) == '.')
                            {
                                temp.append(Character.toString(input.charAt(j)));
                                i++;
                            }
                            break;
                        }
                        else
                        {
                            temp.append(Character.toString(input.charAt(j)));
                            i = j;
                        }
                    }

                    if(input.charAt(i) == '.')
                    {
                        Boolean added = false;
                        for(int j = i+1;j < input.length();j++)
                        {
                            if(!Character.isDigit(input.charAt(j)))
                            {
                                break;
                            }
                            else
                            {
                                added = true;
                                temp.append(Character.toString(input.charAt(j)));
                                i = j;
                            }
                        }

                        if(temp.charAt(temp.length()-1) == '0')
                        {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: The last digit of a decimal number must be [1-9]. Error at line " + row + " at column " + (col+i) + ".");
                            System.exit(0);
                        }

                        if(!added)
                        {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the decimal point. Error at line " + row + " at column " + (col+i) + ".");
                            System.exit(0);
                        }
                    }

                    Token token = new Token(id,"CONST",temp.toString(),row,(col));
                    tokens.add(token);
                    col = col + temp.length();//subract 1??
                }

            }
            else if(input.charAt(i) == '<')
            {
                if((i+6) <= input.length() && input.charAt(i+1) == ' ' && input.charAt(i+2) == 'i' && input.charAt(i+3) == 'n' && input.charAt(i+4) == 'p' && input.charAt(i+5) == 'u' && input.charAt(i+6) == 't')
                {
                    Token token = new Token(id,"Keyword","< input",row,col);
                    tokens.add(token);
                    i+=7;
                }
                else
                {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"< input\", Expected \" input\" after the character \'<\' at line " + row + " between columns " + col + "and " + (col+6) + ".");
                    System.exit(0);
                }
            }
            else if((input.charAt(i)=='m'|| input.charAt(i)=='b'||input.charAt(i)=='e'||input.charAt(i)=='n'||input.charAt(i)=='s'||input.charAt(i)=='h'||input.charAt(i)=='p'||input.charAt(i)=='i'||input.charAt(i)=='t'||input.charAt(i)=='e'||input.charAt(i)=='v'))
            {}
            else
            {
                System.out.println("\u001B[31mLexing Error\u001B[0m: Invalid character " + input.charAt(i) + " at line " + row + " at column " + col + ".");
                System.exit(0);
            }

        }

        return tokens;
    }

    public int FindEndTheEndOfTheToken_Function_Variable(String input, int startIndex)//test
    {
        //possible error
        int index=startIndex;

        while(Character.isLowerCase(input.charAt(index))  || Character.isDigit(input.charAt(index)))
        {
            index++;
        }

        return index;
    }

    public boolean CheckIfItIsAConstant_String(String input)//tested
    {
        String[] regex = new String[8];
        regex[0] = "\"[A-Z][a-z]{7}\"";
        regex[1] = "\"[A-Z][a-z]{6}\"";
        regex[2] = "\"[A-Z][a-z]{5}\"";
        regex[3] = "\"[A-Z][a-z]{4}\"";
        regex[4] = "\"[A-Z][a-z]{3}\"";
        regex[5] = "\"[A-Z][a-z]{2}\"";
        regex[6] = "\"[A-Z][a-z]\"";
        regex[7] = "\"[A-Z]\"";

        Pattern pattern;
        Matcher matcher;
        for (String s : regex)
        {
            pattern = Pattern.compile(s);
            matcher = pattern.matcher(input);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;

    }
}