import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNodes {
    boolean variadic;
    String name;

    BuiltInFunctionDefinitionNode(String functname, Collection<String> param, BlockNodes statements,Boolean var) {
        super(functname, param, statements);
        variadic = var;
    }
    BuiltInFunctionDefinitionNode(String functname,Boolean var) {
        super(functname, null, null);
        variadic = var;
    }
    
    boolean GetVariadic(){
        return variadic;
    }
    String GetName(){
        return name;
    }
    public Function<HashMap<String,InterpreterDataType>,String> Execute;
}
