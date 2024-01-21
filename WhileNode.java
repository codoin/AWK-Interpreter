import java.util.LinkedList;

public class WhileNode extends StatementNode{
    Node condition;
    BlockNodes statements;

    WhileNode(Node cond, BlockNodes state){
        condition = cond;
        statements = state;
    }
    Node GetCondition(){
        return condition;
    }
    LinkedList<Node> GetBlock(){
        return statements.GetStatements();
    }

    String ToString(){
        return "While " + condition.ToString();
    }
}
