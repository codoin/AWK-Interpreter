import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    //processes awk code and runs awk
    //Example command in terminal: javac Main.java 
    //Main code.awk file.txt
    public static void main(String[] args) throws Exception {
        //takes in arguements
        Path awk = Paths.get(args[0]);
        String text = "";
        if(!(args[1].isBlank())){
            text = Paths.get(args[1])+"";
        }

        //reads awk file as string
        String content = new String(Files.readAllBytes(awk));

        Lexer lex = new Lexer(content);
        Parser parse = new Parser(lex.Lex());
        ProgramNode program = (parse.Parse());

        //interpreter takes in parsed awk program and text file path
        Interpreter inter = new Interpreter(program,text);
        inter.InterpretProgram();
    }
}
