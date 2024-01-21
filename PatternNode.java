public class PatternNode extends Node{
    private String pattern;

    PatternNode(String value){
        pattern = value;
    }
    String GetPattern() {
        return pattern;
    }
    String ToString(){
        return "Pattern: " + pattern;
    }
}
