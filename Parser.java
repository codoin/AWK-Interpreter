import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    LinkedList<Token> list;
    private TokenManager manager;
    ProgramNode root;

    Parser (LinkedList<Token> lexerlist){ //creates the lexer to generate a linkedlist of tokens
        list = lexerlist;
        manager = new TokenManager(lexerlist);
    }
    boolean AcceptSeperators(){ //removes unneccessary consectutive newlines and semicolons

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
    boolean ParseFunction(ProgramNode node) throws Exception{ //adds function node to program node if correctly formated
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
            manager.MatchAndRemove(Token.TokenTypes.COMMA);
            AcceptSeperators();
        }
        if(!(MatchToken(Token.TokenTypes.ENDPARENTHESIS))){
            throw new Exception("Error: Incorrect Syntax");
        }
        funct = new FunctionDefinitionNodes(word,param); //creates function node
        //funct.statementList.addAll(ParseBlock().statement); deal with later when statement node is functional
        node.functionNode.add(funct);
        return true;
    }
    boolean ParseAction(ProgramNode node) throws Exception{ //checks for begin, end, or other token and adds them as a node to program node
        if(manager.MatchAndRemove(Token.TokenTypes.BEGIN).isPresent()){
            node.beginNode.add(ParseBlock());
            return true;
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.END).isPresent()){
            node.endNode.add(ParseBlock());
            return true;
        }
        else {
            ParseOperation();
            node.otherNode.add(ParseBlock());
            return true;
        }
    }
    BlockNodes ParseBlock(){ //for now returns empty block node
        return new BlockNodes();
    }
    public Optional<Node> ParseOperation() throws Exception{ //parses operations of lowest precedence 
        return  ParseAssignment();
    }

    Optional<Node> ParseLValue() throws Exception{ //returns an operation node if list begins with $ and variable reference node if is a variable or an array
        Optional<Node> value = Optional.empty();
        if (manager.MatchAndRemove(Token.TokenTypes.DOLLARSIGN).isPresent()){ //checks for field reference
            value = ParseBottomLevel();
            return Optional.of(new OperationNode(value.get(), OperationNode.operations.DOLLAR));
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


        if (manager.MatchAndRemove(Token.TokenTypes.PLUSPLUS).isPresent()){ //checks for ++ or -- to preinc/dec
            value = ParseBottomLevel();
            return Optional.of(new OperationNode(value.get(), OperationNode.operations.PREINC)); 
        }
        if (manager.MatchAndRemove(Token.TokenTypes.MINUSMINUS).isPresent()){
            value = ParseBottomLevel();
            return Optional.of(new OperationNode(value.get(), OperationNode.operations.PREDEC));
        }

        if (manager.MoreTokens() && manager.Peek(0).get().GetToken().equals(Token.TokenTypes.WORD)){ //if the value after is ++ or --, make operation node for post inc and dec
            if(manager.MoreTokens() && manager.Peek(1).get().GetToken().equals(Token.TokenTypes.PLUSPLUS)){
                value = ParseBottomLevel();
                manager.MatchAndRemove(Token.TokenTypes.PLUSPLUS);
                return Optional.of(new OperationNode(value.get(), OperationNode.operations.POSTINC));
            }
            if (manager.MoreTokens() && manager.Peek(1).get().GetToken().equals(Token.TokenTypes.MINUSMINUS)){
                value = ParseBottomLevel();
                manager.MatchAndRemove(Token.TokenTypes.MINUSMINUS);
                return Optional.of(new OperationNode(value.get(), OperationNode.operations.POSTDEC));
            }
        }

        return Optional.empty();
    }

    Optional<Node> ParseBottomLevel() throws Exception{ //handles constants that aren't expressions and return as constant or pattern node. Else use ParseLValue method.
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
        else if (MatchToken(Token.TokenTypes.WORD)){ //creates an array reference variable
            String word = MatchTokenString(Token.TokenTypes.WORD);
            if (manager.MatchAndRemove(Token.TokenTypes.STARTSQUAREBRACE).isPresent()){
                operation = ParseLValue();
                if (manager.MatchAndRemove(Token.TokenTypes.ENDSQUAREBRACE).isPresent()){
                    return Optional.of(new VariableReferenceNode(word, operation));
                }
                else {
                    throw new Exception("Array Error");
                }
            }
            else {
                return Optional.of(new VariableReferenceNode(word));
            }
        }

        return ParseLValue();
    }

    Optional<Node> ParseTerm() throws Exception{ //parses operations with multiply or divide
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

    Optional<Node> ParseExpression() throws Exception{ //parses operations with plus or minus
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

    Optional<Node> ParseString() throws Exception{
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

    Optional<Node> PowerOperation() throws Exception{ //parses power expression (x^3)
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

    Optional<Node> ParseCompare() throws Exception{ //parses boolean expression comparisons
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

    Optional<Node> ParseRegex() throws Exception{ //parses regex matching operations
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

    Optional<Node> ParseArray() throws Exception{ //parses the 'IN' key word for arrays
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

    Optional<Node> ParseLogic() throws Exception{ //parses AND or OR logic into operation nodes
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

    Optional<Node> ParseTernary() throws Exception{ //parses conditional expressions (e1 ? e2:e3)
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

    Optional<Node> ParseAssignment() throws Exception{ //parses assignments like ^=, %=, *=, /=, +=, -=, =
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
            math = manager.MatchAndRemove(Token.TokenTypes.APPEND);
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

    String MatchTokenString(Token.TokenTypes type){ //converts matching token as the string value
        return manager.MatchAndRemove(type).get().TokenValue;
    }

    boolean MatchToken(Token.TokenTypes type){ //sees if current token in list matches the parameter
        if (manager.MoreTokens()){
            return manager.Peek(0).get().GetToken().equals(type);
        }
        return false;
    }
}
