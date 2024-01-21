import java.util.LinkedList;
import java.util.Optional;

public class BlockNodes extends Node{
    private LinkedList<Node> statement = new LinkedList<>();
    private Optional<Node> condition = Optional.empty();

    BlockNodes(LinkedList<Node> statementNodes, Optional<Node> cond){
        statement.addAll(statementNodes);
        condition = cond;
    }
    BlockNodes(LinkedList<Node> statementNodes){
        statement.addAll(statementNodes);
    }
    LinkedList<Node> GetStatements(){
        return statement;
    }
    Node GetCondition(){
        if(condition.isPresent()){
            return condition.get();
        }
        return null;
    }
    public String ToString(){
        String s = "BlockNode: ";
        if (!statement.isEmpty()){
            s += "Statement: " + statement.getFirst().ToString();
        }
        if (condition.isPresent()){
            s += "BlockNode: Condition: " + condition.get().ToString();
        }
        return s;
    }
}
