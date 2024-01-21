import java.util.LinkedList;

public class DoWhileNode extends StatementNode{
    Node condition;
    BlockNodes statements;

    DoWhileNode(Node cond, BlockNodes state){
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
        return "DoWhile " + condition.ToString();
    }
}
