public class TernaryNode extends Node{
    private Node BoolExpression, trueCase, falseCase;

    TernaryNode(Node expression, Node tru, Node fals){
        BoolExpression = expression;
        trueCase = tru;
        falseCase = fals;
    }
    Node GetExpression(){
        return BoolExpression;
    }
    Node GetTrueCase(){
        return trueCase;
    }
    Node GetFalseCase(){
        return falseCase;
    }
    String ToString(){
        return "TernaryNode: " + BoolExpression.ToString() + " TrueCase: " + trueCase.ToString() + " FalseCase: " + falseCase.ToString();
    }
}
