import java.util.LinkedList;
import java.util.HashMap;

public class Lexer {
    StringHandler handler;
    int line = 0, charPosition = 1;
    HashMap<String, Token.TokenTypes> keywords;
    HashMap<String, Token.TokenTypes> oneSymbol;
    HashMap<String, Token.TokenTypes> twoSymbol;

    public Lexer (String wholeString){ //creates string handler and hashmaps of special words/symbols
        handler = new StringHandler(wholeString);
        keywords = new HashMap<String, Token.TokenTypes>();
        oneSymbol = new HashMap<String, Token.TokenTypes>();
        twoSymbol = new HashMap<String, Token.TokenTypes>();
        putWords();
        putSymbol();
    }

    void putWords(){ //list of keywords in hashmap
        keywords.put("while", Token.TokenTypes.WHILE);
        keywords.put("if", Token.TokenTypes.IF);
        keywords.put("do", Token.TokenTypes.DO);
        keywords.put("for", Token.TokenTypes.FOR);
        keywords.put("break", Token.TokenTypes.BREAK);
        keywords.put("continue", Token.TokenTypes.CONTINUE);
        keywords.put("else", Token.TokenTypes.ELSE);
        keywords.put("return", Token.TokenTypes.RETURN);
        keywords.put("BEGIN", Token.TokenTypes.BEGIN);
        keywords.put("END", Token.TokenTypes.END);
        keywords.put("print", Token.TokenTypes.PRINT);
        keywords.put("printf", Token.TokenTypes.PRINTF);
        keywords.put("next", Token.TokenTypes.NEXT);
        keywords.put("in", Token.TokenTypes.IN);
        keywords.put("delete", Token.TokenTypes.DELETE);
        keywords.put("getline", Token.TokenTypes.GETLINE);
        keywords.put("exit", Token.TokenTypes.EXIT);
        keywords.put("nextfile", Token.TokenTypes.NEXTFILE);
        keywords.put("function", Token.TokenTypes.FUNCTION);
    }

    void putSymbol(){ //list of symbols in hashmap
        twoSymbol.put(">=", Token.TokenTypes.GREATEREQUAL);
        twoSymbol.put("++", Token.TokenTypes.PLUSPLUS);
        twoSymbol.put("--", Token.TokenTypes.MINUSMINUS);
        twoSymbol.put("<=", Token.TokenTypes.LESSEQUAL);
        twoSymbol.put("==", Token.TokenTypes.EQUALEQUAL);
        twoSymbol.put("!=", Token.TokenTypes.NOTEQUAL);
        twoSymbol.put("^=", Token.TokenTypes.POWEREQUAL);
        twoSymbol.put("%=", Token.TokenTypes.MODEQUAL);
        twoSymbol.put("*=", Token.TokenTypes.MULTEQUAL);
        twoSymbol.put("/=", Token.TokenTypes.DIVEQUAL);
        twoSymbol.put("+=", Token.TokenTypes.PLUSEQUAL);
        twoSymbol.put("-=", Token.TokenTypes.MINUSEQUAL);
        twoSymbol.put("!~", Token.TokenTypes.NOTMATCH);
        twoSymbol.put("&&", Token.TokenTypes.AND);
        twoSymbol.put(">>", Token.TokenTypes.APPEND);
        twoSymbol.put("||", Token.TokenTypes.OROR);
        oneSymbol.put("{", Token.TokenTypes.STARTCURLYBRACE);
        oneSymbol.put("}", Token.TokenTypes.ENDCURLYBRACE);
        oneSymbol.put("[", Token.TokenTypes.STARTSQUAREBRACE);
        oneSymbol.put("]", Token.TokenTypes.ENDSQUAREBRACE);
        oneSymbol.put("(", Token.TokenTypes.STARTPARENTHESIS);
        oneSymbol.put(")", Token.TokenTypes.ENDPARENTHESIS);
        oneSymbol.put("$", Token.TokenTypes.DOLLARSIGN);
        oneSymbol.put("~", Token.TokenTypes.TILDA);
        oneSymbol.put("=", Token.TokenTypes.EQUAL);
        oneSymbol.put("<", Token.TokenTypes.LESSTHAN);
        oneSymbol.put(">", Token.TokenTypes.GREATERTHAN);
        oneSymbol.put("!", Token.TokenTypes.NOT);
        oneSymbol.put("+", Token.TokenTypes.PLUS);
        oneSymbol.put("^", Token.TokenTypes.POWER);
        oneSymbol.put("-", Token.TokenTypes.MINUS);
        oneSymbol.put("?", Token.TokenTypes.TERNARY);
        oneSymbol.put(":", Token.TokenTypes.COLON);
        oneSymbol.put("*", Token.TokenTypes.MULTIPLY);
        oneSymbol.put("/", Token.TokenTypes.DIVIDE);
        oneSymbol.put("%", Token.TokenTypes.MODULO);
        oneSymbol.put(";", Token.TokenTypes.SEPERATOR);
        oneSymbol.put("\n", Token.TokenTypes.SEPERATOR);
        oneSymbol.put("|", Token.TokenTypes.OR);
        oneSymbol.put(",", Token.TokenTypes.COMMA);
    }

