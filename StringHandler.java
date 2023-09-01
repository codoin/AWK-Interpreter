class StringHandler {
    private String document;
    private int index = 0;

    public StringHandler(String string){
        document = string;
    }
    public char Peek (int i){
        return document.charAt(index+i);
    }
    public String PeekString(int i){
        return document.substring(index,index+i);
    }
    public char GetChar(){
        index++;
        return document.charAt(index-1);
    }
    public void Swallow(int i){
        index+=i;
    }
    public boolean IsDone(){
        if (document.length()==0 || document.length()-2==index){
            return true;
        }
        else {
            return false;
        }
    }
    public String Remainder(){
        return document.substring(index);
    }
}