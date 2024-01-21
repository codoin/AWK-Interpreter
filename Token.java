public class Token {
    public enum TokenTypes {WORD, NUMBER, SEPERATOR, WHILE, IF, DO, FOR, BREAK, CONTINUE, ELSE, RETURN, BEGIN, END, PRINT, PRINTF, NEXT, IN, DELETE, GETLINE, EXIT, NEXTFILE, FUNCTION, STRINGLITERAL, PATTERN, GREATEREQUAL, PLUSPLUS, MINUSMINUS, LESSEQUAL, EQUALEQUAL, NOTEQUAL, POWEREQUAL, MODEQUAL, MULTEQUAL, DIVEQUAL, PLUSEQUAL, MINUSEQUAL, NOTMATCH, AND, APPEND, OROR, STARTCURLYBRACE, ENDCURLYBRACE, STARTSQUAREBRACE, ENDSQUAREBRACE, STARTPARENTHESIS, ENDPARENTHESIS, DOLLARSIGN, TILDA, EQUAL, LESSTHAN, GREATERTHAN, NOT, PLUS, POWER, MINUS, TERNARY, COLON, MULTIPLY, DIVIDE, MODULO, OR, COMMA};
    TokenTypes token;
    String TokenValue;
    int linenum, charposition;
    
    Token (TokenTypes type, int line, int position){ //constructor
        token = type;
        linenum = line;
        charposition = position;
    }
    Token (TokenTypes type, int line, int position, String TokenVal){ //should call other constructor and then add value
        token = type;
        linenum = line;
        charposition = position;
        TokenValue = TokenVal;
    }
    String ToString(){ //format of tokens
        if (token == TokenTypes.SEPERATOR){
            return token.toString() + " ";
        }
        else{
            return token + " (" + TokenValue + ") ";
        }
    }
    TokenTypes GetToken(){ //return token type
        return token;
    }
    String GetValue(){
        return TokenValue;
    }
}
