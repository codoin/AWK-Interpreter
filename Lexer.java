import java.util.LinkedList;

public class Lexer {
    StringHandler handler;
    int line = 0, charPosition = 0;
    String word = ""; //concat until make token

    public Lexer (String wholeString){
        handler = new StringHandler(wholeString);
    }

    LinkedList<Token> Lex() throws Exception{ //return linkedlist
        LinkedList<Token> data = new LinkedList<Token>();

        while (!(handler.IsDone())){
            
            if(handler.Peek(charPosition)==' '){//skips spaces
                handler.Swallow(1);
                System.out.println("space");//test
            }
            else if(handler.PeekString(charPosition+1)=="/t"){//skips tabs
                handler.Swallow(2);
            }
            else if(Character.isDigit(handler.Peek(charPosition)) || handler.Peek(charPosition)=='.'){
                data.add(ProcessNumber());
                word="";
            }
            else if(Character.isLetter(handler.Peek(charPosition))){
                data.add(ProcessWord());
                word="";
            }
            else if(handler.PeekString(charPosition+1)=="/n"){//if linefeed line++ swallow char position charPosition = 0 
                data.add(new Token(Token.TokenTypes.SEPERATOR,line,charPosition));
                line++;
                handler.Swallow(charPosition+1);
                charPosition=0;
            }   
            else{
                throw new Exception("Error: Special Character");
            }          
        }
        return data;
    }
    Token ProcessWord() {
        int position = charPosition;
        while(!(handler.IsDone()) && Character.isLetter(handler.Peek(charPosition)) || handler.Peek(charPosition)=='_' || Character.isDigit(handler.Peek(charPosition))){
            word+=handler.GetChar();
            charPosition++;
            System.out.println(handler.Peek(0) + " " + charPosition);//test
        }
        return new Token(Token.TokenTypes.WORD, line, position, word);
    }
    Token ProcessNumber() throws Exception{
        int position = charPosition, periods = 0;
        while(!(handler.IsDone()) && Character.isDigit(handler.Peek(charPosition)) || handler.Peek(charPosition)=='.'){
            word+=handler.GetChar();
            charPosition++;
            if (handler.Peek(charPosition)=='.') {
                periods++;
            }
        }
        if (periods > 1){
            throw new Exception("Error: Not a Number");
        }
        charPosition+=position;
        return new Token(Token.TokenTypes.NUMBER, line, position, word);
    }
    
}

