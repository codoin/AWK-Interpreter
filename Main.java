import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws Exception {
        GetAllBytes("File.txt");
    }
    static void GetAllBytes(String someFile) throws Exception{ //print out tokens from Lex method
        Path myPath = Paths.get(someFile);
        String content = new String(Files.readAllBytes(myPath));
        //System.out.println(content);
        Lexer lex = new Lexer(content);
        LinkedList<Token> list = lex.Lex();
        for (Token t : list) {
            System.out.print(t.toString());
        }
    }
}
