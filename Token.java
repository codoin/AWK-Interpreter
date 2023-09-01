public class Token {
    enum TokenTypes {WORD, NUMBER, SEPERATOR};
    TokenTypes token;
    String TokenValue;
    int linenum, charposition;
    
    Token (TokenTypes type, int line, int position){
        token = type;
        linenum = line;
        charposition = position;
    }
    Token (TokenTypes type, int line, int position, String TokenVal){
        token = type;
        linenum = line;
        charposition = position;
        TokenValue = TokenVal;
    }
    String ToString(){
        if (token == TokenTypes.SEPERATOR){
            return token.toString();
        }
        else{
            return token + " (" + TokenValue + ") ";
        }
    }
}
