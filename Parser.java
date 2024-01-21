import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    LinkedList<Token> list;
    private TokenManager manager;
    private ProgramNode root;

    Parser (LinkedList<Token> lexerlist){ //creates the lexer to generate a linkedlist of tokens
        list = lexerlist;
        manager = new TokenManager(lexerlist);
    }

    private boolean AcceptSeperators(){ //removes unneccessary consectutive newlines and semicolons

        boolean isSeperator = false;
        while (manager.MatchAndRemove(Token.TokenTypes.SEPERATOR).isPresent() && manager.MoreTokens()){
            isSeperator = true;
        }
        return isSeperator;
    }

    ProgramNode Parse() throws Exception{ //finds suitable functions and actions and puts them into lists in program node
        root = new ProgramNode();

        while (manager.MoreTokens()){
            AcceptSeperators();
            if(ParseFunction(root) || ParseAction(root)){

            }
            else{
                throw new Exception("Can't Parse");
            }
        }
        return root;
    }
    
    private boolean ParseFunction(ProgramNode node) throws Exception{ //adds function node to program node if correctly formated
        String word;
        FunctionDefinitionNodes funct;

        LinkedList<String> param = new LinkedList<String>();
        if(!(manager.MatchAndRemove(Token.TokenTypes.FUNCTION).isPresent())){ //checks if token function is present
            return false;
        }
        if(!(MatchToken(Token.TokenTypes.WORD))){ //checks for word token
            throw new Exception("Error: Incorrect Name");
        }
        word = MatchTokenString(Token.TokenTypes.WORD);
        
        if(!(MatchToken(Token.TokenTypes.STARTPARENTHESIS))){ //checks for parentheses
            throw new Exception("Error: Incorrect Syntax");
        }
        manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS);

        AcceptSeperators();
        while (MatchToken(Token.TokenTypes.WORD)){ //add parameters
            param.add(MatchTokenString(Token.TokenTypes.WORD));
            AcceptSeperators();
            manager.MatchAndRemove(Token.TokenTypes.COMMA); //checks for comma separated parameters
            AcceptSeperators();
        }
        if(!(manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isPresent())){
            throw new Exception("Error: Incorrect Syntax");
        }
        funct = new FunctionDefinitionNodes(word,param,ParseBlock()); //creates function node
        node.functionNode.add(funct);
        return true;
    }
    
    private boolean ParseAction(ProgramNode node) throws Exception{ //checks for begin, end, or other token and adds them as a node to program node
        if(manager.MatchAndRemove(Token.TokenTypes.BEGIN).isPresent()){
            node.beginNode.add(ParseBlock());
            return true;
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.END).isPresent()){
            node.endNode.add(ParseBlock());
            return true;
        }
        else {

            Optional<Node> condition = ParseOperation(); //if the block has a condition, it will parse conidition and create a blocknode
            if (condition.isPresent()){
                node.otherNode.add(new BlockNodes(ParseBlock().GetStatements(),condition));
            }
            else {
                node.otherNode.add(new BlockNodes(ParseBlock().GetStatements()));
            }
            
            return true;
        }
    }
    
    private BlockNodes ParseBlock() throws Exception{ //parses statement(s) and adds to BlokeNode's list of statements
        LinkedList<Node> statements = new LinkedList<Node>();
        AcceptSeperators();
        if (manager.MatchAndRemove(Token.TokenTypes.STARTCURLYBRACE).isPresent()){ //checks for multiple statements
            while(manager.MatchAndRemove(Token.TokenTypes.ENDCURLYBRACE).isEmpty()){
                AcceptSeperators();
                statements.add(ParseStatement().get()); //adds all statements in the block
                AcceptSeperators();
            }
        }
        else {
            statements.add(ParseStatement().get()); //adds a single statement to block node
        }
        AcceptSeperators();
        
        return new BlockNodes(statements); //returns block node with a list of statements
    }

    Optional<Node> ParseStatement() throws Exception{ //checks for statement nodes (continue, break, if, for, delete, while, doWhile, return) then tries to parse operations, else return empty
        Optional<Node> node = ParseContinue();
        if (node.isEmpty()){ //if the parse method does not work, try next method
            node = ParseBreak();
        }
        if (node.isEmpty()){
            node = ParseIf();
        }
        if (node.isEmpty()){
            node = ParseFor();
        }
        if (node.isEmpty()){
            node = ParseDelete();
        }
        if (node.isEmpty()){
            node = ParseWhile();
        }
        if (node.isEmpty()){
            node = ParseDoWhile();
        }
        if (node.isEmpty()){
            node = ParseReturn();
        }
        if (node.isEmpty()){
            node = ParseOperation();
        }
        if (node.isEmpty()){ //if none of these methods work the block is empty 
            return Optional.empty();
        }
        AcceptSeperators();
        return node;
    }

    private Optional<Node> ParseContinue(){//parses the keyword 'continue' into continue nodes
        if(manager.MatchAndRemove(Token.TokenTypes.CONTINUE).isPresent()){ 
            return Optional.of(new ContinueNode());
        }
        return Optional.empty();
    }

    private Optional<Node> ParseBreak(){//parses the keyword 'break' into break nodes
        if(manager.MatchAndRemove(Token.TokenTypes.BREAK).isPresent()){ 
            return Optional.of(new BreakNode());
        }
        return Optional.empty();
    }

    private Optional<Node> ParseIf() throws Exception{//parses if, else if, and else
        if(manager.MatchAndRemove(Token.TokenTypes.IF).isPresent()){
            AcceptSeperators();
            if(manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isEmpty()){ //checks for parenthesis which is the beginning of the conditions 
                throw new Exception("Error: No Parenthesis for IF");
            }

            Optional<Node> condition = ParseOperation();

            if (condition.isEmpty()){
                throw new Exception("Error: No Condition in IF"); //checks for parameters in the IF
            }
            if(manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isEmpty()){ //check for closing of the conditions
                throw new Exception("Error: No Parenthesis for IF");
            }

            AcceptSeperators();
            BlockNodes statements = ParseBlock();
            AcceptSeperators();

            if (manager.MatchAndRemove(Token.TokenTypes.ELSE).isPresent()){ //if finds a else then it has to be Else or ElseIf
                AcceptSeperators();
                Optional<Node> next = ParseIf();
                if (next.isPresent()){ //if it parseIf succeeds, then it is an ElseIf statement (has conditions, statements, and the else if as a node)
                    return Optional.of(new IfNode(condition,statements,next));
                }
                else{                   //else it is an Else statement (has only statements)
                    return Optional.of(new IfNode(condition,statements,Optional.of(new IfNode(ParseBlock()))));
                }
            }
            return Optional.of(new IfNode(condition,statements)); //if the 'if' is not connected to an else then create a node with just condition and statements 
        }
        return Optional.empty();
    }

    private Optional<Node> ParseFor() throws Exception{//there are two types of For: for and forIn

        Optional<Node> condition1,condition2,condition3;
        BlockNodes block;

        if (manager.MatchAndRemove(Token.TokenTypes.FOR).isPresent()){ //checks for 'for' token
            AcceptSeperators();
            if (manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isEmpty()){ 
                    throw new Exception("For Error: No Parenthesis");
            }
            if (manager.MoreTokens() && manager.Peek(1).get().GetToken().equals(Token.TokenTypes.IN)){ //if the it is a ForEach type
                condition1 = ParseOperation();
                AcceptSeperators();
                if(manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isEmpty()){
                    throw new Exception("Error: No Parenthesis");
                }
                block = ParseBlock();
                return Optional.of(new ForEachNode(condition1.get(), block)); //creates forInNode
            }
            else {
                condition1 = ParseOperation(); //takes in the conditions in a regular for loop (for(c1;c2;c3))
                if(!AcceptSeperators()){
                    throw new Exception("For Error: No Semicolon"); //removes the semicolons
                }
                condition2 = ParseOperation();
                if(!AcceptSeperators()){
                    throw new Exception("For Error: No Semicolon");
                }
                condition3 = ParseOperation();
                AcceptSeperators();
                if(manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isEmpty()){ //makes sure for loop conditions are closed
                    throw new Exception("Error: No Parenthesis");
                }
                block = ParseBlock();
                return Optional.of(new ForNode(condition1,condition2,condition3,block));
            }
        }
        return Optional.empty();
    }

    private Optional<Node> ParseDelete() throws Exception{
        LinkedList<Optional<Node>> param = new LinkedList<>();
        Optional<Node> name;

        if(manager.MatchAndRemove(Token.TokenTypes.DELETE).isPresent()){ //checks for delete token
            if (!(MatchToken(Token.TokenTypes.WORD))){ 
                throw new Exception("Error: No Array Name"); //checks for array name
            }
            name = ParseOperation();
            if(name.isEmpty()){
                throw new Exception("Error: No Parameters");
            }
            if(manager.MatchAndRemove(Token.TokenTypes.STARTSQUAREBRACE).isEmpty()){ //checks for brackets
                return Optional.of(new DeleteNode(name));
            }
            AcceptSeperators();
            while (MatchToken(Token.TokenTypes.NUMBER)){ //add array indices
                param.add(ParseOperation());
                AcceptSeperators();
                manager.MatchAndRemove(Token.TokenTypes.COMMA); //adds comma separated parameters
                AcceptSeperators();
            }
            if(manager.MatchAndRemove(Token.TokenTypes.ENDSQUAREBRACE).isEmpty()){ //checks for completed square brackets
                throw new Exception("Error: Incorrect Syntax");
            }
            return Optional.of(new DeleteNode(name,param));
        }
        return Optional.empty();
    }

    private Optional<Node> ParseWhile() throws Exception{
        if(manager.MatchAndRemove(Token.TokenTypes.WHILE).isPresent()){
            AcceptSeperators();
            if(manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isEmpty()){ //checks for parenthesis for conditions
                throw new Exception("Error: No Parenthesis");
            }
            Optional<Node> condition = ParseOperation();                            //parses condition
            if(manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isEmpty()){
                throw new Exception("Error: No Parenthesis");
            }
            BlockNodes block = ParseBlock();
            return Optional.of(new WhileNode(condition.get(),block)); //creates whileNode
        }
        return Optional.empty();
    }

    private Optional<Node> ParseDoWhile() throws Exception{//finds do while token
        if(manager.MatchAndRemove(Token.TokenTypes.DO).isPresent()){
            BlockNodes block = ParseBlock(); //parses block
            if(manager.MatchAndRemove(Token.TokenTypes.WHILE).isEmpty()){ //checks for while
                throw new Exception("Error: No While");
            }
            if(manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isEmpty()){ //checks for parenthesis then parses condition
                throw new Exception("Error: No Parenthesis");
            }
            Optional<Node> condition = ParseOperation();
            if(manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isEmpty()){
                throw new Exception("Error: No Parenthesis");
            }
            return Optional.of(new DoWhileNode(condition.get(),block)); //create doWhileNode
        }
        return Optional.empty();
    }

    private Optional<Node> ParseReturn() throws Exception{ //parses keyword 'return', takes in at most 1 paramter and creates returnNode
        if(manager.MatchAndRemove(Token.TokenTypes.RETURN).isPresent()){
            AcceptSeperators();
            Optional<Node> param = ParseOperation(); //parses single parameter
            return Optional.of(new ReturnNode(param));
        }
        return Optional.empty();
    }

    private Optional<Node> ParseFunctionCall() throws Exception{ //finds Function calls in assignments (as in a=max(1,2,3))
        Optional<Token> name = manager.MatchAndRemove(Token.TokenTypes.GETLINE);
        LinkedList<Optional<Node>> param = new LinkedList<>();
        boolean parenthesis = false; //parenthesis are not needed. however if present, throws exception if pair parenthesis is missing

        if(name.isPresent()){ //getline
            param.add(ParseOperation());
            return Optional.of(new FunctionCallNode(name.get(),param));
        }

        name = manager.MatchAndRemove(Token.TokenTypes.PRINT);
        if(name.isPresent()){ //print
            if(manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isPresent()){
                parenthesis = true;
            }
            param.add(ParseOperation());
            //if
            while(manager.MatchAndRemove(Token.TokenTypes.COMMA).isPresent()){
                param.add(ParseOperation());
            }
            if(manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isPresent() && !parenthesis){
                throw new Exception("Error: Missing Parenthesis");
            }
            return Optional.of(new FunctionCallNode(name.get(),param));
        }

        name = manager.MatchAndRemove(Token.TokenTypes.PRINTF);
        if(name.isPresent()){ //printf
            if(manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isPresent()){
                parenthesis = true;
            }
            param.add(ParseOperation());
            while(manager.MatchAndRemove(Token.TokenTypes.COMMA).isPresent()){ //if
                param.add(ParseOperation());
            }
            if(manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isPresent() && !parenthesis){
                throw new Exception("Error: Missing Parenthesis");
            }
            return Optional.of(new FunctionCallNode(name.get(),param));
        }

        name = manager.MatchAndRemove(Token.TokenTypes.EXIT);
        if(name.isPresent()){ //exit
            param.add(ParseOperation());
            return Optional.of(new FunctionCallNode(name.get(),param));
        }

        name = manager.MatchAndRemove(Token.TokenTypes.NEXTFILE);
        if(name.isPresent()){ //nextfile
            return Optional.of(new FunctionCallNode(name.get(),param));
        }

        name = manager.MatchAndRemove(Token.TokenTypes.NEXT);
        if(name.isPresent()){ //next
            return Optional.of(new FunctionCallNode(name.get(),param));
        }


        if(manager.MoreTokens() && manager.Peek(0).get().GetToken().equals(Token.TokenTypes.WORD)){ //peeks for word then parenthesis to deferentiate from variables and grouping
            if (manager.MoreTokens() && manager.Peek(1).isPresent() && manager.Peek(1).get().GetToken().equals(Token.TokenTypes.STARTPARENTHESIS)){
                name = manager.MatchAndRemove(Token.TokenTypes.WORD);

                if(manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isEmpty()){ //checks for parentheses
                    throw new Exception("Error: Incorrect Syntax");
                }
                AcceptSeperators();
                while (manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isEmpty()){ //add parameters and comma separted parameters
                    param.add(ParseOperation());
                    AcceptSeperators();
                    manager.MatchAndRemove(Token.TokenTypes.COMMA);
                    AcceptSeperators();
                }
                return Optional.of(new FunctionCallNode(name.get(),param)); //creates functionCallNode
            }
        }
        
        return Optional.empty();
    }

    private Optional<Node> ParseOperation() throws Exception{ //parses operations of lowest precedence 
        return ParseAssignment();
    }

    private Optional<Node> ParseLValue() throws Exception{ //returns an operation node if list begins with $ and variable reference node if is a variable or an array
        Optional<Node> value = Optional.empty();
        if (manager.MatchAndRemove(Token.TokenTypes.DOLLARSIGN).isPresent()){ //checks for field reference
            value = ParseBottomLevel();
            return Optional.of(new OperationNode(value.get(), OperationNode.operations.DOLLAR));
        }
        if (MatchToken(Token.TokenTypes.WORD)){ //creates an array reference variable
            String word = MatchTokenString(Token.TokenTypes.WORD);
            if (manager.MatchAndRemove(Token.TokenTypes.STARTSQUAREBRACE).isPresent()){
                value = ParseBottomLevel();
                if (manager.MatchAndRemove(Token.TokenTypes.ENDSQUAREBRACE).isPresent()){
                    return Optional.of(new VariableReferenceNode(word, value));
                }
                else {
                    throw new Exception("Array Error");
                }
            }
            else {
                return Optional.of(new VariableReferenceNode(word));
            }
        }
        if(manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isPresent()){ //parses between parenthesis braces
            value = ParseAssignment();

            if (value.isEmpty()){
                throw new Exception("Factor Error: No Expression");
            }
            if (manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isEmpty()){
                throw new Exception("Factor Error: No End Parenthesis");
            }

            return value;
        }
        if(MatchToken(Token.TokenTypes.NUMBER)){ //makes numbers into constants
            return Optional.of(new ConstantNode(MatchTokenString(Token.TokenTypes.NUMBER)));
        }

        return Optional.empty();
    }

    private Optional<Node> ParseBottomLevel() throws Exception{ //handles constants that aren't expressions and return as constant or pattern node. Else use ParseLValue method.
        Optional<Node> operation;

        if(MatchToken(Token.TokenTypes.NUMBER)){ //makes numbers into constants
            return Optional.of(new ConstantNode(MatchTokenString(Token.TokenTypes.NUMBER)));
        }
        else if(MatchToken(Token.TokenTypes.STRINGLITERAL)){
            return Optional.of(new ConstantNode(MatchTokenString(Token.TokenTypes.STRINGLITERAL)));
        }
        else if(MatchToken(Token.TokenTypes.PATTERN)){
            return Optional.of(new PatternNode(MatchTokenString(Token.TokenTypes.PATTERN)));
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.NOT).isPresent()){ //makes rest of constants into operations
            operation = ParseLValue();
            return Optional.of(new OperationNode(operation.get(), OperationNode.operations.NOT));
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.MINUS).isPresent()){
            operation = ParseLValue();
            return Optional.of(new OperationNode(operation.get(), OperationNode.operations.UNARYNEG));
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.PLUS).isPresent()){
            operation = ParseLValue();
            return Optional.of(new OperationNode(operation.get(), OperationNode.operations.UNARYPOS));
        }

        if (manager.MatchAndRemove(Token.TokenTypes.PLUSPLUS).isPresent()){ //checks for ++ or -- to preinc/dec
            operation = ParseLValue();
            return Optional.of(new OperationNode(operation.get(), OperationNode.operations.PREINC)); 
        }
        if (manager.MatchAndRemove(Token.TokenTypes.MINUSMINUS).isPresent()){
            operation = ParseLValue();
            return Optional.of(new OperationNode(operation.get(), OperationNode.operations.PREDEC));
        }

        if (manager.MoreTokens() && manager.Peek(0).get().GetToken().equals(Token.TokenTypes.WORD) && manager.Peek(1).isPresent() && (manager.Peek(1).get().GetToken().equals(Token.TokenTypes.PLUSPLUS)||manager.Peek(1).get().GetToken().equals(Token.TokenTypes.MINUSMINUS))){ //if the value after is ++ or --, make operation node for post inc and dec
            operation = ParseLValue();
            if(manager.Peek(0).get().GetToken().equals(Token.TokenTypes.PLUSPLUS)){
                manager.MatchAndRemove(Token.TokenTypes.PLUSPLUS);
                return Optional.of(new OperationNode(operation.get(), OperationNode.operations.POSTINC));
            }
            if (manager.Peek(0).get().GetToken().equals(Token.TokenTypes.MINUSMINUS)){
                manager.MatchAndRemove(Token.TokenTypes.MINUSMINUS);
                return Optional.of(new OperationNode(operation.get(), OperationNode.operations.POSTDEC));
            }
        }

        operation = ParseFunctionCall();
        if (operation.isPresent()){
            return operation;
        } 
        return ParseLValue();
    }

    private Optional<Node> ParseTerm() throws Exception{ //parses operations with multiply or divide
        Optional<Node> left = ParseBottomLevel();
        Optional<Node> right;
        Optional<Token> operation;
        OperationNode.operations type;

        while(left.isPresent()){
            operation = manager.MatchAndRemove(Token.TokenTypes.MULTIPLY);
            type = OperationNode.operations.MULTIPLY;
            if (operation.isEmpty()){ //checks for multiplication, division, and modulo
                operation = manager.MatchAndRemove(Token.TokenTypes.DIVIDE);
                type = OperationNode.operations.DIVIDE;
            }
            if (operation.isEmpty()){
                operation = manager.MatchAndRemove(Token.TokenTypes.MODULO);
                type = OperationNode.operations.MODULO;
            }
            if (operation.isEmpty()){
                return left;
            }

            right = ParseBottomLevel();
            if (right.isEmpty()){ //check for right term
                throw new Exception("Term Error: Right Term is Empty");
            }

            if (right.isPresent()){
                left = Optional.of(new OperationNode(left.get(), right, type));
            }
            else {
                return left;
            }
        }

        return left;
    }

    private Optional<Node> ParseExpression() throws Exception{ //parses operations with plus or minus
        Optional<Node> left = ParseTerm();
        Optional<Node> right;
        Optional<Token> operation;
        OperationNode.operations type;

        while (left.isPresent()){
            operation = manager.MatchAndRemove(Token.TokenTypes.PLUS); //if the math operation is a plus or minus
            type = OperationNode.operations.ADD;
            if (operation.isEmpty()){
                operation = manager.MatchAndRemove(Token.TokenTypes.MINUS);
                type = OperationNode.operations.SUBTRACT;
            }
            if (operation.isEmpty()){
                return left;
            }

            right = ParseTerm();

            if (right.isEmpty()){ //checks for right term
                throw new Exception("Expression Error: Right Term is Empty");
            }
            
            if (right.isPresent()){
                left = Optional.of(new OperationNode(left.get(), right, type)); //create operation nodes
            }
            else {
                return left;
            }
        }
        
        return left;
    }

    private Optional<Node> ParseString() throws Exception{ //parses string concatination
        Optional<Node> left = ParseExpression();
        Optional<Node> right;
        
        while(left.isPresent() && manager.MoreTokens()){ //makes strings into constants and concatinates strings

            right = ParseExpression();

            if (right.isPresent()){//if second term exists, concatenates string
                left = Optional.of(new OperationNode(left.get(), right, OperationNode.operations.CONCATENATION));
            }
            else {
                return left;
            }
        }
        
        return left; //returns partial
    }

    private Optional<Node> PowerOperation() throws Exception{ //parses power expression (x^3)
        Optional<Node> left = ParseString();
        Optional<Node> right;
        Optional<Token> power;

        power = manager.MatchAndRemove(Token.TokenTypes.POWER); //finds power token
        
        if (power.isPresent()){ //if the exponent is present, call recursively
            right = PowerOperation();
            if (right.isPresent()){
                return Optional.of(new OperationNode(left.get(), right, OperationNode.operations.EXPONENT));
            }
            throw new Exception("Power Error: No Right Term");
        }

        return left; //return partial
    }

    private Optional<Node> ParseCompare() throws Exception{ //parses boolean expression comparisons
        Optional<Node> left = PowerOperation();
        Optional<Node> right;
        Optional<Token> compare = manager.MatchAndRemove(Token.TokenTypes.LESSTHAN); //checks for expressions like <, !=, ==, >, etc.
        OperationNode.operations type = OperationNode.operations.LT;

        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.LESSEQUAL);
            type = OperationNode.operations.LE;
        }
        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.NOTEQUAL);
            type = OperationNode.operations.NE;
        }
        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.EQUALEQUAL);
            type = OperationNode.operations.EQ;
        }
        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.GREATERTHAN);
            type = OperationNode.operations.GT;
        }
        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.GREATEREQUAL);
            type = OperationNode.operations.GE;
        }
        if (compare.isEmpty()){ //if doesnt match operation type returns partial
            return left;
        }

        right = ParseExpression();
        if (right.isEmpty()){ //checks for second term
            throw new Exception("Compare Error: Right Term is Empty");
        }

        if (right.isPresent()){
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, type)));
        }
        
        return left;
    }

    private Optional<Node> ParseRegex() throws Exception{ //parses regex matching operations
        Optional<Node> left = ParseCompare();
        Optional<Node> right;
        Optional<Token> match = manager.MatchAndRemove(Token.TokenTypes.TILDA); //checks if token is a match operation

        if (match.isEmpty()){
            match = manager.MatchAndRemove(Token.TokenTypes.NOTMATCH); //checks if token is a not match
        }
        if (match.isEmpty()){
            return left;
        }

        right = ParseExpression();

        if (right.isEmpty()){ //checks for second term
            throw new Exception("Regex Error: No Right Term");
        }
        if (match.get().GetToken().equals(Token.TokenTypes.TILDA)){ //creates an Assignment node
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.MATCH)));
        }
        if (match.get().GetToken().equals(Token.TokenTypes.NOTMATCH)) {
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.NOTMATCH)));
        }

        return left;
    }

    private Optional<Node> ParseArray() throws Exception{ //parses the 'IN' key word for arrays
        Optional<Node> left = ParseRegex();
        Optional<Node> right;
        Optional<Token> in = manager.MatchAndRemove(Token.TokenTypes.IN); //checks for key word

        while (left.isPresent()){
            right = ParseRegex();
            if (right.isPresent() && in.isPresent()){
                left = Optional.of(new OperationNode(left.get(), right,OperationNode.operations.IN));
            }
            else {
                return left;
            }
        }
        
        return left;
    }

    private Optional<Node> ParseLogic() throws Exception{ //parses AND or OR logic into operation nodes
        Optional<Node> left = ParseArray();
        Optional<Node> right;
        Optional<Token> logic = manager.MatchAndRemove(Token.TokenTypes.AND); //checks for AND or OR operations
        OperationNode.operations type = OperationNode.operations.AND;
        
        while (left.isPresent()){
            if (logic.isEmpty()){
                logic = manager.MatchAndRemove(Token.TokenTypes.OR);
                type = OperationNode.operations.OR;
            }
            if (logic.isEmpty()){
                return left;
            }

            right = ParseExpression();

            if (right.isPresent()){ //checks for second term
                left = Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, type)));
            }
            else {
                return left;
            }
        }
        
        return left;
    }

    private Optional<Node> ParseTernary() throws Exception{ //parses conditional expressions (e1 ? e2:e3)
        Optional<Node> node = ParseLogic();
        Optional<Node> tru, fal;

        Optional<Token> question = manager.MatchAndRemove(Token.TokenTypes.TERNARY);
        
        if (question.isPresent()){ //checks for ternary operation
            tru = ParseTernary();
            if (tru.isPresent()){
                if (manager.MatchAndRemove(Token.TokenTypes.COLON).isEmpty()){ //checks for colon 
                    throw new Exception("Ternary Error: No Colon");
                }
                fal = ParseTernary();
                if (fal.isPresent()){
                    return Optional.of(new TernaryNode(node.get(), tru.get(), fal.get())); //creates Ternary Node if all requirements are met
                }
                
            }
        }
        return node;
    }

    private Optional<Node> ParseAssignment() throws Exception{ //parses assignments like ^=, %=, *=, /=, +=, -=, =
        Optional<Node> left = ParseTernary();
        Optional<Node> right;
        Optional<Token> math = manager.MatchAndRemove(Token.TokenTypes.POWEREQUAL); //checks for assigment operations
        OperationNode.operations type = OperationNode.operations.EXPONENT;
        
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.MODEQUAL);
            type = OperationNode.operations.MODULO;
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.MULTEQUAL);
            type = OperationNode.operations.MULTIPLY;
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.DIVEQUAL);
            type = OperationNode.operations.DIVIDE;
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.PLUSEQUAL);
            type = OperationNode.operations.ADD;
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.MINUSEQUAL);
            type = OperationNode.operations.SUBTRACT;
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.EQUAL);
            type = OperationNode.operations.EQ;
        }
        if (math.isEmpty()){ //returns partial if none of operations matches
            return left;
        }

        right = ParseAssignment(); 

        if (right.isEmpty()){ //checks for right term
            throw new Exception("Assignment Error: Right Term is Empty");
        }

        if (right.isPresent()){
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, type))); //create an operation node of said type
        }
        else {
            return left;
        }
    }

    private String MatchTokenString(Token.TokenTypes type){ //converts matching token as the string value
        return manager.MatchAndRemove(type).get().TokenValue;
    }

    private boolean MatchToken(Token.TokenTypes type){ //sees if current token in list matches the parameter
        if (manager.MoreTokens()){
            return manager.Peek(0).get().GetToken().equals(type);
        }
        return false;
    }
}