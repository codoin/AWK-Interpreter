import java.util.LinkedList;

public class ForEachNode extends StatementNode{
    BlockNodes statements;
    Node condition;

    ForEachNode(Node cond, BlockNodes state){
        statements = state;
        condition = cond;
    }

    Node GetArray(){
        return condition;
    }
    LinkedList<Node> GetBlock(){
        return statements.GetStatements();
    }
    String ToString(){
        return "For In: " + statements.ToString();
    }
}
