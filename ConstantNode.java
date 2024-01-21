public class ConstantNode extends Node{
    private String value;

    ConstantNode(String val){
        value = val;
    }
    String GetValue(){
        return value;
    }
    String ToString(){
        return "Constant: " + value;
    }
}
