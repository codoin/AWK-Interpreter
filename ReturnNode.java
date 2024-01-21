import java.util.Optional;

public class ReturnNode extends StatementNode{
    Optional<Node> parameter;

    ReturnNode(Optional<Node> param){
        parameter = param;
    }

    Node GetValue(){
        if(parameter.isPresent()){
            return parameter.get();
        }
        else {
            return null;
        }
    }
    String ToString(){
        String s = "Return ";
        if (parameter.isPresent()){
            s += parameter.get().ToString();
        }
        return s;
    }
}
