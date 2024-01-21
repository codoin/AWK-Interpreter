class StringHandler {
    private String document;
    private int index = 0;

    public StringHandler(String string){ //constructor
        document = string;
    }
    public char Peek (int i){//looks ahead i positons
        return document.charAt(index+i);
    }
    public String PeekString(int i){ //return certian subtring of the documents beginning at position i
        if (index+1+i > document.length()){
            return "out of bounds";
        }
        return document.substring(index,index+1+i);
    }
    public char GetChar(){//get current character
        index++;
        return document.charAt(index-1);
    }
    public void Swallow(int i){//moves pointer i positions
        index+=i;
    }
    public boolean IsDone(){//when file is empty returns true
        if (document.length()==0 || document.length()==index){
            return true;
        }
        else {
            return false;
        }
    }
    public String Remainder(){ //return rest of list not lexed yet
        return document.substring(index);
    }
}