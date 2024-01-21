import java.util.LinkedList;
import java.util.Optional;

public class TokenManager {
    private LinkedList<Token> tokenlist; 
    
    TokenManager(LinkedList<Token> list){
        tokenlist = list;
    }
    Optional<Token> Peek(int j){ //peeks j tokens from current head
        if (j < tokenlist.size()){
            return Optional.ofNullable(tokenlist.get(j));
        }
        return Optional.empty();
    }
    boolean MoreTokens(){ //checks if the list is empty
        if (tokenlist.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }
    Optional<Token> MatchAndRemove(Token.TokenTypes t){ //sees if current token in list matches parameter if so removes from list
        Token removedtoken;
        if (MoreTokens() && tokenlist.getFirst().GetToken().equals(t)){
            removedtoken = tokenlist.getFirst();
            tokenlist.removeFirst();
            return Optional.of(removedtoken);
        }
        else{
            return Optional.empty();
        }
    }
}