    LinkedList<Token> Lex() throws Exception{ //goes through file and makes a list of tokens
        LinkedList<Token> data = new LinkedList<Token>();

        while (!(handler.IsDone())){ //runs until file is empty
            
            if(handler.Peek(0)==' '){//skips spaces
                handler.Swallow(1);
                charPosition++;
            }
            else if(handler.Peek(0)=='\t'){//skips tabs
                handler.Swallow(1);
                charPosition+=1;
            }
            else if(Character.isDigit(handler.Peek(0)) || handler.Peek(0)=='.'){//finds a digit or decimal
                data.add(ProcessNumber());
            }
            else if(Character.isLetter(handler.Peek(0))){//finds a letter
                data.add(ProcessWord());
            }
            else if(handler.Peek(0)=='\r'){//finds carriage return
                charPosition++;
                handler.Swallow(1);
            }
            else if(handler.Peek(0)=='\n'){//finds linefeed, increments line number, swallow char, set char position to 0
                data.add(new Token(Token.TokenTypes.SEPERATOR,line,charPosition));
                line++;
                handler.Swallow(1);
                charPosition=0;
            }
            else if(handler.Peek(0)=='#'){ //finds comment
                ProcessComment();
            }
            else if(handler.Peek(0)=='"'){ //find string literals
                data.add(HandleStringLiteral());
            }
            else if(handler.Peek(0)=='`'){ //finds patterns
                data.add(HandlePattern());
            }
            else{ //last case finds symbol or throw exception
                data.add(ProcessSymbol());
            }          
        }
        return data;
    }
    private Token ProcessWord() { //goes through file until the end of word and creates a token
        int position = charPosition;
        String word = "";
        while(!(handler.IsDone()) && (Character.isLetter(handler.Peek(0)) || handler.Peek(0)=='_' || Character.isDigit(handler.Peek(0)))){ //while lexer is not done, check for letters or numbers
            word+=handler.GetChar();
            charPosition++;
        }
        if (keywords.containsKey(word)){ //if the word is a keyword return as symbol
            return new Token(keywords.get(word),line, position, word);
        }
        else{
            return new Token(Token.TokenTypes.WORD, line, position, word); //returns word token
        }
    }
    private Token ProcessNumber() throws Exception{ //goes through file until end of number and creates a token
        int position = charPosition, periods = 0;
        String number = "";
        while(!(handler.IsDone()) && (Character.isDigit(handler.Peek(0)) || handler.Peek(0)=='.')){ //while list lexer is not done, check if digit or decimal
            number+=handler.GetChar();
            charPosition++;
            if (!(handler.IsDone()) && handler.Peek(0)=='.') {
                periods++;
            }
        }
        if (periods > 1){ //if theres multiple decimal point return exception
            throw new Exception("Error: Not a Number");
        }
        charPosition+=position;
        return new Token(Token.TokenTypes.NUMBER, line, position, number);
    }
    private void ProcessComment(){ //goes through file until end of commment and creates a token
        while (!(handler.IsDone()) && !(handler.Peek(0)=='\n')){
            handler.Swallow(1);  
        }
    }
    private Token HandleStringLiteral() throws Exception{ //goes through file until end of string literal and creates a token
        int position = charPosition;
        String string = "";
        handler.Swallow(1);
        while(!(handler.IsDone())){
            if (handler.Peek(0)=='\\'){ //checks for escaped quotes
                handler.Swallow(1);
                charPosition++;
                if (handler.Peek(0)=='"'){
                    string += handler.GetChar();
                    charPosition++;
                }
            }
            else {
                if (!(handler.Peek(0)=='"')){ //takes in characters
                    string += handler.GetChar();
                    charPosition++;
                }
                else { //creates string literal token
                    handler.Swallow(1); 
                    charPosition++;
                    return new Token(Token.TokenTypes.STRINGLITERAL, line, position, string);
                }
            }
        }
        throw new Exception("Error: Not Complete String");
    }
    private Token HandlePattern() throws Exception{ //goes through file until end of pattern and creates a token
        int position = charPosition;
        String string = "";
        handler.Swallow(1);
        while (!(handler.IsDone())){
            if (handler.Peek(0)=='\\'){ //checks for escaped patterns
                handler.Swallow(1);
                charPosition++;
                if (handler.Peek(0)=='`'){
                    string += handler.GetChar();
                    charPosition++;
                }
            }
            else {
                if (!(handler.Peek(0)=='`')){ //takes in code in pattern
                    string += handler.GetChar();
                    charPosition++;
                }
                else { //creates pattern token
                    handler.Swallow(1);
                    charPosition++;
                    return new Token(Token.TokenTypes.PATTERN, line, position, string); 
                }
            }
        }
        throw new Exception("Error: Not Complete Pattern");
    }
    private Token ProcessSymbol() throws Exception{ //goes through file until end of symbol and creates a token or throw exception
        
        int position = charPosition;
        String word = "";

        while (!(handler.IsDone())){
            if (twoSymbol.containsKey(handler.PeekString(1))){ //checks for two character symbol
                word += handler.GetChar();
                word += handler.GetChar();
                return new Token(twoSymbol.get(word), line, position, word);
            }
            else if (oneSymbol.containsKey(handler.PeekString(0))){ //else checks for one character symbol
                word += handler.GetChar();
                return new Token(oneSymbol.get(word), line, position, word);
            }
        }
        throw new Exception("Error: Special Character");
    }
}

