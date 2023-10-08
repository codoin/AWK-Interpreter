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
    public Optional<Node> ParseOperation() throws Exception{ //parses operations in the list else parse bottom level
        Optional<Node> value = ParseBottomLevel(); 
        if (value.isPresent() && manager.MatchAndRemove(Token.TokenTypes.PLUSPLUS).isPresent()){ //if the value after is ++ or --, make operation node for post inc and dec
            return Optional.of(new OperationNode(value.get(), OperationNode.operations.POSTINC));
        }
        else if (value.isPresent() && manager.MatchAndRemove(Token.TokenTypes.MINUSMINUS).isPresent()){
            return Optional.of(new OperationNode(value.get(), OperationNode.operations.POSTDEC));
        }
        else if (value.isPresent() && manager.MatchAndRemove(Token.TokenTypes.POWER).isPresent()){
            return PowerOperation(value);           
        }
        else {
            return value;
        }
    }
    Optional<Node> ParseLValue() throws Exception{ //returns an operation node if list begins with $ and variable reference node if is a variable or an array
        Node value;
        Optional<Node> val;
        String word;
        if (manager.MatchAndRemove(Token.TokenTypes.DOLLARSIGN).isPresent()){
            value = ParseBottomLevel().get();
            return Optional.of(new OperationNode(value, OperationNode.operations.DOLLAR));
        }
        if (MatchToken(Token.TokenTypes.WORD)){
            word = MatchTokenString(Token.TokenTypes.WORD);
            if (manager.MatchAndRemove(Token.TokenTypes.STARTSQUAREBRACE).isPresent()){
                val = ParseOperation();
                if (manager.MatchAndRemove(Token.TokenTypes.ENDSQUAREBRACE).isPresent()){
                    return Optional.of(new VariableReferenceNode(word, val));
                }
                else {
                    throw new Exception("Array Error");
                }
            }
            else {
                return Optional.of(new VariableReferenceNode(word));
            }
        }
        return null;
    }
    Optional<Node> ParseBottomLevel() throws Exception{ //handles constants that aren't expressions and return as constant or pattern node. Else use ParseLValue method.
        Node operation;
        if(MatchToken(Token.TokenTypes.STRINGLITERAL)){ //makes strings into constants and concatinates strings
            String s;
            s = MatchTokenString(Token.TokenTypes.STRINGLITERAL);
            while(MatchToken(Token.TokenTypes.STRINGLITERAL)){
                s += MatchTokenString(Token.TokenTypes.STRINGLITERAL);
            }
            return Optional.of(new ConstantNode(s));
        }
        else if(MatchToken(Token.TokenTypes.NUMBER)){ //makes numbers into constants
            return Optional.of(new ConstantNode(MatchTokenString(Token.TokenTypes.NUMBER)));
        }
        else if(MatchToken(Token.TokenTypes.PATTERN)){
            return Optional.of(new PatternNode(MatchTokenString(Token.TokenTypes.PATTERN)));
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.STARTPARENTHESIS).isPresent()){ //parses between parenthesis braces
            operation = ParseOperation().get();
            if (manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isPresent()){
                return Optional.of(operation);
            }
            else {
                throw new Exception("Parenthesis Error");
            }
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.NOT).isPresent()){ //makes rest of constants into operations
            operation = ParseOperation().get();
            return Optional.of(new OperationNode(operation, OperationNode.operations.NOT));
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.MINUS).isPresent()){
            operation = ParseOperation().get();
            return Optional.of(new OperationNode(operation, OperationNode.operations.UNARYNEG));
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.PLUS).isPresent()){
            operation = ParseOperation().get();
            return Optional.of(new OperationNode(operation, OperationNode.operations.UNARYPOS));
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.PLUSPLUS).isPresent()){
            operation = ParseOperation().get();
            return Optional.of(new OperationNode(operation, OperationNode.operations.PREINC));
        }
        else if(manager.MatchAndRemove(Token.TokenTypes.MINUSMINUS).isPresent()){
            operation = ParseOperation().get();
            return Optional.of(new OperationNode(operation, OperationNode.operations.PREDEC));
        }
        else {
            return ParseLValue();
        }
    }

    Optional<Node> PowerOperation(Optional<Node> value) throws Exception{ //parses power expression
        Optional<Node> node = ParseBottomLevel();
        OperationNode o;
            
            if (node.isPresent() && manager.MatchAndRemove(Token.TokenTypes.POWER).isPresent()){
                o = new OperationNode(value.get(), PowerOperation(node), OperationNode.operations.EXPONENT);
            }
            if(node.isPresent() && manager.MatchAndRemove(Token.TokenTypes.POWER).isEmpty()){
                o = new OperationNode(value.get(), OperationNode.operations.EXPONENT);
                return Optional.of(o);
            }
            else {
                throw new Exception("Exponent Error"); 
            }
    }
    Optional<Node> ParseFactor() throws Exception{ //parses numbers and parenthesis
        if(MatchToken(Token.TokenTypes.NUMBER)){
            return Optional.of(new ConstantNode(MatchTokenString(Token.TokenTypes.NUMBER)));
        }
        if(MatchToken(Token.TokenTypes.WORD)){
            return Optional.of(new ConstantNode(MatchTokenString(Token.TokenTypes.WORD)));
        }
        Optional<Node> expression;
        if(MatchToken(Token.TokenTypes.STARTPARENTHESIS)){
            expression  = ParseExpression();
            if (expression.isEmpty()){
                throw new Exception("Factor Error: No Expression");
            }
            if (manager.MatchAndRemove(Token.TokenTypes.ENDPARENTHESIS).isEmpty()){
                throw new Exception("Factor Error: No End Parenthesis");
            }
            return expression;
        }
        return Optional.empty();
    }
    Optional<Node> ParseExpression() throws Exception{ //parses operations with plus or minus
        Optional<Node> left = ParseTerm();
        Optional<Node> right;
        Optional<Token> operation;

        while(true) {
            operation = manager.MatchAndRemove(Token.TokenTypes.PLUS);
            if (operation.isEmpty()){
                operation = manager.MatchAndRemove(Token.TokenTypes.MINUS);
            }
            if (operation.isEmpty()){
                return left;
            }
            right = ParseTerm();
            if (right.isEmpty()){
                throw new Exception("Expression Error: Right Term is Empty");
            }
            if (operation.get().GetToken().equals(Token.TokenTypes.PLUS)){
                return Optional.of(new OperationNode(left.get(), right, OperationNode.operations.ADD));
            }
            else if (operation.get().GetToken().equals(Token.TokenTypes.MINUS)){
                return Optional.of(new OperationNode(left.get(), right, OperationNode.operations.SUBTRACT));
            }
            else {
                return ParseAssignment();
            }
        }
    }
    Optional<Node> ParseTerm() throws Exception{ //parses operations with multiply or divide
        Optional<Node> left = ParseFactor();
        Optional<Node> right;
        Optional<Token> operation;

        while(true){
            operation = manager.MatchAndRemove(Token.TokenTypes.MULTIPLY);
            if (operation.isEmpty()){ //checks for multiplication, division, and modulo
                operation = manager.MatchAndRemove(Token.TokenTypes.DIVIDE);
            }
            if (operation.isEmpty()){
                operation = manager.MatchAndRemove(Token.TokenTypes.MODULO);
            }
            if (operation.isEmpty()){
                return left;
            }

            right = ParseFactor();
            if (right.isEmpty()){
                throw new Exception("Term Error: Right Term is Empty");
            }
            if (operation.get().GetToken().equals(Token.TokenTypes.MULTIPLY)){
                return Optional.of(new OperationNode(left.get(), right, OperationNode.operations.MULTIPLY));
            }
            else if (operation.get().GetToken().equals(Token.TokenTypes.DIVIDE)){
                return Optional.of(new OperationNode(left.get(), right, OperationNode.operations.DIVIDE));
            }
            else {
                return Optional.of(new OperationNode(left.get(), right, OperationNode.operations.MODULO));
            }
        }
    }


    Optional<Node> ParseCompare() throws Exception{ //parses expression comparisons
        Optional<Node> left = ParseExpression();
        Optional<Node> right;
        Optional<Token> compare = manager.MatchAndRemove(Token.TokenTypes.LESSTHAN);

        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.LESSEQUAL);
        }
        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.NOTEQUAL);
        }
        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.EQUALEQUAL);
        }
        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.GREATERTHAN);
        }
        if (compare.isEmpty()){
            compare = manager.MatchAndRemove(Token.TokenTypes.GREATEREQUAL);
        }
        if (compare.isEmpty()){
            return left;
        }

        right = ParseExpression();
        if (right.isEmpty()){
            throw new Exception("Compare Error: Right Term is Empty");
        }
        if (compare.get().GetToken().equals(Token.TokenTypes.LESSTHAN)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.LT)));
        }
        if (compare.get().GetToken().equals(Token.TokenTypes.LESSEQUAL)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.LE)));
        }
        if (compare.get().GetToken().equals(Token.TokenTypes.NOTEQUAL)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.NE)));
        }
        if (compare.get().GetToken().equals(Token.TokenTypes.EQUALEQUAL)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.EQ)));
        }
        if (compare.get().GetToken().equals(Token.TokenTypes.GREATERTHAN)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.GT)));
        }
        else {
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.GE)));
        }
    }

    Optional<Node> ParseRegex() throws Exception{ //parses regex matching operations
        Optional<Node> left = ParseExpression();
        Optional<Node> right;
        Optional<Token> match = manager.MatchAndRemove(Token.TokenTypes.TILDA);

        if (match.isEmpty()){
            match = manager.MatchAndRemove(Token.TokenTypes.NOTMATCH);
        }
        if (match.isEmpty()){
            return left;
        }

        right = ParseExpression();

        if (right.isEmpty()){
            throw new Exception("Regex Error: No Right Term");
        }
        if (match.get().GetToken().equals(Token.TokenTypes.TILDA)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.MATCH)));
        }
        else {
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.NOTMATCH)));
        }
    }

    Optional<Node> ParseArray(){

    }

    Optional<Node> ParseLogic() throws Exception{ //parses AND or OR
        Optional<Node> left = ParseExpression();
        Optional<Node> right;
        Optional<Token> logic = manager.MatchAndRemove(Token.TokenTypes.AND);
        
        if (logic.isEmpty()){
            logic = manager.MatchAndRemove(Token.TokenTypes.OR);
        }
        if (logic.isEmpty()){
            return left;
        }

        right = ParseExpression();

        if (logic.get().GetToken().equals(Token.TokenTypes.AND)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.AND)));
        }
        else {
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.OR)));
        }
    }

    Optional<Node> ParseTernary(){ //parses conditional expressions

    }

    Optional<Node> ParseAssignment() throws Exception{ //parses assignments
        Optional<Node> left = ParseLValue();
        Optional<Node> right;
        Optional<Token> math = manager.MatchAndRemove(Token.TokenTypes.POWEREQUAL);
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.MODEQUAL);
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.MULTEQUAL);
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.DIVEQUAL);
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.APPEND);
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.MINUSEQUAL);
        }
        if (math.isEmpty()){
            math = manager.MatchAndRemove(Token.TokenTypes.EQUAL);
        }
        if (math.isEmpty()){
            return left;
        }

        right = ParseExpression();
        if (right.isEmpty()){
            throw new Exception("Assignment Error: Right Term is Empty");
        }
        if (math.get().GetToken().equals(Token.TokenTypes.POWEREQUAL)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.EXPONENT)));
        }
        if (math.get().GetToken().equals(Token.TokenTypes.MULTEQUAL)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.MULTIPLY)));
        }
        if (math.get().GetToken().equals(Token.TokenTypes.DIVEQUAL)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.DIVIDE)));
        }
        if (math.get().GetToken().equals(Token.TokenTypes.APPEND)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.ADD)));
        }
        if (math.get().GetToken().equals(Token.TokenTypes.MINUSEQUAL)){
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.SUBTRACT)));
        }
        else {
                return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.operations.EQ)));
        }
    }

    String MatchTokenString(Token.TokenTypes type){ //converts matching token as the string value
        return manager.MatchAndRemove(type).get().TokenValue;
    }

    boolean MatchToken(Token.TokenTypes type){ //sees if current token in list matches the parameter
        return manager.Peek(0).get().GetToken().equals(type);
    }
}
