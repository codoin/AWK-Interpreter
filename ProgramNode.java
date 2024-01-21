import java.util.LinkedList;

public class ProgramNode extends Node{
    LinkedList<BlockNodes> beginNode;
    LinkedList<BlockNodes> endNode;
    LinkedList<BlockNodes> otherNode;
    LinkedList<FunctionDefinitionNodes> functionNode;

    ProgramNode(){
        beginNode = new LinkedList<>();
        endNode = new LinkedList<>();
        otherNode = new LinkedList<>();
        functionNode = new LinkedList<>();
    }

    LinkedList<BlockNodes> GetBeginNodes(){
        return beginNode;
    }
    LinkedList<BlockNodes> GetEndNodes(){
        return endNode;
    }
    LinkedList<BlockNodes> GetOtherNodes(){
        return otherNode;
    }
    LinkedList<FunctionDefinitionNodes> GetFunctionNodes(){
        return functionNode;
    }
    
    public String ToString(){
        String s = "(PROGRAM)";
        if (!beginNode.isEmpty()){
            s += " Begin Node: " + beginNode.getFirst().ToString();
        }
        if (!endNode.isEmpty()){
            s += "End Node: " + endNode.getFirst().ToString();
        }
        if (!otherNode.isEmpty()){
            s += "Other Node: " + otherNode.getFirst().ToString();
        }
        if (!functionNode.isEmpty()){
            s += "Funtion Node: " + functionNode.getFirst().ToString();
        }
        return s;
    }
}
