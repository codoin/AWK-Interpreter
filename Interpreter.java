import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {
    HashMap<String,InterpreterDataType> globalVariables;
    HashMap<String,FunctionDefinitionNodes> functions;
    LineManager lineManager;
    ProgramNode programNode;

    //Runs BEGIN, END, and other blocks in the program
    void InterpretProgram() throws Exception{
        //Runs each BEGIN block
        for(BlockNodes begin:programNode.GetBeginNodes()){
            InterpretBlock(begin);
        }

        lineManager.SplitAndAssign();
        //for each record, runs each other block
        for(int i = 0; i < Integer.parseInt(globalVariables.get("FNR").getName()); i++){
            for(BlockNodes other:programNode.GetOtherNodes()){
                InterpretBlock(other);
            }
            lineManager.SplitAndAssign();
        }

        //Runs each END block
        for(BlockNodes end:programNode.GetEndNodes()){
            InterpretBlock(end);
        }
    }

    //if conditon is true or there's no condition,
    //call ProcessStatement for each statement
    void InterpretBlock(BlockNodes block) throws Exception{

        if(block.GetCondition()!=null){
            //if block has condition, check if true
            if(GetIDT(block.GetCondition(), null).getName()=="1"){
                for(Node node:block.GetStatements()){
                    ProcessStatement(globalVariables, (StatementNode)node);
                }
            }
        }
        else{
            for(Node node:block.GetStatements()){
                ProcessStatement(globalVariables, (StatementNode)node);
            }
        }
    }

    //solves flow of execution of BREAK, CONTINUE, and RETURN
    ReturnType ProcessStatement(HashMap<String, InterpreterDataType> locals, StatementNode stmt) throws Exception{

        //returns right expression 
        if(stmt instanceof AssignmentNode){
            var right = GetIDT(stmt, locals);
            return new ReturnType(ReturnType.type.NORMAL,right.getName());
        }

        //returns return type of BREAK
        else if(stmt instanceof BreakNode){
            return new ReturnType(ReturnType.type.BREAK);
        }

        //returns return type of CONTINUE
        else if(stmt instanceof ContinueNode){
            return new ReturnType(ReturnType.type.CONTINUE);
        }

        //checks if array is actually an array
        //if the array has indices then delete indices in array
        //else delete entire array
        else if(stmt instanceof DeleteNode){
            var array = ((DeleteNode)stmt).GetArray();

            //if target is not an array
            if(!(array instanceof VariableReferenceNode)){
                throw new Exception("Delete Error: Not an Array");
            }
            //checks local variables
            if(locals != null && locals.containsKey(((VariableReferenceNode)array).GetVariable())){
                if(((DeleteNode)stmt).GetIndex()!=null){
                    for(Optional<Node> n:((DeleteNode)stmt).GetIndex()){ //for each index
                        if(n.isPresent()){
                            //delete each index
                            String index = GetIDT(n.get(), locals).getName();
                            var delArray = ((InterpreterArrayDataType)locals.get(((VariableReferenceNode)array).GetVariable())).getArray();
                            delArray.remove(index);
                        }
                    }
                }
                else {
                    var check = locals.get(((VariableReferenceNode)array).GetVariable());

                    //check array for type IADT
                    if(check instanceof InterpreterArrayDataType){
                        locals.remove(((VariableReferenceNode)array).GetVariable());
                    }
                    else {
                        throw new Exception("Error: Not an Array");
                    }
                }
            }
            //then checks global variables
            if(globalVariables.containsKey(((VariableReferenceNode)array).GetVariable())){
                if(((DeleteNode)stmt).GetIndex()!=null){
                    for(Optional<Node> n:((DeleteNode)stmt).GetIndex()){ //for each index
                        if(n.isPresent()){
                            //delete each index
                            String index = GetIDT(n.get(), locals).getName();
                            var delArray = ((InterpreterArrayDataType)globalVariables.get(((VariableReferenceNode)array).GetVariable())).getArray();
                            delArray.remove(index);
                        }
                    }
                }
                else {
                    var check = globalVariables.get(((VariableReferenceNode)array).GetVariable());

                    //check array for type IADT
                    if(check instanceof InterpreterArrayDataType){
                        globalVariables.remove(((VariableReferenceNode)array).GetVariable());
                    }
                    else {
                        throw new Exception("Error: Not an Array");
                    }
                }
            }
            return new ReturnType(ReturnType.type.NORMAL);
        }

        //evaluate the condition
        //then interprets list of statement/s while condition is true
        //if returns or breaks then 
        else if(stmt instanceof DoWhileNode){
            String condition = "0";
            do{
                condition = GetIDT(((DoWhileNode)stmt).GetCondition(),locals).getName(); //interprets condition
        
                var returnValue = InterpretListOfStatements(((((DoWhileNode)stmt).GetBlock())), locals); //interprets statements
                if(returnValue.GetType()==ReturnType.type.BREAK){ //checks return type
                    return returnValue;
                }
                if(returnValue.GetType()==ReturnType.type.RETURN){
                    return returnValue;
                }
            }while(condition=="1");
            return new ReturnType(ReturnType.type.NORMAL);
        }

        //process the intial, condition, increment
        //while the condition is true, interpretes list of statements 
        //checks for BREAK and RETURN type
        else if(stmt instanceof ForNode){
            if(((ForNode)stmt).GetInitial()!=null){ //process the initial
                ProcessStatement(locals,((StatementNode)((ForNode)stmt).GetInitial()));
            }

            //process the condition
            String condition = GetIDT(((StatementNode)((ForNode)stmt).GetCondition()),globalVariables).getName();

            while(condition=="1"){ 
                var returnValue = InterpretListOfStatements(((((ForNode)stmt).GetBlock())), locals);
                condition = GetIDT(((StatementNode)((ForNode)stmt).GetCondition()),globalVariables).getName();

                //checks return type
                if(returnValue.GetType()==ReturnType.type.BREAK){
                    break;
                }
                if(returnValue.GetType()==ReturnType.type.RETURN){
                    return returnValue;
                }

                GetIDT(((StatementNode)((ForNode)stmt).GetIncrement()),locals);
                //ProcessStatement(locals,((StatementNode)((ForNode)stmt).GetIncrement())); //process the increment
            }

            return new ReturnType(ReturnType.type.NORMAL);
        }

        //checks if the right expression is an array
        //assigns the left to each index of the right array
        //then 
        else if(stmt instanceof ForEachNode){
            var array = ((ForEachNode)stmt).GetArray();
            if(array instanceof OperationNode){

                //checks if right expression is an array
                if(((VariableReferenceNode)((OperationNode)array).GetRight())==null){
                    throw new Exception("Right Expression Does Not Exist");
                }
                if(!(((VariableReferenceNode)((OperationNode)array).GetRight()) instanceof VariableReferenceNode)){
                    throw new Exception("Right Expression Is Not an Array");
                }
                if(((VariableReferenceNode)((OperationNode)array).GetRight()).GetIndex()==null){
                    throw new Exception("Right Expression Does Have Indices");
                }

                String arrayName = ((VariableReferenceNode)((OperationNode)array).GetRight()).GetVariable();
                String operation = ((OperationNode)array).GetOperation();

                if(operation=="IN"){ //checks the operation for "IN" 
                    InterpreterArrayDataType IADT = null;

                    //checks if local or global variables has the array
                    //then gets the array
                    if(locals.containsKey(arrayName)){
                        if(locals.get(arrayName) instanceof InterpreterArrayDataType){
                            IADT = ((InterpreterArrayDataType)locals.get(arrayName));
                        }
                    }
                    if(globalVariables.containsKey(arrayName)){
                        if(globalVariables.get(arrayName) instanceof InterpreterArrayDataType){
                            IADT = ((InterpreterArrayDataType)globalVariables.get(arrayName));
                        }
                    }

                    //for each index of the array 
                    for(Map.Entry<String,InterpreterDataType> entry:IADT.getArray().entrySet()){
                        var left = ((VariableReferenceNode)((OperationNode)array).GetLeft()).GetVariable();
                        //assign the left as the index
                        locals.put(left, entry.getValue());

                        //then interpret the statements
                        var returnValue = InterpretListOfStatements(((ForEachNode)stmt).GetBlock(), locals);

                        //checks return type
                        if(returnValue.GetType()==ReturnType.type.BREAK){
                            break;
                        }
                        if(returnValue.GetType()==ReturnType.type.RETURN){
                            return returnValue;
                        }
                    }
                    return new ReturnType(ReturnType.type.NORMAL);
                }
                throw new Exception("Not Operation IN");
            }
        }

        //call RunFunctionCall()
        else if(stmt instanceof FunctionCallNode){
            RunFunctionCall((FunctionCallNode)stmt, locals);
            return new ReturnType(ReturnType.type.NORMAL);
        }

        //walks through linkedlist of ifNode
        //interprets the condition
        //if condition is true, interprets statements
        //else process next ifNode
        else if(stmt instanceof IfNode){
            String condition = "";
            if(((IfNode)stmt).GetCondition()!=null){
                condition = GetIDT(((IfNode)stmt).GetCondition(),null).getName();
            }

            //if the condition is true or empty
            if(condition=="1" || condition=="" || condition.compareTo("1") > 0){
                var returnValue = InterpretListOfStatements(((IfNode)stmt).GetBlock(), locals);
                if(returnValue.GetType()!=ReturnType.type.NORMAL){ //if return type is not NORMAL
                    return new ReturnType(returnValue.GetType(),returnValue.GetValue());
                }
            }
            else { //else process next if
                if(((IfNode)((IfNode)stmt).GetNext())!=null){
                    ProcessStatement(locals,((IfNode)((IfNode)stmt).GetNext()));
                }
            }

            return new ReturnType(ReturnType.type.NORMAL);
        }

        //if there is a value, process the value
        //return of type RETURN
        else if(stmt instanceof ReturnNode){
            if(((ReturnNode)stmt).GetValue()!=null){
                var val = GetIDT(((ReturnNode)stmt).GetValue(), locals);
                return new ReturnType(ReturnType.type.RETURN, val.getName());
            }
            return new ReturnType(ReturnType.type.RETURN);
        }

        //evaluate the condition
        //while condition is true, interpret list of statement/s 
        //if returns or breaks then
        else if(stmt instanceof WhileNode){
            String condition = GetIDT(((WhileNode)stmt).GetCondition(),locals).getName();

            while(condition=="1"){
                condition = GetIDT(((WhileNode)stmt).GetCondition(),locals).getName();

                var returnValue = InterpretListOfStatements(((((WhileNode)stmt).GetBlock())), locals);
                if(returnValue.GetType()==ReturnType.type.BREAK){
                    break;
                }
                if(returnValue.GetType()==ReturnType.type.RETURN){
                    return returnValue;
                }
            }
            return new ReturnType(ReturnType.type.NORMAL);
        }
        throw new Exception("Not Correct Type of Node");
    }
    
    //loops through list of statements for return type, returns type that is not normal else return normal
    ReturnType InterpretListOfStatements(LinkedList<Node> statements, HashMap<String, InterpreterDataType> locals) throws Exception{
        for(Node s:statements){
            ReturnType returnType = ProcessStatement(locals, (StatementNode)s); //Processes Function Call

            if(s instanceof AssignmentNode){
                String variable = ((VariableReferenceNode)((AssignmentNode)s).GetTarget()).GetVariable();
                locals.put(variable,new InterpreterDataType(returnType.GetValue()));
            }

            if(returnType.GetType()!=ReturnType.type.NORMAL){
                return returnType;
            }
        }
        return new ReturnType(ReturnType.type.NORMAL);
    }

    //Takes a Node in and figures out the type of node and creates an IDT
    InterpreterDataType GetIDT(Node node,HashMap<String,InterpreterDataType> localVariables) throws Exception{
        InterpreterDataType IDT = null;

        //checks target for variable or "$" and return right expression value
        if(node instanceof AssignmentNode){
            var target = ((AssignmentNode)node).GetTarget();
            if(target instanceof VariableReferenceNode){
                var expression = ((AssignmentNode)node).GetExpression();

                if (((OperationNode)expression).GetOperation() == "EQ"){
                    var right = GetIDT(((OperationNode)expression).GetRight(),localVariables);

                    if(((VariableReferenceNode)target).GetIndex().isPresent()){
                        if(localVariables.containsKey(((VariableReferenceNode)target).GetVariable())){
                            var variable = localVariables.get(((VariableReferenceNode)target).GetVariable());
                            if(!(variable instanceof InterpreterArrayDataType)){
                                throw new Exception("Assignment Not an Array");
                            }
                            HashMap<String,InterpreterDataType> array = ((InterpreterArrayDataType)variable).getArray();
                            array.put(GetIDT(((VariableReferenceNode)target).GetIndex().get(), localVariables).getName(),new InterpreterDataType(right.getName()));
                        }
                        else if(globalVariables.containsKey(((VariableReferenceNode)target).GetVariable())){
                            var variable = globalVariables.get(((VariableReferenceNode)target).GetVariable());
                            if(!(variable instanceof InterpreterArrayDataType)){
                                throw new Exception("Assignment Not an Array");
                            }
                            HashMap<String,InterpreterDataType> array = ((InterpreterArrayDataType)variable).getArray();
                            array.put(GetIDT(((VariableReferenceNode)target).GetIndex().get(), localVariables).getName(),new InterpreterDataType(right.getName()));
                        }
                        else {
                            globalVariables.put(((VariableReferenceNode)target).GetVariable(),new InterpreterArrayDataType(GetIDT(((VariableReferenceNode)target).GetIndex().get(), localVariables).getName(),new InterpreterDataType(right.getName())));
                        }
                        return right;
                    }

                    if(localVariables!=null && localVariables.containsKey(((VariableReferenceNode)target).GetVariable())){
                        localVariables.replace(((VariableReferenceNode)target).GetVariable(),new InterpreterDataType(right.getName()));
                    }
                    else{
                        globalVariables.put(((VariableReferenceNode)target).GetVariable(), new InterpreterDataType(right.getName()));
                    }
                    return right;
                }
            }
            else if(target instanceof OperationNode){ //only returns value if it is the DOLLAR operation
                if(((OperationNode) target).GetOperation().compareTo("DOLLAR")==0){
                    var expression = ((AssignmentNode)node).GetExpression();

                    if (((OperationNode)expression).GetOperation() == "EQ"){
                        var right = GetIDT(((OperationNode)expression).GetRight(), localVariables);
                        String name = "$" + ((ConstantNode)((OperationNode) target).GetLeft()).GetValue();
                        if(localVariables!=null && localVariables.containsKey(target.ToString())){
                            localVariables.replace(name,new InterpreterDataType(right.getName()));
                        }//target.ToString()
                        else{
                            globalVariables.put(name, new InterpreterDataType(right.getName()));
                        }
                        return right;
                    }
                }
            }

        }

        //returns its value
        if(node instanceof ConstantNode){
            var t = ((ConstantNode)node).GetValue();
            return new InterpreterDataType(t);
        }

        //call RunFunctionCall() returns IDT of Function Call
        if(node instanceof FunctionCallNode){
            return new InterpreterDataType(RunFunctionCall((FunctionCallNode)node, localVariables));
        }

        //throws exception
        //cannot pass pattern to a function or assignment
        if(node instanceof PatternNode){
            throw new Exception("No Pattern");
        }

        //returns true or false condition based on boolean condition
        if(node instanceof TernaryNode){
            var t = ((TernaryNode)node);
            var condition = GetIDT(t.GetExpression(),localVariables).getName(); //gets the value of the boolean condition
            if(condition=="1"){ //if true
                return GetIDT(t.GetTrueCase(),localVariables);
                
            }
            else{ //if false
                return GetIDT(t.GetFalseCase(),localVariables);
            }
        }

        //if it is a variable returns value of variable from global/local
        //if it is an array returns value of the index in array (array[index])
        if(node instanceof VariableReferenceNode){ 
            var t = ((VariableReferenceNode)node);
            var index = t.GetIndex();
            if(index.isEmpty()){ //checks if the variable is an array
                if(localVariables!=null && localVariables.containsKey(t.GetVariable())){ //checks local then global
                    return localVariables.get(t.GetVariable());
                }
                if(globalVariables.containsKey(t.GetVariable())){
                    return globalVariables.get(t.GetVariable());
                }
                return new InterpreterDataType("0"); //returns value of 0 if variable not found in global/local
            }

            //check for array in local/global variables and if the array is an IADT
            if(globalVariables.containsKey(t.GetVariable())||localVariables.containsKey(t.GetVariable())){
                if(globalVariables.get(t.GetVariable()) instanceof InterpreterArrayDataType || localVariables.get(t.GetVariable()) instanceof InterpreterArrayDataType){
                    //finds the value of the index
                    String indexValue = "";
                    if(index.get() instanceof VariableReferenceNode){
                        if(globalVariables.containsKey(((VariableReferenceNode)index.get()).GetVariable()) || localVariables.containsKey(((VariableReferenceNode)index.get()).GetVariable())){
                            indexValue = ((VariableReferenceNode)index.get()).GetVariable();
                            return localVariables.get(indexValue);
                        }
                    }
                    else {
                        indexValue = GetIDT(index.get(), localVariables).getName();
                    }

                    if(localVariables!= null && localVariables.containsKey(t.GetVariable())){ //checks for index in the array
                        var array = ((InterpreterArrayDataType)localVariables.get(t.GetVariable())).getArray();
                        return array.get(indexValue);
                    }
                    if(globalVariables.containsKey(t.GetVariable())){ //checks for index in the array
                        var array = ((InterpreterArrayDataType)globalVariables.get(t.GetVariable())).getArray();
                        return array.get(indexValue);
                    }
                    return new InterpreterDataType("0"); //returns value of 0 if index is not found in array
                }
            }
            throw new Exception("Array Error");
        }

        //Operation Nodes maybe contained in AssignmentNode
        if(node instanceof AssignmentNode){
            node = ((AssignmentNode)node).GetExpression();
        }

        //performs math-based operations, compares, boolean operations, match, not match, dollar coperations, pre/pos inc/dec, unary +/-, concatination, in
        if(node instanceof OperationNode){
            var left = GetIDT(((OperationNode)node).GetLeft(),localVariables).getName();
            var operation = ((OperationNode)node).GetOperation();

            //returns true if the string matches the regex
            if(operation=="MATCH"){
                var right = ((OperationNode)node).GetRight();
                if(right instanceof PatternNode){
                    if(left.matches(((PatternNode)right).GetPattern())){
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");
                }
                throw new Exception("Right is not a Pattern");
            }

            //returns true if the string does not match the regex
            if(operation=="NOTMATCH"){
                var right = ((OperationNode)node).GetRight();
                if(right instanceof PatternNode){
                    if(left.matches(((PatternNode)right).GetPattern())){
                        return new InterpreterDataType("0");
                    }
                    return new InterpreterDataType("1");
                }
                throw new Exception("Right is not a Pattern");
            }

            //returns true if the index is in the array
            //checks if right is an array in local/global variables then checks if left is contained in the array
            if(operation=="IN"){
                var right = ((OperationNode)node).GetRight();
                if(right instanceof VariableReferenceNode){
                    if(((VariableReferenceNode)right).GetIndex().isPresent()){ //checks if right has indices
                        if(localVariables!=null && localVariables.containsKey(((VariableReferenceNode)right).GetVariable())){ //checks if the array is in local variables
                            var array = localVariables.get(((VariableReferenceNode)right).GetVariable());
                            if(array instanceof InterpreterArrayDataType){
                                if(((InterpreterArrayDataType)array).getArray().containsKey(left)){ //if the array has the left as an index
                                    return new InterpreterDataType("1");
                                }
                                return new InterpreterDataType("0");
                            }
                            throw new Exception("IN: Right is not an array"); //throws if the right does not have an IADT
                        }
                        if(globalVariables.containsKey(((VariableReferenceNode)right).GetVariable())){ //checks if the array is in global variables
                            var array = globalVariables.get(((VariableReferenceNode)right).GetVariable());
                            if(array instanceof InterpreterArrayDataType){
                                if(((InterpreterArrayDataType)array).getArray().containsKey(left)){ //if the array has the left as an index
                                    return new InterpreterDataType("1");
                                }
                                return new InterpreterDataType("0");
                            }
                            throw new Exception("IN: Right is not an array"); //throws if the right does not have an IADT
                        }
                        return new InterpreterDataType("0");
                    }
                }
                throw new Exception("IN: Right is not an array"); //throws if right is not a variable reference
            }

            if(operation=="NOT"){
                try{ //tries to parse left as float
                    Float compare = Float.parseFloat(left);
                    if(compare==0){
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");
                }
                catch(NumberFormatException e){ //converts left into string 
                    return new InterpreterDataType("1");
                }
            }

            //returns the value of the field variable "$number"
            if(operation=="DOLLAR"){
                left = left.replaceFirst(".0",""); //removes the ".0" if the left was a math operation (1+1)
                if(globalVariables.containsKey("$"+left)){
                    return new InterpreterDataType(globalVariables.get("$"+left).getName());
                }
                return new InterpreterDataType("");
            }
            if(operation=="PREINC"){
                Float variable = Float.parseFloat(left) + 1;
                var name = ((VariableReferenceNode)((OperationNode)node).GetLeft()).GetVariable();
                localVariables.put(name,new InterpreterDataType(variable+""));
                return new InterpreterDataType(variable+"");
            }
            if(operation=="POSTINC"){
                Float variable = Float.parseFloat(left) + 1;
                var name = ((VariableReferenceNode)((OperationNode)node).GetLeft()).GetVariable();
                localVariables.put(name,new InterpreterDataType(variable+""));
                return new InterpreterDataType(variable+"");
            }
            if(operation=="PREDEC"){
                Float variable = Float.parseFloat(left) - 1;
                var name = ((VariableReferenceNode)((OperationNode)node).GetLeft()).GetVariable();
                localVariables.put(name,new InterpreterDataType(variable+""));
                return new InterpreterDataType(variable+"");
            }
            if(operation=="POSTDEC"){
                Float variable = Float.parseFloat(left) - 1;
                var name = ((VariableReferenceNode)((OperationNode)node).GetLeft()).GetVariable();
                localVariables.put(name,new InterpreterDataType(variable+""));
                return new InterpreterDataType(variable+"");
            }
            if(operation=="UNARYPOS"){
                Float variable = Float.parseFloat(left);
                var name = ((VariableReferenceNode)((OperationNode)node).GetLeft()).GetVariable();
                localVariables.put(name,new InterpreterDataType(variable+""));
                return new InterpreterDataType(variable+"");
            }
            if(operation=="UNARYNEG"){
                Float variable = Float.parseFloat(left) * -1;
                var name = ((VariableReferenceNode)((OperationNode)node).GetLeft()).GetVariable();
                localVariables.put(name,new InterpreterDataType(variable+""));
                return new InterpreterDataType(variable+"");
            }

            var right = GetIDT(((OperationNode)node).GetRight(),localVariables).getName();

            if(right!=null){ //if the operation uses two values
                float math;
                double total;
                if(operation=="ADD"){
                    math = Float.parseFloat(left);
                    math += Float.parseFloat(right);
                    return new InterpreterDataType(math+"");
                }
                if(operation=="SUBTRACT"){
                    math = Float.parseFloat(left);
                    math -= Float.parseFloat(right);
                    return new InterpreterDataType(math+"");
                }
                if(operation=="MULTIPLY"){
                    math = Float.parseFloat(left);
                    math *= Float.parseFloat(right);
                    return new InterpreterDataType(math+"");
                }
                if(operation=="DIVIDE"){
                    math = Float.parseFloat(left);
                    math /= Float.parseFloat(right);
                    return new InterpreterDataType(math+"");
                }
                if(operation=="MODULO"){
                    math = Float.parseFloat(left);
                    math %= Float.parseFloat(right);
                    return new InterpreterDataType(math+"");
                }
                if(operation=="EXPONENT"){
                    math = Float.parseFloat(left);
                    total = Math.pow(math,Float.parseFloat(right));
                    return new InterpreterDataType(total+"");
                }

                if(operation=="EQ"||operation=="NE"||operation=="LT"||operation=="LE"||operation=="GT"||operation=="GE"||operation=="AND"||operation=="OR"){
                    try{ //tries to convert left and right into float, else converts to string
                        Float compareLeft = Float.parseFloat(left);
                        Float compareRight = Float.parseFloat(right);
                        int compare;
                        if(operation=="EQ"){ //Equals comparison
                            compare = Float.compare(compareLeft,compareRight);
                            if (compare == 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="NE"){ //Not equals comparison
                            compare = Float.compare(compareLeft,compareRight);
                            if(compare!=0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="LT"){ //Less than comparison
                            compare = Float.compare(compareLeft,compareRight);
                            if(compare < 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="LE"){ //Less than equal to comparison
                            compare = Float.compare(compareLeft,compareRight);
                            if(compare <= 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="GT"){ //Greater than comparison
                            compare = Float.compare(compareLeft,compareRight);
                            if(compare > 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="GE"){ //Greater than equal to comparison
                            compare = Float.compare(compareLeft,compareRight);
                            if(compare >= 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="AND"){
                            if(compareLeft!=0 && compareRight!=0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="OR"){
                            if(compareLeft==0 || compareRight==0){
                                return new InterpreterDataType("0");
                            }
                            return new InterpreterDataType("1");
                        }
                    }
                    catch(NumberFormatException e){
                        String compareLeft = left;
                        String compareRight = right;
                        int compare;
                        if(operation=="EQ"){
                            compare = compareLeft.compareTo(compareRight);
                            if(compare==0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="NE"){
                            compare = compareLeft.compareTo(compareRight);
                            if(compare!=0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="LT"){
                            compare = compareLeft.compareTo(compareRight);
                            if(compare < 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="LE"){
                            compare = compareLeft.compareTo(compareRight);
                            if(compare <= 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="GT"){
                            compare = compareLeft.compareTo(compareRight);
                            if(compare > 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="GE"){
                            compare = compareLeft.compareTo(compareRight);
                            if(compare >= 0){
                                return new InterpreterDataType("1");
                            }
                            return new InterpreterDataType("0");
                        }
                        if(operation=="AND"){
                            return new InterpreterDataType("0");
                        }
                        if(operation=="OR"){
                            return new InterpreterDataType("0");
                        }
                    }
                }

                if(operation=="CONCATENATION"){
                    String concat = left + right;
                    return new InterpreterDataType(concat);
                }
            }
        }
        return IDT;
    }

    //map function call variables to be used properly by function definiton
    //for variadics, create an array of parameters
    //for non-variadics, fill in parameters
    //for custom functions assign each value to function defintion variable and run
    String RunFunctionCall(FunctionCallNode node,HashMap<String,InterpreterDataType> localVariables) throws Exception{
        FunctionDefinitionNodes funct = functions.get(node.GetName());

        //checks if function exists
        if(funct==null){
            throw new Exception("Function doesn't exist");
        }
        //checks if parameters matches
        if (funct.getParam() != null && funct.getParam().size() != node.GetParam().size()){
            throw new Exception("Parameter Sizes Dont Match");
        }

        int count = 0;
        LinkedList <String> list = new LinkedList<>();
        HashMap<String,InterpreterDataType> map = new HashMap<>();
        
        //for variadic builtin functions
        if(funct instanceof BuiltInFunctionDefinitionNode && ((BuiltInFunctionDefinitionNode)funct).GetVariadic()){
            while(count < node.GetParam().size()){
                list.add(GetIDT(node.GetParam().get(count++).get(), globalVariables).getName());
            }
            InterpreterArrayDataType array = new InterpreterArrayDataType(list);
            map.put("", (InterpreterDataType)array);
        }
        //for non-variadic builtin functions
        else if(funct instanceof BuiltInFunctionDefinitionNode){

            //assigns arguements to non-variadic builtin functions
            //if number of parameters dont match, throw exception
            try{
                HashMap<String,InterpreterDataType> blank = new HashMap<>();
                if(((FunctionDefinitionNodes)funct).getName()=="gsub"){
                    map.put("regex",GetIDT(node.GetParam().get(0).get(), null));
                    map.put("sub",GetIDT(node.GetParam().get(1).get(), null));

                    if (node.GetParam().size() > 2 && node.GetParam().get(2).isPresent()){
                        map.put("string",GetIDT(node.GetParam().get(2).get(), null));
                    }
                }
                if(((FunctionDefinitionNodes)funct).getName()=="index"){
                    map.put("str",GetIDT(node.GetParam().get(0).get(), null));
                    map.put("sub",GetIDT(node.GetParam().get(1).get(), null));
                }
                if(((FunctionDefinitionNodes)funct).getName()=="length"){
                    map.put("str",GetIDT(node.GetParam().get(0).get(), null));
                }
                if(((FunctionDefinitionNodes)funct).getName()=="match"){
                    map.put("str",GetIDT(node.GetParam().get(0).get(), null));

                    //gets value of pattern
                    map.put("regex",new InterpreterDataType(((PatternNode)node.GetParam().get(1).get()).GetPattern()));
                }
                if(((FunctionDefinitionNodes)funct).getName()=="split"){
                    map.put("str",GetIDT(node.GetParam().get(0).get(), null));
                    map.put("arr",new InterpreterDataType(((VariableReferenceNode)node.GetParam().get(1).get()).GetVariable()));
                    if (node.GetParam().size() > 2 && node.GetParam().get(2).isPresent()){
                        map.put("regex",GetIDT(node.GetParam().get(2).get(), blank));
                    }
                }
                if(((FunctionDefinitionNodes)funct).getName()=="sub"){
                    map.put("regex",GetIDT(node.GetParam().get(0).get(), null));
                    map.put("sub",GetIDT(node.GetParam().get(1).get(), null));
                    if (node.GetParam().size() > 2 && node.GetParam().get(2).isPresent()){
                        map.put("str",GetIDT(node.GetParam().get(2).get(), null));
                    }
                }
                if(((FunctionDefinitionNodes)funct).getName()=="substr"){
                    map.put("string",GetIDT(node.GetParam().get(0).get(), null));
                    map.put("start",GetIDT(node.GetParam().get(1).get(), null));
                    map.put("l",GetIDT(node.GetParam().get(2).get(), null));
                }
                if(((FunctionDefinitionNodes)funct).getName()=="tolower"){
                    map.put("str",GetIDT(node.GetParam().get(0).get(), null));
                }
                if(((FunctionDefinitionNodes)funct).getName()=="toupper"){
                    map.put("str",GetIDT(node.GetParam().get(0).get(), null));
                }
            }
            catch(NoSuchElementException e){
                throw new Exception("Function Parameters Dont Match");
            }
        }
        //for custom funcitions
        else {
            //assigns the variable to the value in function call
            for(String s:funct.getParam()){
                InterpreterDataType IDT = GetIDT(node.GetParam().get(count++).get(), localVariables);
                localVariables.put(s, IDT);
            }
        }

        if(funct instanceof BuiltInFunctionDefinitionNode && ((BuiltInFunctionDefinitionNode)funct).GetVariadic()){
            return ((BuiltInFunctionDefinitionNode)funct).Execute.apply(map);
        }
        else if(funct instanceof BuiltInFunctionDefinitionNode){
            return ((BuiltInFunctionDefinitionNode)funct).Execute.apply(map); //localvariables
        }
        else {
            ReturnType type = InterpretListOfStatements(funct.getStatements().GetStatements(), localVariables);
            if (type.GetType() == ReturnType.type.RETURN){
                return type.GetValue();
            }
        }

        return "";
    }

    public class LineManager{ //make accessor to test
        List<String> readFile;
        LineManager(List<String> list){
            readFile = list;
            replaceKey("FNR","0"); //resets line number every new doc
        }
        Boolean SplitAndAssign(){ //goes to next line in file and splits the fields to $1,$2,$3... ($0 is the whole line)
            if(readFile.isEmpty()){ //End of File
                return false;
            }

            String s = readFile.get(0);
            String[] temp;

            globalVariables.put("$0", new InterpreterDataType(s)); 
            temp = s.split(globalVariables.get("FS").getName());
            readFile.remove(0);

            for (int i=0; i < temp.length; i++){ //assignment of field variables
                globalVariables.put("$"+(i+1),new InterpreterDataType(temp[i]));
            }

            replaceKey("NF",temp.length+""); //set NF,NR,FNR
            int records = temp.length + Integer.parseInt(globalVariables.get("NR").getName());
            replaceKey("NR",records+"");
            int sum = Integer.parseInt(globalVariables.get("FNR").getName())+1;
            replaceKey("FNR",sum+"");
            
            if (temp.length > 0){ //if there exists elements in the line
                return true;
            }
            return false;
        }
    }

    //Takes in awk program and file path
    Interpreter(ProgramNode program,String path) throws IOException{
        globalVariables = new HashMap<>();
        functions = new HashMap<>();
        programNode = program;

        //if file path does not exists, use empty list
        if (!(path.isBlank())){
            lineManager = new LineManager(Files.readAllLines(Paths.get(path)));
        }
        else {
            lineManager = new LineManager(new LinkedList<>());
        }

        setKey("NF", "0");
        setKey("NR","0");
        setKey("FNR", "0");
        setKey("FILENAME",path); //putting global variables into hashmap
        setKey("FS"," ");
        setKey("OFMT","%.6g");
        setKey("OFS"," ");
        setKey("ORS","\n");

        //prints the IADTs in the hashmap in order of 0,1,2,3...
        BuiltInFunctionDefinitionNode print = new BuiltInFunctionDefinitionNode("print",null,null,true);
        print.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
        {
            InterpreterArrayDataType IADT = (InterpreterArrayDataType)hashmap.values().iterator().next();
            HashMap<String,InterpreterDataType> array = IADT.getArray();

            for (int i=0; array.containsKey(i+""); i++){ //if the next index is available: print
                System.out.print(array.get(i+"").getName() + "\r\n");
            }
            return "";
        };
        functions.put("print",print);

        //stores the values in the IADT in a array to format the print statement
        //for now, everything is a string
        BuiltInFunctionDefinitionNode printf = new BuiltInFunctionDefinitionNode("printf",null,null,true);
        printf.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
        {
            InterpreterArrayDataType IADT = (InterpreterArrayDataType)hashmap.values().iterator().next(); //takes in IADT
            HashMap<String,InterpreterDataType> array = IADT.getArray();
            String[] format = new String[array.size()];

            for (int i=0; array.containsKey(i+""); i++){
                format[i] = array.get(i+"").getName();
            }
            for (int i=0; i < format.length; i++){
                System.out.printf("%s",(Object)format[i]); //prints the statement in the format
            }
            return "";
        };
        functions.put("printf",printf);

        //The getline command returns 1 if it finds a record and 0 if it encounters the end of the file
        BuiltInFunctionDefinitionNode getline = new BuiltInFunctionDefinitionNode("getline",null,null,false);
        getline.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
        {
            if(lineManager.SplitAndAssign()){ //if there are still lines
                return "1";
            }
            else {
                return "0";
            }
        };
        functions.put("getline",getline);

        //The next statement forces awk to immediately stop processing the current record and go on to the next record.
        BuiltInFunctionDefinitionNode next = new BuiltInFunctionDefinitionNode("next",null,null,false);
        next.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
        {
            lineManager.SplitAndAssign(); //go to next line
            return "";
        };
        functions.put("next",next);

        //global search target for all matching substrings it can find and replace them with replacement
        BuiltInFunctionDefinitionNode gsub = new BuiltInFunctionDefinitionNode("gsub",null,null,false);
        gsub.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
        {
            String regex = hashmap.get("regex").getName();
            String sub = hashmap.get("sub").getName();
            String str;
            int count = 0;

            Pattern pattern = Pattern.compile(regex); 

            if (hashmap.size()==3){ //if gsub has 3 parameters
                str = hashmap.get("string").getName();
                Matcher match = pattern.matcher(str);

                while(match.find()){
                    count++;
                }

                str = match.replaceAll(sub);
                hashmap.replace("string", new InterpreterDataType(str)); //set the value of the string to the new one
                return count+"";
            }
            else {
                str = globalVariables.get("$0").getName();
                Matcher match = pattern.matcher(str);

                while(match.find()){
                    count++;
                }

                str = match.replaceAll(sub);
                replaceKey("$0", str); //set the value of the whole line to the new one
                return count+"";
            }
    };
    functions.put("gsub",gsub);

    //Search the string in for the first occurrence of the string find, and return the position 
    BuiltInFunctionDefinitionNode index = new BuiltInFunctionDefinitionNode("index",null,null,false);
    index.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
    {
        String str = hashmap.get("str").getName();
        String sub = hashmap.get("sub").getName();
        if (str.contains(sub)){
            return str.indexOf(sub)+1+"";
        }
        return "0";
    };
    functions.put("index",index);

    //Return the number of characters in string
    BuiltInFunctionDefinitionNode length = new BuiltInFunctionDefinitionNode("length",null,null,false);
    length.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
    {
        String str = hashmap.get("str").getName();
        return str.length()+"";
    };
    functions.put("length",length);

    //Search string for the substring matched by the regular expression regexp and return the character position
    BuiltInFunctionDefinitionNode match = new BuiltInFunctionDefinitionNode("match",null,null,false);
    match.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
    {
        String str = hashmap.get("str").getName();
        String regex = hashmap.get("regex").getName();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        if(matcher.find()){
            return matcher.start()+1+"";
        }
        return "0";
    };
    functions.put("match",match);

    //Divide string into pieces separated by regex and store the pieces in array
    BuiltInFunctionDefinitionNode split = new BuiltInFunctionDefinitionNode("split",null,null,false);
    split.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
    {
        String str = hashmap.get("str").getName();
        String name = hashmap.get("arr").getName();
        String temp[];
        LinkedList<String> newArr = new LinkedList<>();
        String size;

        if (hashmap.containsKey("regex")){
            String regex = hashmap.get("regex").getName();
            temp = str.split(regex);
            size = temp.length+"";
            for (int i = 0; i < temp.length; i++){
                newArr.add(temp[i].toString());
            }
            hashmap.replace("arr", new InterpreterArrayDataType(newArr));
        }
        else {
            temp = str.split(globalVariables.get("FS").getName());
            size = temp.length+"";
            for (int i = 0; i < temp.length; i++){
                newArr.add(temp[i].toString());
            }
            hashmap.replace("arr", new InterpreterArrayDataType(newArr));
        }
        globalVariables.put(name, new InterpreterArrayDataType(newArr));
        return size;
    };
    functions.put("split",split);

    //Return the string that printf would have printed out
    BuiltInFunctionDefinitionNode sprintf = new BuiltInFunctionDefinitionNode("sprintf",null,null,true);
        sprintf.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
        {
            InterpreterArrayDataType IADT = (InterpreterArrayDataType)hashmap.values().iterator().next();
            HashMap<String,InterpreterDataType> array = IADT.getArray();
            String[] format = new String[array.size()];
            String totalString = "";

            for (int i=0; array.containsKey(i+""); i++){
                format[i] = array.get(i+"").getName();
            }
            for (int i=0; i < format.length; i++){
                totalString += String.format("%s",format[i]);
            }
            return totalString;
        };
        functions.put("sprintf",sprintf);

        //Search target, which is treated as a string, for the substring matched by the regex. 
        //Modify the entire string by replacing the matched text with replacement. The modified string becomes the new value of target. 
        //Return the number of substitutions made 
        BuiltInFunctionDefinitionNode sub = new BuiltInFunctionDefinitionNode("sub",null,null,false);
        sub.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
        {
            String regex = hashmap.get("regex").getName();
            String subst = hashmap.get("sub").getName();
            String str;

            Pattern pattern = Pattern.compile(regex);

            if (hashmap.size()==3){
                str = hashmap.get("str").getName();
                Matcher matcher = pattern.matcher(str);
                str = matcher.replaceFirst(subst);
                hashmap.replace("str", new InterpreterDataType(str));
                return matcher.results().count()+"";
            }
            else {
                str = globalVariables.get("$0").getName();
                Matcher matcher = pattern.matcher(str);
                str = matcher.replaceFirst(subst);
                replaceKey("$0", str);
                return matcher.results().count()+"";
            }
    };
    functions.put("sub",sub);

    //Return a length-character-long substring of string
    //Strings starting at 1
    BuiltInFunctionDefinitionNode substr = new BuiltInFunctionDefinitionNode("substr",null,null,false);
    substr.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
    {
        String string = hashmap.get("string").getName();
        int start = Integer.parseInt(hashmap.get("start").getName());
        int l = Integer.parseInt(hashmap.get("l").getName());
        return string.substring(start-1, l-1);
    };
    functions.put("substr",substr);

    //Return a copy of string, but all lowercase
    BuiltInFunctionDefinitionNode tolower = new BuiltInFunctionDefinitionNode("tolower",null,null,false);
    tolower.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
    {
        String str = hashmap.get("str").getName();
        return str.toLowerCase();
    };
    functions.put("tolower",tolower);

    //Return a copy of string, but all uppercase
    BuiltInFunctionDefinitionNode toupper = new BuiltInFunctionDefinitionNode("toupper",null,null,false);
    toupper.Execute = (HashMap<String, InterpreterDataType> hashmap) -> 
    {
        String str = hashmap.get("str").getName();
        return str.toUpperCase();
    };
    functions.put("toupper",toupper);

        for (FunctionDefinitionNodes funct:program.functionNode){ //putting all non-built-in functions into hashmap
            functions.put(funct.getName(),funct);
        }
    }

    //helper functions to populate the hashmaps
    void setKey(String key, String value){
        globalVariables.put(key, new InterpreterDataType(value));
    }
    void replaceKey(String key, String value){
        globalVariables.replace(key, new InterpreterDataType(value));
    }
}