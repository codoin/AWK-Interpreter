import java.util.LinkedList;
import java.util.Optional;

public class IfNode extends StatementNode{
    Optional<Node> ifCondition = Optional.empty();
    BlockNodes statements;
    Optional<Node> ifNext = Optional.empty();

    IfNode(BlockNodes statements){
        this.statements = statements;
    }
    IfNode(Optional<Node> condition, BlockNodes statements){
        ifCondition = condition;
        this.statements = statements;
    }
    IfNode(Optional<Node> condition, BlockNodes statements, Optional<Node> next){
        ifCondition = condition;
        this.statements = statements;
        ifNext = next;
    }

    Node GetCondition(){
        if(ifCondition.isPresent()){
            return ifCondition.get();
        }
        return null;
    }
    Node GetNext(){
        if(ifNext.isPresent()){
            return ifNext.get();
        }
        return null;
    }
    LinkedList<Node> GetBlock(){
        return statements.GetStatements();
    }
    String ToString(){
        if (ifCondition.isPresent()){
            if (ifNext.isPresent()){
                return "IfNode: " + ifCondition.get().ToString() + " " + statements.ToString() + " " + ifNext.get().ToString();
            }
            return "IfNode: " + ifCondition.get().ToString() + statements.ToString();
        }
        return "IfNode: " + statements.ToString();
    }
}
