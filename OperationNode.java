import java.util.LinkedList;
import java.util.Optional;

public class OperationNode extends StatementNode{
    private Node left;
    private Optional<Node> right = Optional.empty();
    enum operations {EQ, NE, LT, LE, GT, GE, AND, OR, NOT, MATCH, NOTMATCH, DOLLAR,
        PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS, UNARYNEG, IN,
        EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, CONCATENATION} //list of operations
    LinkedList<operations> operationlist = new LinkedList<operations>();

    OperationNode(Node leftValue, Optional<Node> rightValue, operations op){
        left = leftValue;
        right = Optional.of(rightValue.get());
        operationlist.add(op);
    }
    OperationNode(Node leftValue, operations op){
        left = leftValue;
        operationlist.add(op);
    }
    String GetOperation(){
        return operationlist.getFirst().toString();
    }
    Node GetLeft(){
        return left;
    }
    Node GetRight(){
        if(right.isPresent()){
            return right.get();
        }
        return null;
    }
    String ToString(){
        if (right.isPresent()){
            return "OperationNode: " + left.ToString() + " Operation: " + operationlist.getFirst() + " " + right.get().ToString();
        }
        return "OperationNode: " + left.ToString() + " Operation: " + operationlist.getFirst();
    }
}
