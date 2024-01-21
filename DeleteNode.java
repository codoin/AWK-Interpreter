import java.util.LinkedList;
import java.util.Optional;

public class DeleteNode extends StatementNode{
    
    Optional<Node> deleteArray;
    LinkedList<Optional<Node>> indices = null;

    DeleteNode(Optional<Node> array){
        deleteArray = array;
    }
    DeleteNode(Optional<Node> array, LinkedList<Optional<Node>> param){
        deleteArray = array;
        indices = param;
    }

    Node GetArray(){
        if(deleteArray.isPresent()){
            return deleteArray.get();
        }
        return null;
    }
    LinkedList<Optional<Node>> GetIndex(){
        return indices;
    }

    String ToString(){
        return "Delete " + deleteArray.get().ToString();
    }
}
