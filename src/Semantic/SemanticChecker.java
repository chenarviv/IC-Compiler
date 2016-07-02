package Semantic;

import java.util.LinkedList;
import java.util.List;

import slp.*;
import Types.*;
import SymbolTable.*;

public class SemanticChecker implements Visitor{	
	/**
	/* This class goes over the symbol table and checks for semantic errors. 
	 */	
	private SymbolTableProgram programTable;
	private int whileCounter=0;
	private boolean hasReturn=false;
	private boolean inStaticMethod = false;

	public SemanticChecker(SymbolTableProgram programTbl) {
		this.programTable = programTbl;
	}

	private SymbolTableClass searchForClassTableByName(String name){//search for class symbol table by name (search in the classes hierarchy)
		SymbolEntityClass classEntity = (SymbolEntityClass) programTable.searchEntityInThisTable(name, SymbolCategories.CLASS); //return class entity from the global table
		if(classEntity==null)//no such class exists
			return null;
		String class_name = name;
		LinkedList<String> hierarchy_list = new LinkedList<String>();//hierarchy of classes by name
		hierarchy_list.addFirst(name);//add class to head of list
		while(programTable.getChildScope(class_name)==null){//while we can't find the class in the program table 
			class_name = ((TypeClass) classEntity.getType()).getClassObject().getSuperClassName();//get the class we extends (parent)
			hierarchy_list.addFirst(class_name);//add parent class to hierarchy list
			classEntity = (SymbolEntityClass) programTable.searchEntityInThisTable(class_name, SymbolCategories.CLASS); //search entity of parent class in the program table
		}
		SymbolTableClass current_table = programTable.getChildScope(hierarchy_list.getFirst());//get the first class we found in the global table
		for(int i=1;i<hierarchy_list.size();i++){//add the classes in the hierarchy to the program table
			current_table = current_table.getClassTable(hierarchy_list.get(i));
		}
		return current_table;
	}

