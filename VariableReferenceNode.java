import java.util.Optional;

public class VariableReferenceNode extends Node{
    private String variable;
    private Optional<Node> indexExpression = Optional.empty();

    VariableReferenceNode(String name, Optional<Node> index){
        variable = name;
        indexExpression = index;
    }
    VariableReferenceNode(String name){
        variable = name;
    }
    String GetVariable(){
        return variable;
    }
    Optional<Node> GetIndex(){
        return indexExpression;
    }
    String ToString(){
        if (indexExpression.isPresent()){
            return "VariableReferenceNode: " + variable;
        }
        return "VariableReferenceNode: " + variable;
    }
}
