import java.util.Collection;

public class FunctionDefinitionNodes extends Node{
    private String name;
    private Collection<String> parameter;
    private BlockNodes statementList;

    FunctionDefinitionNodes(String functname, Collection<String> param, BlockNodes statements){
        name = functname;
        parameter = param;
        statementList = statements;
    }

    String getName(){
        return name;
    }
    Collection<String> getParam(){
        return parameter;
    }
    BlockNodes getStatements(){
        return statementList;
    }

    public String ToString(){
        return "(Function) " + name + "\n\tParameter: " + parameter.toString() + "\n\tStatement: " + statementList.toString();
    }
}