	public Object visit(Program program) {
		Types classType;
		boolean isTypeCheckDone = true;
		for (ICClass icClass : program.getClasses()){//go over all classes
			classType = (Types) icClass.accept(this);//visit each class
			if(classType == null){
				isTypeCheckDone = false;
				try {
					throw new SemanticError("Failed while executing semantic check"); //general error
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
		}
		return isTypeCheckDone;//return if the check ended successfully
	}

	public Object visit(ICClass icClass) {
		for (Field field : icClass.getFields()){//visit each field
			if(field.accept(this) == null){
				return null;
			}
		}
		for (Method method : icClass.getMethods()){//visit each method
			if(method.accept(this) == null){
				return null;
			}
		}
		return TableOfTypes.classTypeByName(icClass.getName());//search if class is in the type table
	}

	public Object visit(Field field) {
		Types type= (Types) field.getType().accept(this);
		if(type!=null){
			field.getEnclosingScope().setVariableType(field.getName(), type);//update specific type
		}

		SymbolEntity fieldSymbol = (SymbolEntity)field.getEnclosingScope().getEntityByName(field.getName(), SymbolCategories.FIELD);//check if field exists in parent scope (by name) else return parent
		Types fieldType = (Types) fieldSymbol.getType();//find type of result entity
		if(fieldType == null){//field doesn't exists
			return null;
		}
		else{
			return fieldType;//field exists
		}
	}

	public Object visit(VirtualMethod method) {
		hasReturn=false;//flag if the method has a return stmt
		method.getType().accept(this);
		for (Formal formal : method.getFormals()){//visit method's formals
			formal.accept(this);
		}
		SymbolTableClass enclosing = (SymbolTableClass) method.getEnclosingScope();//get current scop's table (the class's scope)
		SymbolEntity methodEntity =  (SymbolEntity) enclosing.searchEntityInThisTable(method.getName(), SymbolCategories.VIRTUAL_METHOD);//search if method in table
		if(!enclosing.isLegalMethod(methodEntity)){//not a legal method
			try {
				throw new SemanticError(method.getLine(),
						"Method overloading is not supported: trying to overload the method '"
						+ method.getName()
						+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		else if(!enclosing.isNotOverloadingField(methodEntity)){//declaration of method in the same name as a field
			try {
				throw new SemanticError(method.getLine(),
						"Duplicate declaration for '"
						+ method.getName()
						+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		for(Stmt stmt : method.getStmts()){//visit method's stmts
			if(stmt.accept(this) == null){
				return null;
			}
		}
		if (!hasReturn && !method.getType().getName().equals("void")){
			try {
				throw new SemanticError(method.getLine(),
						"The method '"+method.getName()+"' must return a result of type " + method.getType().getName() +" or be declared void");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		return true;
	}

	public Object visit(StaticMethod method) {
		inStaticMethod = true;
		method.getType().accept(this);
		for (Formal formal : method.getFormals())//visit method's formals
			formal.accept(this);
		SymbolTableClass enclosing = (SymbolTableClass) method.getEnclosingScope();//get parent scope
		SymbolEntity methodEntity =  (SymbolEntity) enclosing.searchEntityInThisTable(method.getName(), SymbolCategories.STATIC_METHOD);//search method in parent scope
		if(!enclosing.isLegalMethod(methodEntity)){//if not a legal method
			try {
				throw new SemanticError(method.getLine(),
						"Method overloading is not supported: trying to overload the method '"
						+ method.getName()
						+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		else if(!enclosing.isNotOverloadingField(methodEntity)){//declaration of method in the same name as a field
			try {
				throw new SemanticError(method.getLine(),
						"Duplicate declaration for '"
						+ method.getName()
						+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		for(Stmt stmt : method.getStmts()){//visit method's stmts
			if(stmt.accept(this) == null){
				inStaticMethod = false;
				return null;
			}
		}
		if (!hasReturn && !method.getType().getName().equals("void")){
			try {
				throw new SemanticError(method.getLine(),
						"The method '"+method.getName()+"' must return a result of type " + method.getType().getName() +" or be declared void");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		inStaticMethod = false;
		return true;
	}
	
	public Object visit(StaticCallStmt call) {//static call
		return call.call.accept(this);
	}

	public Object visit(VirtualCallStmt call) {//virtual call
		return call.call.accept(this);
	}

	public Object visit(LibraryMethod method) {
		//checks if the return type and parameters type are legal 
		method.getType().accept(this);
		for (Formal formal : method.getFormals())//visit method's formals
			formal.accept(this);

		for(Stmt stmt : method.getStmts()){//visit method's stmts
			if(stmt.accept(this) == null){
				return null;
			}
		}
		return true;
	}

	public Object visit(Formal formal) {
		Types type= (Types) formal.getType().accept(this);
		if(type!=null){
			formal.getEnclosingScope().setVariableType(formal.getName(), type);//set specific type
		}
		SymbolEntity formalSymbol = (SymbolEntity)formal.getEnclosingScope().getEntityByName(formal.getName(), SymbolCategories.PARAMETER);//search formal in parent table (by name) return parent if not found
		Types formalType = (Types)formalSymbol.getType();//check if formal found
		if(formalType == null){
			return null;
		}
		else{
			return formalType;
		}
	}

	public Object visit(PrimitiveType type) {
		return TableOfTypes.typeToTypesConverter(type);//convert Type to Types
	}

	public Object visit(UserType type) {
		if(searchForClassTableByName(type.getName())==null){//class not declared
			try {
				throw new SemanticError(type.getLine(),
						"Use of an undeclared class '"
						+ type.getName()
						+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
			return null;
		}
		else{
			return TableOfTypes.typeToTypesConverter(type);//user type exists, convert to Types
		}
	}
	


	public Object visit(AssignStmt assignment) {
		Types varType = (Types)assignment.getLocation().accept(this);
		Types valueType = (Types)assignment.getExpr().accept(this);
		if(valueType.isSubtypeOf(varType)){ //checks if legal assignment: assignment value's type is a sub type of the variable's type
			return assignment;
		}
		
		if (varType instanceof TypeString && valueType instanceof TypeNull){
			return assignment;
		}			
		
		else{
			try {
				throw new SemanticError(assignment.getLine(),
				"Assignment to a variable must be a sub type of its declared type");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}	
		
		}
		return null;
	}

	public Object visit(CallStmt callStatement) {
		return callStatement.getCall().accept(this);//visit call
	}

	public Object visit(ReturnStmt returnStatement) {
		hasReturn=true;
		Types returnStmtType;
		Types methodReturnType;
		if(!returnStatement.hasReturnExpr()){
			returnStmtType = (Types)TableOfTypes.typeVoid; // if it's just a 'return' it returns void 
		}
		else{
			returnStmtType = (Types)returnStatement.gerReturnExpr().accept(this);//visit the returned expression and return Types
		}
	
		SymbolTable returnStmtScope = (SymbolTable)returnStatement.getEnclosingScope(); // get the method symbol table which contains the return statement
		while(!(returnStmtScope.getTableCategory().compareTo(SymbolTableCategories.VIRTUAL_METHOD) == 0) && !(returnStmtScope.getTableCategory().compareTo(SymbolTableCategories.STATIC_METHOD) == 0)){
			returnStmtScope = returnStmtScope.getParent(); //if we are not in the method scope, for example for loop or if, we want to go up to the method's scope 
		}

		String methodName = returnStmtScope.getTableName(); //get the method's name
		SymbolTableCategories methodTableKind = returnStmtScope.getTableCategory();//get the method's scope
		SymbolCategories methodSymbolKind;
		if(methodTableKind.compareTo(SymbolTableCategories.STATIC_METHOD) == 0) { 
			methodSymbolKind = SymbolCategories.STATIC_METHOD; 
			}
		else { methodSymbolKind = SymbolCategories.VIRTUAL_METHOD; }

		SymbolTable classScope = returnStmtScope.getParent(); //get the method's parent Symbol table
	
		SymbolEntityMethod mSymbol = (SymbolEntityMethod)classScope.getEntityByName(methodName, methodSymbolKind);//get the MethodSymbol entity from the parent table

		TypeMethod mType = (TypeMethod) mSymbol.getType();//get the method (MethodType)
		methodReturnType = mType.getReturnType();//get the method's return type
		
		//checks if the expression in the return statement is from the return type of the method
		if(!returnStmtType.isSubtypeOf(methodReturnType)){
			try {
				throw new SemanticError(returnStatement.getLine(),
						"The type of the returned value in method '"
						+ mType.getMethod().getName()
						+ "' must be a sub type of the declared method's return type");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		else{ return true; }

		return null;
	}

	public Object visit(IFStmt ifStatement) {
		Types condType = (Types)ifStatement.getCondExpr().accept(this);
		if(condType == null) { return null;}
		if(ifStatement.getOpStmt().accept(this) == null){ return null; }
		if(ifStatement.hasElse()){
			if(ifStatement.getElseStmt().accept(this) == null){ return null; }
		}
		if(condType.isSubtypeOf(TableOfTypes.typeBool)){
			return true;
		}
		else{
			try {
				throw new SemanticError(ifStatement.getLine(),
				"Type mismatch : 'if' condition must be of boolean type");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}

		return true;
	}

	public Object visit(WhileStmt whileStatement) {
		whileCounter++; //get into a while loop - while counter is incremented
		Types condExprType = (Types)whileStatement.getCondExpr().accept(this);
		
		if(!condExprType.isSubtypeOf(TableOfTypes.typeBool)){
			try {
				throw new SemanticError(whileStatement.getLine(),
				"Type mismatch : 'while' condition must be of boolean type");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}

		if(whileStatement.getOpStmt().accept(this) == null){

			whileCounter--; //get out of a while loop - while counter is decremented
			return null;
		}
	
		whileCounter--; //get out of a while loop - while counter is decremented
		return true;
	}

	public Object visit(BreakStmt breakStatement) {
		if(whileCounter==0){ //means we are not inside a while loop
			try {
				throw new SemanticError(breakStatement.getLine(),
				"'break' can only be used inside 'while' loop");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		return true;
	}

	public Object visit(ContinueStmt continueStatement) {
		if(whileCounter==0){ //means we are not inside a while loop
			try {
				throw new SemanticError(continueStatement.getLine(),
				"'continue' can only be used inside 'while' loop");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		return true;
	}

	public Object visit(StmtsBlock statementsBlock) {
		for(Stmt stms: statementsBlock.getStmts()){
			if(stms.accept(this) == null){
				return null;
			}
		}
		return true;
	}
	
	public Object visit(LocalVariable localVariable) {
		Types varType = (Types)localVariable.getType().accept(this);
	
		if(varType!=null){
			localVariable.getEnclosingScope().setVariableType(localVariable.getName(), varType);//set specific type
		}
		if (varType.getName().equals("Library")){ 
			try {
				throw new SemanticError(localVariable.getLine(),
						"Cannot create an instance of class 'Library'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		if(localVariable.hasInitValue()){//init value has been set (not 'null')				
			Types initValType = (Types)localVariable.getInitValue().accept(this);			 
				if (varType instanceof TypeString && initValType instanceof TypeNull){
					return true;
				}			
				if(!initValType.isSubtypeOf(varType)){
					try {
						throw new SemanticError(localVariable.getLine(),
								"Initialization value of '"
								+ localVariable.getName()
								+"' must be a sub type of the declared variable's type");
					}
					catch (SemanticError e) {
						System.out.println(e.getErrorMessage());
						System.exit(-1);
					}
				}
			}
		return true;
	}

	public Object visit(LocationVar location) {
		Types fieldType=null;
		if(location.isPointer()){ // location hello.x (example: hello is an instance of class Hello, x is a public method or a field)
		
			Types locationType = (Types)location.getLocationExpr().accept(this);
			if(locationType == null) { return null; }

			String locationTypeName = locationType.getName(); //get type of the location expr (Hello)		
			SymbolTableClass classTable = searchForClassTableByName(locationTypeName); //get the class table, which the variable located in
			if(classTable==null){
				try {
					throw new SemanticError(location.getLine(),
							"Use of an undeclared class '"
							+ locationTypeName
							+ "'");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
				return null;
			}
			
			SymbolEntity fEntity = (SymbolEntity)classTable.getEntityByName(location.getID(), SymbolCategories.FIELD); //search field (x) in class (Hello)
			if(fEntity==null){
				try {
					throw new SemanticError(location.getLine(),
							"Use of an undeclared field '"
							+ location.getID()//###
							+ "'");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			fieldType = (Types)fEntity.getType();//get field (x) type
		}
		else{ //location is just ID (x)
			SymbolTable enclosingTable = location.getEnclosingScope();//search var (x) in the scope location is in (closest scope) 
			SymbolEntity variableSymbol = enclosingTable.searchForVariableIdentifier(location.getID(),location.getLine()); //search recursively for x declaration
			if(variableSymbol==null){
				try {
					throw new SemanticError(location.getLine(),
							"The identifier '"
							+ location.getID()
							+ "' must be declared before use");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			fieldType = (Types)variableSymbol.getType();
			
		}
		SymbolTable parent=location.getEnclosingScope();
		boolean isInit=false;

		while  (parent!=null){ 
				if (parent.isInitializied(location.toString())){
					isInit=true;
					break;
				}
			parent=parent.getParent();
		}
		if (!isInit){
			try {
				throw new SemanticError(location.getLine(),
						"Variable "+location.toString()+" may not have been initialized");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		return fieldType;
	}

	public Object visit(LocationArray location) {
		Types indexType = (Types)location.getIndexExpr().accept(this);//get i (a[i])
		Types arrGetType = (Types)location.getArrayExpr().accept(this);//get a (a[i])
		TypeArray arrayType = null;
		if(arrGetType.getClass().equals(TypeArray.class)){//check if cast to ArrayType possible
			arrayType = (TypeArray)arrGetType;//get array type
		}
		else{
			try {
				throw new SemanticError(location.getLine(),
						"The type of the expression '" +location.getArrayExpr().toString()+"' must be an array type");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		if(indexType.isSubtypeOf(TableOfTypes.typeInt)){//check that index is of type 'int'
			return TableOfTypes.typeToTypesConverter(arrayType.getElementType());
		}
		else{
			try {
				throw new SemanticError(location.getLine(),
						"Array's location index must be of type int");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		return null;
	}

	public Object visit(StaticCall call) {//Class.ID (example: Hello.getName() -> call for static method of class Hello)
		SymbolTableClass classScope = searchForClassTableByName(call.getClassId()); //search for the class table (Hello)
		if(classScope==null){//(Hello) doesn't exists
			try {
				throw new SemanticError(call.getLine(),
						"Use of an undeclared class '"
						+ call.getClassId()
						+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
			return null;
		}
		String methodName = call.getID();
		SymbolEntityMethod mSymbol = (SymbolEntityMethod)classScope.getEntityByName(methodName, SymbolCategories.STATIC_METHOD);//get entity of method (getName())
		if(mSymbol==null){
			try {
				throw new SemanticError(call.getLine(),
						"Use of an undeclared method '"
						+ call.getID()
						+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
			return null;
		}
		TypeMethod mType = (TypeMethod)mSymbol.getType();//get method type (example: public static String getName()-> String)
		List<Formal> formalList = mType.getMethod().getFormals();//get method's formals
		int formalsNum = formalList.size();
		int formalCounter = 0;

		if(call.getExprList().size() != formalsNum){// (example: public static set_X_Y(int x, int y) -> if formals != 2 -> Error)
			try {
				throw new SemanticError(call.getLine(),
						"The number of arguments in the call for method '" 
						+ call.getID()
						+ "' must be equal to the number of the method's formals");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}

		for (Expr actualParam : call.getExprList()) {// (example: Hello.set_X_Y(x,y) -> the expr: (x,y))
			Types argType = (Types)actualParam.accept(this);//convert type of each expr to Types
			Types formalType = (Types)formalList.get(formalCounter).accept(this); //convert type of the corresponding formal to Types
			formalCounter++;

			if (!argType.isSubtypeOf(formalType)){//check that the expr type is a sub type of the formal type
				try {
					throw new SemanticError(call.getLine(),
							"The type of the arguments in the call for method '" 
							+ call.getID()
							+ "' must be a sub type of the method's formals type");
					
				} 
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
		}
		return mType.getReturnType();
	}

	public Object visit(VirtualCall call) {// virtual call: hello.setSize(x) OR setSize(x);
		List<Formal> mFormals;
		String methodName = call.getID();//get ID (setSize)
		TypeMethod mType;
		if(call.hasLocationExpr()){//if expr.ID (hello)
			Types callLocationType = (Types)call.getLocationExpr().accept(this);//visit expr userTyoe (Hello)
			if(callLocationType == null){ return null; }
			//Gets class  by it's name from location
			String callLocationTypeName = callLocationType.getName();//get name (Hello)
			//get the class table, which the virtual method located in
			SymbolTableClass classTable = searchForClassTableByName(callLocationTypeName); //search for class (Hello)
			if(classTable==null){
				try {
					throw new SemanticError(call.getLine(),
							"Use of an undeclared class '"
							+ callLocationTypeName
							+ "'");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
				return true;
			} 
			//Gets method formal parameters types
			SymbolEntity mEntity = (SymbolEntity)classTable.getEntityByName(methodName, SymbolCategories.VIRTUAL_METHOD);//find method by name (setSize)
			if(mEntity==null){
				try {
					throw new SemanticError(call.getLine(),
							"Use of an undeclared method '"
							+ methodName
							+ "'");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
				return true;
			}
			mType = (TypeMethod)mEntity.getType();//get type method (void setSize(int x) -> void)
			mFormals = mType.getMethod().getFormals(); //get method's formals (int x)
		}
		else{//if call is: setSize(x) (no hello) [method only in this class or parent class]
			SymbolTable currentScope = call.getEnclosingScope();//get current scope symbol table
			while(!(currentScope.getTableCategory().compareTo(SymbolTableCategories.CLASS) == 0)){//go up until scope is a class scope
				currentScope = currentScope.getParent();
			}
			boolean virtualFound = false;
			SymbolEntity mEntity = (SymbolEntity)currentScope.getEntityByName(methodName, SymbolCategories.VIRTUAL_METHOD);//find method by name (setSize)
			if(mEntity!=null){///found method in class
				virtualFound = true;
			}
			if(inStaticMethod && virtualFound){//virtual call from inside a static method - error!
				try {
					throw new SemanticError(call.getLine(),
							"Cannot make a static reference to the non-static method '"
							+ methodName
							+ "'");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			if(!virtualFound){//check if method is static
				mEntity = (SymbolEntity)currentScope.getEntityByName(methodName, SymbolCategories.STATIC_METHOD);//static method can be called by virtual call
			}
			if(mEntity==null){
				try {
					throw new SemanticError(call.getLine(),
							"Use of an undeclared method '"
							+ methodName
							+ "'");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
				return true;
			}
			mType = (TypeMethod)mEntity.getType();
			mFormals = mType.getMethod().getFormals();//get formals
		}

		int formalsNum = mFormals.size();
		if(call.getExprList().size() != formalsNum){//check if args num in call equals num of formals
			try {
				throw new SemanticError(call.getLine(),
						"The number of arguments in the call for method '" 
								+ call.getID()
								+ "' must be equal to the number of the method's formals");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		int formalCounter = 0;
		for (Expr actualParam : call.getExprList()) {//go over exprs and corresponding formals
			Types argType = (Types)actualParam.accept(this);
			Types formalType = (Types)mFormals.get(formalCounter).accept(this);
			formalCounter++;

			if(!argType.isSubtypeOf(formalType)){//if type doesn't match - > error.
				try {
					throw new SemanticError(call.getLine(),
							"The type of the arguments in the call for method '" 
									+ call.getID()
									+ "' must be a sub type of the method's formals type");
				} 
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}

		}
		return mType.getReturnType();

	}

	public Object visit(ThisExpr thisExpression) {
		SymbolTable currScope = thisExpression.getEnclosingScope();//get scope for the 'this' expr

		while(!(currScope.getTableCategory().compareTo(SymbolTableCategories.VIRTUAL_METHOD) == 0) && !(currScope.getTableCategory().compareTo(SymbolTableCategories.STATIC_METHOD) == 0)){//move up until method scope is found
			currScope = currScope.getParent();
		}

		if(currScope.getTableCategory().compareTo(SymbolTableCategories.STATIC_METHOD) == 0){//if in static method -> error!
			try {
				throw new SemanticError(thisExpression.getLine(),
						"'this' cannot be used in a static method");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}

		while(!(currScope.getTableCategory().compareTo(SymbolTableCategories.CLASS) == 0)){ //move up until class scope found
			currScope = currScope.getParent();
		}

		String thisClassName = currScope.getTableName();//get class name from table ('this' is an instance of this class)
		TypeClass thisClassType = TableOfTypes.classTypeByName(thisClassName); //return the ClassType of the class which the this expression refers to
		return thisClassType;

	}

	public Object visit(NewClass newClass) {
		if(searchForClassTableByName(newClass.getName())==null){//search if class exists
			try {
				throw new SemanticError(newClass.getLine(),
						"Use of an undeclared class '"
						+ newClass.getName()
						+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
			return null;
		}
		return TableOfTypes.classTypeByName(newClass.getName());//return type of the class
	}

	public Object visit(NewArrayExpr newArray) {//int[] arr=new int[3] -> we go over: int[3];
		Types arraySizeType=null;
		Type newType = null;
		Type arrayElementType =  newArray.getType(); //type of elements in array
		if(arrayElementType.getClass().equals(PrimitiveType.class)){//downcast to specific type
			newType = new PrimitiveType(-1, ((PrimitiveType)arrayElementType).getType());
		}
		if(arrayElementType.getClass().equals(UserType.class)){//downcast to specific type
			newType = new UserType(-1, ((UserType)arrayElementType).getName());
		}   
		newType.setDimention(arrayElementType.getDimension());
		if(arrayElementType.getDimension()>0){ //array size can't be null
			try {
				throw new SemanticError(newArray.getLine(),
						"Array size cannot be null");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}	 
		}
		for (Expr expr: newArray.getSize()){//go over each dimension of the array: example: a[5][5]-> go over [5],[5]///#####
			arraySizeType = (Types)expr.accept(this);
			if(arraySizeType == null){ //array size can't be null
				return null;	 
			}
			if(!arraySizeType.isSubtypeOf(TableOfTypes.typeInt)){//array size must be 'int'
				try {
					throw new SemanticError(newArray.getLine(),
							"Array size must be of type int");
				} 
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}		
			}
			newType.setDimention(newType.getDimension() + 1);//update new array dimension
		}
		Types newArrayType = (Types)newType.accept(this);//get new array Types
		if(newArrayType == null){ //new array wasn't created
			return null; 
		}			
		return newArrayType;
	}

	public Object visit(Length length) { //int[] x
		Types exprType = (Types) length.getArray().accept(this); //convert to Types

		if(exprType == null) { return null; }

		if(exprType.toString().endsWith("[]")){
			return TableOfTypes.typeInt; //Length is from type int 
		}
		else{
			try {
				throw new SemanticError(length.getLine(),
				"'length' can be called only for an array");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}

		return null;
	}

	public Object visit(MathematicalBinaryExpr binaryOp) {
		Types operandType1 = (Types)binaryOp.getFirstExpr().accept(this);
		Types operandType2 = (Types)binaryOp.getSecondExpr().accept(this);
		Operator operator = (Operator)binaryOp.getOperator();

		if(operandType1 == null || operandType2 == null){
			return null;
		}

		// If the operator is '+'
		else if(operator.compareTo(Operator.PLUS) == 0){// for int+int or string+string
			if(operandType1.isSubtypeOf(TableOfTypes.typeString) && operandType2.isSubtypeOf(TableOfTypes.typeString)){
				return TableOfTypes.typeString;
			}
			else if (operandType1.isSubtypeOf(TableOfTypes.typeInt) && operandType2.isSubtypeOf(TableOfTypes.typeInt)){
				return TableOfTypes.typeInt;
			}
			else{
				try {
					throw new SemanticError(binaryOp.getLine(),
							"Math binary operation '" 
							+ binaryOp.getOperator().toString() 
							+ "' can only be applied for two operands, both of type int or both of type string");
				} 
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
		}

		// If the operator is one of {-,/,*,%}
		else{
			if(operandType1.isSubtypeOf(TableOfTypes.typeInt) && operandType2.isSubtypeOf(TableOfTypes.typeInt)){//for int+int
				return TableOfTypes.typeInt;
			}
			else{
				try {
					throw new SemanticError(binaryOp.getLine(),
							"Math binary operation '" 
							+ binaryOp.getOperator().toString() 
							+ "' can only be applied for two operands, both of type int");
				} 
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
		}

		return null;
	}

	public Object visit(LogicalBinaryExpr binaryOp) {
		Types operandType1 = (Types)binaryOp.getFirstExpr().accept(this);
		Types operandType2 = (Types)binaryOp.getSecondExpr().accept(this);
		Operator operator = (Operator) binaryOp.getOperator();

		if(operandType1 == null || operandType2 == null){
			return null;
		}
		else if(operator.compareTo(Operator.EQUAL) == 0 || operator.compareTo(Operator.NEQUAL) == 0){
			
			if(operandType1.isSubtypeOf(operandType2) || operandType2.isSubtypeOf(operandType1)){
				return TableOfTypes.typeBool;
			}
			else{
				try {
					throw new SemanticError(binaryOp.getLine(),
							"Logical binary operation '" 
							+ binaryOp.getOperator().toString() 
							+ "' can only be applied for two operands that are sub types of each other");
				} 
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
		}
		
		else if(operator.compareTo(Operator.LAND) == 0 || operator.compareTo(Operator.LOR) == 0){ //checks if && and || gets 2 operands of a boolean type
			if(operandType1.isSubtypeOf(TableOfTypes.typeBool) && operandType2.isSubtypeOf(TableOfTypes.typeBool)){
				return TableOfTypes.typeBool;
			}
			else{
				try {
					throw new SemanticError(binaryOp.getLine(),
							"Logical binary operation '" 
							+ binaryOp.getOperator().toString() 
							+ "' can only be applied for two operands, both of type boolean");
				} 
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
		}
		
		else if(operator.compareTo(Operator.LT) == 0 || operator.compareTo(Operator.LTE) == 0 || operator.compareTo(Operator.GT) == 0 || operator.compareTo(Operator.GTE) == 0){//checks if < , > , >= , <= gets 2 operands of an int type
			if(operandType1.isSubtypeOf(TableOfTypes.typeInt) && operandType2.isSubtypeOf(TableOfTypes.typeInt)){
				return TableOfTypes.typeBool;
			}
			else{
				try {
					throw new SemanticError(binaryOp.getLine(),
							"Logical binary operation '" 
							+ binaryOp.getOperator().toString()
							+ "' can only be applied for two operands, both of type int");
				} 
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}	
			}

		}

		return null;
	}

	public Object visit(MathematicalUnaryExpr unaryOp) {
	
		if(unaryOp.getExpr().getClass().equals(LiteralExpr.class)){ 	//if the operand of the math unaryOp is a literal, change it's value to minus it's value.
			LiteralExpr literal = (LiteralExpr) unaryOp.getExpr();// downcast to LiteralExpr
			if(literal.getType().getValue().equals(DataType.INT.getDefaultValue())){// if x is int than "x" -> "-"+"x"
				String value = "-".concat(String.valueOf(literal.getValue()));
				literal.setValue(value);
			}
		}
		Types operandType = (Types)unaryOp.getExpr().accept(this);
		
		if(operandType.isSubtypeOf(TableOfTypes.typeInt)){
			return TableOfTypes.typeInt;
		}
		else{
			try {
				throw new SemanticError(unaryOp.getLine(),
						"Math unary operation '" 
						+ unaryOp.getOperator().toString()
						+ "' can only be applied for one operand of type int");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		return null;
	}

	public Object visit(LogicalUnaryExpr unaryOp) {
		Types operandType = (Types)unaryOp.getExpr().accept(this);

		if(operandType.isSubtypeOf(TableOfTypes.typeBool)){
			return TableOfTypes.typeBool;
		}
		else{
			try {
				throw new SemanticError(unaryOp.getLine(),
						"Logical unary operation " 
						+ unaryOp.getOperator().toString()
						+ " can only be applied for one operand of type boolean");
			} 
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		return null;
	}

	public Object visit(LiteralExpr literal) {
		
		String literalType = literal.getType().getDescription();
		if(literalType.compareTo(LiteralTypes.INTEGER.getDescription()) == 0){  // get the value as a string
			//we check that the integer value is in the integer range			
			if (String.valueOf(literal.getValue()).length()>11){ //check that integer is less then 11 digits (else invalid int)
	
				try {
					throw new SemanticError(literal.getLine(),
							"The literal '"
							+ literal.getValue()
							+ "' of type int is out of range");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			else{
				long num = Long.valueOf((String)literal.getValue().toString());
				if((num>(Math.pow(2,31))-1)||num<(-Math.pow(2,31))){ // check if number is in the bounderies of int 
					try {
						throw new SemanticError(literal.getLine(),
								"the literal '"
								+ literal.getValue()
								+ "' of type int is out of range");
					}
					catch (SemanticError e) {
						System.out.println(e.getErrorMessage());
						System.exit(-1);
					}
				}
				else{ //number is legal int - in bounderies of int
					if(num<0){
						literal.setValue((int)(-num)); //negative
					}
					else{
						literal.setValue((int)(num));
					}
					return TableOfTypes.typeInt;
				}
			}
		}
		else if(literalType.compareTo(LiteralTypes.STRING.getDescription()) == 0){
			return TableOfTypes.typeString;
		}
		else if(literalType.compareTo(LiteralTypes.FALSE.getDescription()) == 0){
			return TableOfTypes.typeBool;
		}
		else if(literalType.compareTo(LiteralTypes.TRUE.getDescription()) == 0){
			return TableOfTypes.typeBool;
		}
		else if(literalType.compareTo(LiteralTypes.NULL.getDescription()) == 0){
			return TableOfTypes.typeNull;
		}

		return null;
	}
}
