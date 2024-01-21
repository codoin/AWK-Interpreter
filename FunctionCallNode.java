import java.util.LinkedList;
import java.util.Optional;

public class FunctionCallNode extends StatementNode{
    Token name;
    LinkedList<Optional<Node>> parameters;

    FunctionCallNode(Token function, LinkedList<Optional<Node>> param){
        name = function;
        parameters = param;
    }
    String GetName(){
        return name.GetValue();
    }
    LinkedList<Optional<Node>> GetParam(){
        return parameters;
    }

    String ToString(){
        return "FunctionCall: " + name.TokenValue + " " + parameters.getFirst().get().ToString();
    }
}
