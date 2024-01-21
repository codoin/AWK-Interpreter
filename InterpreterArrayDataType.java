import java.util.Collection;
import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType{
    private HashMap<String,InterpreterDataType> array;

    InterpreterArrayDataType(Collection<String> list){
        array = new HashMap<String,InterpreterDataType>();
        int num = 0;
        for (String s:list){
            array.put(num+++"",new InterpreterDataType(s));
        }
    }
    InterpreterArrayDataType(String index, InterpreterDataType idt){
        array = new HashMap<String,InterpreterDataType>();
        array.put(index,idt);
    }

    HashMap<String,InterpreterDataType> getArray(){
        return array;
    }
}
