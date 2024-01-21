public class AssignmentNode extends StatementNode{
    private Node target, expression; 
    
    AssignmentNode(Node left, Node right){
        target = left;
        expression = right;
    }
    Node GetTarget(){
        return target;
    }
    Node GetExpression(){
        return expression;
    }
    String ToString(){
        return "Assignment: " + target.ToString() + " " + expression.ToString();
    }
}
