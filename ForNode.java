import java.util.LinkedList;
import java.util.Optional;

public class ForNode extends StatementNode{
    BlockNodes statements;
    Optional<Node> condition1 = Optional.empty();
    Optional<Node> condition2 = Optional.empty();
    Optional<Node> condition3 = Optional.empty();

    ForNode(Optional<Node> cond1, Optional<Node> cond2, Optional<Node> cond3, BlockNodes state){
        statements = state;
        condition1 = cond1;
        condition2 = cond2;
        condition3 = cond3;
    }
    LinkedList<Node> GetBlock(){
        return statements.GetStatements();
    }
    Node GetInitial(){
        if(condition1.isPresent()){
            return condition1.get();
        }
        return null;
    }
    Node GetCondition(){
        if(condition2.isPresent()){
            return condition2.get();
        }
        return null;
    }
    Node GetIncrement(){
        if(condition3.isPresent()){
            return condition3.get();
        }
        return null;
    }
    String ToString(){
        return "For: " + statements.ToString();
    }
}
