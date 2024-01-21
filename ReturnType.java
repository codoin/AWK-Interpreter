public class ReturnType {
    enum type {NORMAL,BREAK,CONTINUE,RETURN}
    String value;
    type returnType = type.NORMAL;

    ReturnType(type t){
        returnType = t;
    }
    ReturnType(type t, String v){
        returnType = t;
        value = v;
    }

    type GetType(){
        return returnType;
    }
    String GetValue(){
        return value;
    }
    String ToString(){
        return returnType.toString() + " " + value;
    }
}
