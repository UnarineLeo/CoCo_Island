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

        for (int i = 0; i < input.length(); i++) {
            id++;
            col++;

            if (input.charAt(i) == ' ') {
                id--;
                continue;
            } else if (input.charAt(i) == '\n') {
                id--;
                row++;
                col = 1;
                continue;
            } else if (input.charAt(i) == '\t') {
                id--;
                continue;
            } else if (input.charAt(i) == ',') {
                Token token = new Token(id, "Symbol", ",", row, col);
                tokens.add(token);
            } else if (input.charAt(i) == ';') {
                Token token = new Token(id, "Symbol", ";", row, col);
                tokens.add(token);
            } else if (input.charAt(i) == '(') {
                Token token = new Token(id, "Symbol", "(", row, col);
                tokens.add(token);
            } else if (input.charAt(i) == ')') {
                Token token = new Token(id, "Symbol", ")", row, col);
                tokens.add(token);
            } else if (input.charAt(i) == '{') {
                Token token = new Token(id, "Symbol", "{", row, col);
                tokens.add(token);
            } else if (input.charAt(i) == '}') {
                Token token = new Token(id, "Symbol", "}", row, col);
                tokens.add(token);
            } else if (input.charAt(i) == '=') {
                Token token = new Token(id, "Symbol", "=", row, col);
                tokens.add(token);
            } else if (input.charAt(i) == 'F') {
                if (i + 1 < input.length() && input.charAt(i + 1) == '_') {
                    if (i + 2 < input.length() && Character.isLowerCase(input.charAt(i + 2))) {
                        String FunctionName = input.substring(i, FindEndTheEndOfTheToken_Function_Variable(input, i + 3));
                        Token token = new Token(id, "FNAME", FunctionName, row, col);
                        tokens.add(token);
                    } else {
                        System.out.println("\u001B[31mLexing Error\u001B[0m: Expected an lowercase letter after the character \'F_\' at line " + row + " at column " + (col + i + 2) + ".");
                        System.exit(0);
                    }
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: Expected an underscore(_) after the character \'F\' at line " + row + " at column " + (col + i + 1) + ".");
                    System.exit(0);
                }


                col += i - FindEndTheEndOfTheToken_Function_Variable(input, i) - 1; ///
                i = FindEndTheEndOfTheToken_Function_Variable(input, i + 3) - 1;

            } else if (input.charAt(i) == 'V') {
                if (i + 1 < input.length() && input.charAt(i + 1) == '_') {
                    if (i + 2 < input.length() && Character.isLowerCase(input.charAt(i + 2))) {
                        String VariableName = input.substring(i, FindEndTheEndOfTheToken_Function_Variable(input, i + 3));
                        Token token = new Token(id, "VNAME", VariableName, row, col);
                        tokens.add(token);
                    } else {
                        System.out.println("\u001B[31mLexing Error\u001B[0m: Expected an lowercase letter after the character \'V_\' at line " + row + " at column " + (col + i + 2) + ".");
                        System.exit(0);
                    }
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: Expected an underscore(_) after the character \'V\' at line " + row + " at column " + (col + i + 1) + ".");
                    System.exit(0);
                }

                col += i - FindEndTheEndOfTheToken_Function_Variable(input, i) - 1;
                i = FindEndTheEndOfTheToken_Function_Variable(input, i + 3) - 1;

            } else if (input.charAt(i) == '"') {

                String Constant = input.substring(i, input.indexOf("\"", i + 1) + 1);
                if (input.indexOf("\"", i + 1) == -1) {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a closing double quote(\") at the end of the string constant,with 1-8 alphat. Error at line " + row + " at column " + col + ".");
                    System.exit(0);
                }

                if (!CheckIfItIsAConstant_String(Constant.trim())) {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: String Constant is either too long or too short OR it does not start with a capital letter followed by 0-7 lowercase letters. Error at line " + row + " at column " + col + ".");
                    System.exit(0);
                }

                Token token = new Token(id, "CONST", Constant, row, col);
                tokens.add(token);

                int index = input.indexOf("\"", i + 1);
                col += index - i;
                i = index;

            } else if (input.charAt(i) == '-') {
                if (i + 1 < input.length() && Character.isDigit(input.charAt(i + 1)) && (input.charAt(i + 1) != '0')) {
                    StringBuilder temp = new StringBuilder(Character.toString(input.charAt(i)));
                    for (int j = i + 1; j < input.length(); j++) {
                        if (!Character.isDigit(input.charAt(j))) {
                            if (input.charAt(j) == '.') {
                                temp.append(Character.toString(input.charAt(j)));
                                i++;
                            }
                            break;
                        } else {
                            temp.append(Character.toString(input.charAt(j)));
                            i = j;
                        }
                    }

                    if (input.charAt(i) == '.') {
                        Boolean added = false;
                        for (int j = i + 1; j < input.length(); j++) {
                            if (!Character.isDigit(input.charAt(j))) {
                                break;
                            } else {
                                added = true;
                                temp.append(Character.toString(input.charAt(j)));
                                i = j;
                            }
                        }

                        if (temp.charAt(temp.length() - 1) == '0') {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: The last digit of a decimal number must be [1-9]. Error at line " + row + " at column " + (col + i) + ".");
                            System.exit(0);
                        }

                        if (!added) {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the decimal point. Error at line " + row + " at column " + (col + i) + ".");
                            System.exit(0);
                        }
                    }

                    if (temp.length() > 1) {
                        Token token = new Token(id, "CONST", temp.toString(), row, (col));
                        tokens.add(token);
                        col = (col + temp.length() - 1);
                    } else {
                        System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the minus sign. Error at line " + row + " at column " + (col + i) + ".");
                        System.exit(0);
                    }
                } else if (i + 1 < input.length() && Character.isDigit(input.charAt(i + 1)) && (input.charAt(i + 1) == '0')) {
                    if (i + 2 < input.length() && input.charAt(i + 2) == '.') {
                        StringBuilder temp = new StringBuilder(Character.toString(input.charAt(i)));
                        temp.append(Character.toString(input.charAt(i + 1)));
                        temp.append(Character.toString(input.charAt(i + 2)));
                        i += 2;

                        if (i + 1 < input.length() && Character.isDigit(input.charAt(i + 1))) {
                            for (int j = i + 1; j < input.length(); j++) {
                                if (!Character.isDigit(input.charAt(j))) {
                                    break;
                                } else {
                                    temp.append(Character.toString(input.charAt(j)));
                                    i = j;
                                }
                            }

                            if (temp.charAt(temp.length() - 1) == '0') {
                                System.out.println("\u001B[31mLexing Error\u001B[0m: The last digit of a decimal number must be [1-9]. Error at line " + row + " at column " + (col + i) + ".");
                                System.exit(0);
                            }

                            Token token = new Token(id, "CONST", temp.toString(), row, (col));
                            tokens.add(token);
                            col = col + temp.length();

                        } else {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the decimal point. Error at line " + row + " at column " + (col + i) + ".");
                            System.exit(0);
                        }
                    } else {
                        System.out.println("\u001B[31mLexing Error\u001B[0m: \"-0\" is not a valid number. Error at line " + row + " at column " + (col + i) + ".");
                        System.exit(0);
                    }
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit[1-9] after the minus sign. Error at line " + row + " at column " + (col + i) + ".");
                    System.exit(0);
                }
            } else if (Character.isDigit(input.charAt(i))) {
                if (input.charAt(i) == '0') {
                    if (i + 1 < input.length() && Character.isDigit(input.charAt(i + 1))) {
                        System.out.println("\u001B[31mLexing Error\u001B[0m: No number can start with 0 and be followed by another digit. Error at line " + row + " at column " + (col + i) + ".");
                        System.exit(0);
                    } else if (i + 1 < input.length() && input.charAt(i + 1) == '.') {
                        StringBuilder temp = new StringBuilder(Character.toString(input.charAt(i)));
                        temp.append(Character.toString(input.charAt(i + 1)));
                        i++;

                        if (i + 1 < input.length() && Character.isDigit(input.charAt(i + 1))) {
                            temp.append(Character.toString(input.charAt(i + 1)));
                            i++;
                            for (int j = i + 1; j < input.length(); j++) {
                                if (!Character.isDigit(input.charAt(j))) {
                                    break;
                                } else {
                                    temp.append(Character.toString(input.charAt(j)));
                                    i = j;
                                }
                            }

                            if (temp.charAt(temp.length() - 1) == '0') {
                                System.out.println("\u001B[31mLexing Error\u001B[0m: The last digit of a decimal number must be [1-9]. Error at line " + row + " at column " + (col + i) + ".");
                                System.exit(0);
                            }

                            Token token = new Token(id, "CONST", temp.toString(), row, (col));
                            tokens.add(token);
                            col = col + temp.length();

                        } else {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the decimal point. Error at line " + row + " at column " + (col + i) + ".");
                            System.exit(0);
                        }
                    } else {
                        Token token2 = new Token(id, "CONST", "0", row, col);
                        tokens.add(token2);
                    }
                } else {
                    StringBuilder temp = new StringBuilder(Character.toString(input.charAt(i)));
                    for (int j = i + 1; j < input.length(); j++) {
                        if (!Character.isDigit(input.charAt(j))) {
                            if (input.charAt(j) == '.') {
                                temp.append(Character.toString(input.charAt(j)));
                                i++;
                            }
                            break;
                        } else {
                            temp.append(Character.toString(input.charAt(j)));
                            i = j;
                        }
                    }

                    if (input.charAt(i) == '.') {
                        Boolean added = false;
                        for (int j = i + 1; j < input.length(); j++) {
                            if (!Character.isDigit(input.charAt(j))) {
                                break;
                            } else {
                                added = true;
                                temp.append(Character.toString(input.charAt(j)));
                                i = j;
                            }
                        }

                        if (temp.charAt(temp.length() - 1) == '0') {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: The last digit of a decimal number must be [1-9]. Error at line " + row + " at column " + (col + i) + ".");
                            System.exit(0);
                        }

                        if (!added) {
                            System.out.println("\u001B[31mLexing Error\u001B[0m: Expected a digit after the decimal point. Error at line " + row + " at column " + (col + i) + ".");
                            System.exit(0);
                        }
                    }

                    Token token = new Token(id, "CONST", temp.toString(), row, (col));
                    tokens.add(token);
                    col = col + temp.length();//subract 1??
                }

            } else if (input.charAt(i) == '<') {
                if ((i + 6) <= input.length() && input.charAt(i + 1) == ' ' && input.charAt(i + 2) == 'i' && input.charAt(i + 3) == 'n' && input.charAt(i + 4) == 'p' && input.charAt(i + 5) == 'u' && input.charAt(i + 6) == 't') {
                    Token token = new Token(id, "Keyword", "< input", row, col);
                    tokens.add(token);
                    i += 6;
                    col += 6;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"< input\", Expected \" input\" after the character \'<\' at line " + row + " between columns " + col + "and " + (col + 6) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'a') {
                if ((i + 2) <= input.length() && input.charAt(i + 1) == 'd' && input.charAt(i + 2) == 'd') {
                    Token token = new Token(id, "Keyword", "add", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else if ((i + 2) <= input.length() && input.charAt(i + 1) == 'n' && input.charAt(i + 2) == 'd') {
                    Token token = new Token(id, "Keyword", "and", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"add\" or \"and\", Expected \"dd\" or \"nd\" after the character \'a\' at line " + row + " between columns " + col + "and " + (col + 2) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'b') {
                if ((i + 4) <= input.length() && input.charAt(i + 1) == 'e' && input.charAt(i + 2) == 'g' && input.charAt(i + 3) == 'i' && input.charAt(i + 4) == 'n') {
                    Token token = new Token(id, "Keyword", "begin", row, col);
                    tokens.add(token);
                    i += 4;
                    col += 4;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"begin\", Expected \"egin\" after the character \'b\' at line " + row + " between columns " + col + "and " + (col + 4) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'd') {
                if ((i + 2) <= input.length() && input.charAt(i + 1) == 'i' && input.charAt(i + 2) == 'v') {
                    Token token = new Token(id, "Keyword", "div", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"div\", Expected \"iv\" after the character \'d\' at line " + row + " between columns " + col + "and " + (col + 2) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'e') {
                if ((i + 3) <= input.length() && input.charAt(i + 1) == 'l' && input.charAt(i + 2) == 's' && input.charAt(i + 3) == 'e') {
                    Token token = new Token(id, "Keyword", "else", row, col);
                    tokens.add(token);
                    i += 3;
                    col += 3;
                } else if ((i + 2) <= input.length() && input.charAt(i + 1) == 'n' && input.charAt(i + 2) == 'd') {
                    Token token = new Token(id, "Keyword", "end", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else if ((i + 1) <= input.length() && input.charAt(i + 1) == 'q') {
                    Token token = new Token(id, "Keyword", "eq", row, col);
                    tokens.add(token);
                    i += 1;
                    col += 1;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"end\", \"else\" or \"eq\", Expected \"nd\", \"lse\" or \"q\" after the character \'e\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'g') {
                if ((i + 2) <= input.length() && input.charAt(i + 1) == 'r' && input.charAt(i + 2) == 't') {
                    Token token = new Token(id, "Keyword", "grt", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"grt\", Expected \"rt\" after the character \'g\' at line " + row + " between columns " + col + "and " + (col + 2) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'h') {
                if ((i + 3) <= input.length() && input.charAt(i + 1) == 'a' && input.charAt(i + 2) == 'l' && input.charAt(i + 3) == 't') {
                    Token token = new Token(id, "Keyword", "halt", row, col);
                    tokens.add(token);
                    i += 3;
                    col += 3;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"halt\", Expected \"alt\" after the character \'h\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'i') {
                if ((i + 1) <= input.length() && input.charAt(i + 1) == 'f') {
                    Token token = new Token(id, "Keyword", "if", row, col);
                    tokens.add(token);
                    i += 1;
                    col += 1;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"if\", Expected \"f\" after the character \'i\' at line " + row + " between columns " + col + "and " + (col + 1) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'm') {
                if ((i + 3) <= input.length() && input.charAt(i + 1) == 'a' && input.charAt(i + 2) == 'i' && input.charAt(i + 3) == 'n') {
                    Token token = new Token(id, "Keyword", "main", row, col);
                    tokens.add(token);
                    i += 3;
                    col += 3;
                } else if ((i + 2) <= input.length() && input.charAt(i + 1) == 'u' && input.charAt(i + 2) == 'l') {
                    Token token = new Token(id, "Keyword", "mul", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"main\" or \"mul\", Expected \"ain\" or \"ul\" after the character \'m\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'n') {
                if ((i + 2) <= input.length() && input.charAt(i + 1) == 'o' && input.charAt(i + 2) == 't') {
                    Token token = new Token(id, "Keyword", "not", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else if ((i + 2) <= input.length() && input.charAt(i + 1) == 'u' && input.charAt(i + 2) == 'm') {
                    Token token = new Token(id, "Keyword", "num", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"not\" or \"num\", Expected \"ot\" or \"um\" after the character \'n\' at line " + row + " between columns " + col + "and " + (col + 2) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'o') {
                if ((i + 1) <= input.length() && input.charAt(i + 1) == 'r') {
                    Token token = new Token(id, "Keyword", "or", row, col);
                    tokens.add(token);
                    i += 1;
                    col += 1;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"or\", Expected \"r\" after the character \'o\' at line " + row + " between columns " + col + "and " + (col + 1) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'p') {
                if ((i + 4) <= input.length() && input.charAt(i + 1) == 'r' && input.charAt(i + 2) == 'i' && input.charAt(i + 3) == 'n' && input.charAt(i + 4) == 't') {
                    Token token = new Token(id, "Keyword", "print", row, col);
                    tokens.add(token);
                    i += 4;
                    col += 4;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"print\", Expected \"rint\" after the character \'p\' at line " + row + " between columns " + col + "and " + (col + 4) + ".");
                    System.exit(0);
                }
            }
            else if (input.charAt(i) == 'r')
            {
                if((i+5) <= input.length() && input.charAt(i+1) == 'e' && input.charAt(i+2) == 't' && input.charAt(i+3) == 'u' && input.charAt(i+4) == 'r' && input.charAt(i+5) == 'n')
                {
                    Token token = new Token(id, "Keyword", "return", row, col);
                    tokens.add(token);
                    i += 5;
                    col += 5;
                }
                else
                {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"return\", Expected \"eturn\" after the character \'r\' at line " + row + " between columns " + col + "and " + (col+5) + ".");
                    System.exit(0);
                }
            }
            else if (input.charAt(i) == 's') {
                if ((i + 3) <= input.length() && input.charAt(i + 1) == 'q' && input.charAt(i + 2) == 'r' && input.charAt(i + 3) == 't') {
                    Token token = new Token(id, "Keyword", "sqrt", row, col);
                    tokens.add(token);
                    i += 3;
                    col += 3;
                } else if ((i + 3) <= input.length() && input.charAt(i + 1) == 'k' && input.charAt(i + 2) == 'i' && input.charAt(i + 3) == 'p') {
                    Token token = new Token(id, "Keyword", "skip", row, col);
                    tokens.add(token);
                    i += 3;
                    col += 3;
                } else if ((i + 2) <= input.length() && input.charAt(i + 1) == 'u' && input.charAt(i + 2) == 'b') {
                    Token token = new Token(id, "Keyword", "sub", row, col);
                    tokens.add(token);
                    i += 2;
                    col += 2;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"sqrt\", \"skip\" or \"sub\", Expected \"qrt\", \"kip\" or \"ub\" after the character \'s\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 't') {
                if ((i + 3) <= input.length() && input.charAt(i + 1) == 'e' && input.charAt(i + 2) == 'x' && input.charAt(i + 3) == 't') {
                    Token token = new Token(id, "Keyword", "text", row, col);
                    tokens.add(token);
                    i += 3;
                    col += 3;
                } else if ((i + 3) <= input.length() && input.charAt(i + 1) == 'h' && input.charAt(i + 2) == 'e' && input.charAt(i + 3) == 'n') {
                    Token token = new Token(id, "Keyword", "then", row, col);
                    tokens.add(token);
                    i += 3;
                    col += 3;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"text\" or \"then\", Expected \"ext\" or \"hen\" after the character \'t\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
                    System.exit(0);
                }
            } else if (input.charAt(i) == 'v') {
                if ((i + 3) <= input.length() && input.charAt(i + 1) == 'o' && input.charAt(i + 2) == 'i' && input.charAt(i + 3) == 'd') {
                    Token token = new Token(id, "Keyword", "void", row, col);
                    tokens.add(token);
                    i += 3;
                    col += 3;
                } else {
                    System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"void\", Expected \"oid\" after the character \'v\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
                    System.exit(0);
                }
            } else {
                System.out.println("\u001B[31mLexing Error\u001B[0m: Invalid character which is not part of the language NOR a start of a token. Error at line " + row + " at column " + col + ".");
                System.exit(0);
            }
        }

        return tokens;
    }

    public int FindEndTheEndOfTheToken_Function_Variable(String input, int startIndex)//test
    {
        int index = startIndex;

        while (Character.isLowerCase(input.charAt(index)) || Character.isDigit(input.charAt(index))) {
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
        for (String s : regex) {
            pattern = Pattern.compile(s);
            matcher = pattern.matcher(input);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;

    }
}
/*
    public String CheckIfItIsAKeyword(Character first, String input, int i)//tested
    {
        String[] keyword_5 = {
                "begin", "print"
        };

        String[] keyword_4 = {
                "main", "skip", "halt", "sqrt", "void", "else", "text", "then"
        };

        String[] keyword_3 = {
                "end", "num", "and", "not", "mul", "div", "add", "sub", "grt"
        };

        String[] keyword_2 = {
                "if", "or", "eq"
        };

        String trimmed = "";

        if((i+4) <= input.length() && (first == 'b' || first == 'p'))
        {
            trimmed = input.substring(i, i+5);
            for (String s : keyword_5)
            {
                if (trimmed.equals(s)) {
                    return s;
                }
            }
        }

        if((i+3) <= input.length() && (first == 'm' || first == 's' || first == 'h' || first == 'v' || first == 'e' || first == 't'))
        {
            trimmed = input.substring(i, i+4);
            for (String s : keyword_4)
            {
                if (trimmed.equals(s)) {
                    return s;
                }
            }
        }

        if((i+2) <= input.length() && (first == 'e' || first == 'n' || first == 'a' || first == 's' || first == 'g' || first == 'm' || first == 'd'))
        {
            trimmed = input.substring(i, i+3);
            for (String s : keyword_3)
            {
                if (trimmed.equals(s)) {
                    return s;
                }
            }
        }

        if((i+1) <= input.length() && (first == 'i' || first == 'o' || first == 'e'))
        {
            for (String s : keyword_2)
            {
                if (input.equals(s)) {
                    return s;
                }
            }
        }

        //if all fails
        return ErrorMessages(first);

    }

    public String ErrorMessages(Character first)
    {
        if(first == 'a')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"add\" or \"and\", Expected \"dd\" or \"nd\" after the character \'a\' at line " + row + " between columns " + col + "and " + (col+2) + ".");
            System.exit(0);

        }
        else if(first == 'b')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"begin\", Expected \"egin\" after the character \'b\' at line " + row + " between columns " + col + "and " + (col+4) + ".");
            System.exit(0);
        }
        else if(first == 'd')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"div\", Expected \"iv\" after the character \'d\' at line " + row + " between columns " + col + "and " + (col+2) + ".");
            System.exit(0);
        }
        else if(first == 'e')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"end\", \"else\" or \"eq\", Expected \"nd\", \"lse\" or \"q\" after the character \'e\' at line " + row + " between columns " + col + "and " + (col+3) + ".");
            System.exit(0);
        }
        else if(first == 'g')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"grt\", Expected \"rt\" after the character \'g\' at line " + row + " between columns " + col + "and " + (col+2) + ".");
            System.exit(0);
        }
        else if(first == 'h')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"halt\", Expected \"alt\" after the character \'h\' at line " + row + " between columns " + col + "and " + (col+3) + ".");
            System.exit(0);
        }
        else if(first == 'i')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"if\", Expected \"f\" after the character \'i\' at line " + row + " between columns " + col + "and " + (col+1) + ".");
            System.exit(0);
        }
        else if(first == 'm')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"main\" or \"mul\", Expected \"ain\" or \"ul\" after the character \'m\' at line " + row + " between columns " + col + "and " + (col+3) + ".");
            System.exit(0);
        }
        else if(first == 'n') {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"not\" or \"num\", Expected \"ot\" or \"um\" after the character \'n\' at line " + row + " between columns " + col + "and " + (col + 2) + ".");
            System.exit(0);
        }
        else if(first == 'o')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"or\", Expected \"r\" after the character \'o\' at line " + row + " between columns " + col + "and " + (col + 1) + ".");
            System.exit(0);
        }
        else if(first == 'p')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"print\", Expected \"rint\" after the character \'p\' at line " + row + " between columns " + col + "and " + (col + 4) + ".");
            System.exit(0);
        }
        else if(first == 's')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"sqrt\", \"skip\" or \"sub\", Expected \"qrt\", \"kip\" or \"ub\" after the character \'s\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
            System.exit(0);
        }
        else if(first == 't')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For tokens \"text\" or \"then\", Expected \"ext\" or \"hen\" after the character \'t\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
            System.exit(0);
        }
        else if(first == 'v')
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: For token \"void\", Expected \"oid\" after the character \'v\' at line " + row + " between columns " + col + "and " + (col + 3) + ".");
            System.exit(0);
        }
        else
        {
            System.out.println("\u001B[31mLexing Error\u001B[0m: Invalid character which is not part of the language NOR a start of a token. Error at line " + row + " at column " + col + ".");
            System.exit(0);
        }

        return "";
    }
}


//            else if(Character.isLowerCase(input.charAt(i)))
//            {
//                String keyword = CheckIfItIsAKeyword(input.charAt(i), input, i);
//                if(keyword.length() > 1)
//                {
//                    Token token = new Token(id,"Keyword",keyword,row,col);
//                    tokens.add(token);
//                    i += keyword.length()-1;
//                    col += keyword.length()-1;
//                }
//                else
//                {
//                    System.out.println("\u001B[31mLexing Error\u001B[0m: Invalid character which is not part of the language NOR a start of a token. Error at line " + row + " at column " + col + ".");
//                    System.exit(0);
//                }
//            }

 */