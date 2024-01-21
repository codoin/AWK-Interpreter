import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

//integration test
//just take in awk program and file
//some awk code was generated by ChatGPT
public class JunitCompiler {
    @Test
    public void MainTest() throws Exception{
        //from https://github.com/learnbyexample/learn_gnuawk/tree/master/example_files
        String array[] = {"awk.awk","CompilerTest.txt"};

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        Main.main(array);

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "---header---\r\n" +
            "blah blah Error\r\n" +
            "15\r\n" +
            "something went wrong\r\n" +
            "20\r\n" +
            "some more details Error\r\n" +
            "23\r\n" +
            "details about what went wrong\r\n" +
            "29\r\n" +
            "---footer---\r\n");
    }

    @Test
    public void BuiltinTest() throws Exception{
        String array[] = {"builtin.awk"," "};

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        Main.main(array);

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "String length:\r\n" + //
            "13\r\n" + //
            "Uppercase:\r\n" + //
            "HELLO, WORLD!\r\n" + //
            "Lowercase:\r\n" + //
            "hello, world!\r\n" + //
            "Substring:\r\n" + //
            "Hell\r\n" + //
            "Array elements:\r\n" + //
            "one\r\n" + //
            "two\r\n" + //
            "three\r\n" + //
            "Enter a number:\r\n" + //
            "You entered:\r\n" + //
            "0\r\n" + //
            "Formatted output: %s %.2fnHello, World!0");
    }

    @Test
    public void Builtin2Test() throws Exception{
        String array[] = {"builtin2.awk","fruit.txt"};

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        Main.main(array);

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "Using gsub:\r\n" + //
            "banana orange banana\r\n" + //
            "Using sub:\r\n" + //
            "banana orange apple\r\n");
    }

    @Test
    public void Builtin3Test() throws Exception{
        String array[] = {"builtin3.awk","sentence.txt"};

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        Main.main(array);

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "Found 'fox'\r\n" + //
            "No match found for 'elephant'\r\n");
    }

    @Test
    public void ArrayTest() throws Exception{
        String array[] = {"array.awk"," "};

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        Main.main(array);

        System.setOut(output);
        Assert.assertEquals(b.toString(),"hi\r\nthere\r\n");
    }

    @Test
    public void ConditionalBlockTest() throws Exception{
        Lexer l = new Lexer(
            "$2 >= 30 {\r\n" +
            "    print $1 \" is 30 or older.\"\r\n" +
            "}\r\n" +
            "\r\n" +
            "# If the second field (age) is less than 30, print a different message\r\n" +
            "$2 < 30 {\r\n" +
            "    print $1 \" is younger than 30.\"\r\n" +
            "}");
        Parser p = new Parser(l.Lex());
        ProgramNode program = (p.Parse());
        Interpreter inter = new Interpreter(program,"C:\\Users\\15184\\ICSI311\\Age.txt");

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        inter.InterpretProgram();

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "Alice is younger than 30.\r\n" + //
            "Bob is 30 or older.\r\n" + //
            "Charlie is younger than 30.\r\n" + //
            "David is 30 or older.\r\n");
        
    }

    @Test
    public void UserFunctionCallBlockTest() throws Exception{
        Lexer l = new Lexer(
            "# Define a custom method to calculate the square of a number\r\n" + //
            "function calculate_square(number) {\r\n" + //
            "    return number * number;\r\n" + //
            "}\r\n" + //
            "\r\n" + //
            "# Main processing starts here\r\n" + //
            "{\r\n" + //
            "    # Assuming the input contains numeric values\r\n" + //
            "    # Calculate the square using the custom method\r\n" + //
            "    square_result = calculate_square($1);\r\n" + //
            "\r\n" + //
            "    # Print the original number and its square\r\n" + //
            "    print \"Original:\", $1, \"Square:\", square_result;\r\n" + //
            "}");
        Parser p = new Parser(l.Lex());
        ProgramNode program = (p.Parse());
        Interpreter inter = new Interpreter(program,"C:\\Users\\15184\\ICSI311\\Numbers.txt");

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        inter.InterpretProgram();

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "Original:\r\n" + //
            "5\r\n" + //
            "Square:\r\n" + //
            "25.0\r\n" + //
            "Original:\r\n" + //
            "9\r\n" + //
            "Square:\r\n" + //
            "81.0\r\n");   
    }

    @Test
    public void WhileLoopTest() throws Exception{
        Lexer l = new Lexer(
            "\r\n" + //
            "# Initialize a counter variable\r\n" + //
            "BEGIN {\r\n" + //
            "    counter = 1;\r\n" + //
            "}\r\n" + //
            "\r\n" + //
            "# Process each line of input\r\n" + //
            "{\r\n" + //
            "    # While loop to print the line with a counter\r\n" + //
            "    while (counter < 5) {\r\n" + //
            "        print \"Word\", counter;\r\n" + //
            "        counter+=1;\r\n" + //
            "    }\r\n" + //
            "\r\n" + //
            "    # Reset the counter for the next line\r\n" + //
            "    counter = 1;\r\n" + //
            "}");
        Parser p = new Parser(l.Lex());
        ProgramNode program = (p.Parse());
        Interpreter inter = new Interpreter(program,"C:\\Users\\15184\\ICSI311\\Numbers.txt");

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        inter.InterpretProgram();

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "Word\r\n" + //
            "1\r\n" + //
            "Word\r\n" + //
            "2.0\r\n" + //
            "Word\r\n" + //
            "3.0\r\n" + //
            "Word\r\n" + //
            "4.0\r\n" + //
            "Word\r\n" + //
            "5.0\r\n" + //
            "Word\r\n" + //
            "1\r\n" + //
            "Word\r\n" + //
            "2.0\r\n" + //
            "Word\r\n" + //
            "3.0\r\n" + //
            "Word\r\n" + //
            "4.0\r\n" + //
            "Word\r\n" + //
            "5.0\r\n");   
    }

    @Test
    public void ForLoopTest() throws Exception{
        Lexer l = new Lexer(
            "{for (i = 0; i < 9; i++){print \"For loop:\"i}} {array[1] = 1; array[2] = 9; for(a in array){print \"ForEach loop:\"a}}");
        Parser p = new Parser(l.Lex());
        ProgramNode program = (p.Parse());
        Interpreter inter = new Interpreter(program,"C:\\Users\\15184\\ICSI311\\Numbers.txt");

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        inter.InterpretProgram();

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "For loop:0\r\n" + //
            "For loop:1.0\r\n" + //
            "For loop:2.0\r\n" + //
            "For loop:3.0\r\n" + //
            "For loop:4.0\r\n" + //
            "For loop:5.0\r\n" + //
            "For loop:6.0\r\n" + //
            "For loop:7.0\r\n" + //
            "For loop:8.0\r\n" + //
            "For loop:9.0\r\n" + //
            "ForEach loop:1\r\n" + //
            "ForEach loop:9\r\n" + //
            "For loop:0\r\n" + //
            "For loop:1.0\r\n" + //
            "For loop:2.0\r\n" + //
            "For loop:3.0\r\n" + //
            "For loop:4.0\r\n" + //
            "For loop:5.0\r\n" + //
            "For loop:6.0\r\n" + //
            "For loop:7.0\r\n" + //
            "For loop:8.0\r\n" + //
            "For loop:9.0\r\n" + //
            "ForEach loop:1\r\n" + //
            "ForEach loop:9\r\n");   
    }

    @Test
    public void MathTest() throws Exception{
        Lexer l = new Lexer(
            "{a = 23 * 5; b = a/6; c = b - 12 + 1; print c\n}");
        Parser p = new Parser(l.Lex());
        ProgramNode program = (p.Parse());
        Interpreter inter = new Interpreter(program,"C:\\Users\\15184\\ICSI311\\Numbers.txt");

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        inter.InterpretProgram();

        System.setOut(output);
        Assert.assertEquals(b.toString(),"8.166666\r\n8.166666\r\n");   
    }

    @Test
    public void LogicTest() throws Exception{
        Lexer l = new Lexer(
            "12 > 10 {print 0} {if(\"hi\" < 18) print 1} {a = 1 ; b = 2; c = 3; if(a>b){print a} else if (b>a){ print b} else {print c}}");
        Parser p = new Parser(l.Lex());
        ProgramNode program = (p.Parse());
        Interpreter inter = new Interpreter(program,"C:\\Users\\15184\\ICSI311\\Numbers.txt");

        PrintStream output = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));

        inter.InterpretProgram();

        System.setOut(output);
        Assert.assertEquals(b.toString(),
            "0\r\n" + //
            "2\r\n" + //
            "0\r\n" + //
            "2\r\n");   
    }
}