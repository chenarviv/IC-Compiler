package SymbolTable;
import slp.*;
import Types.*;
import Semantic.SemanticError;
import Semantic.TableOfTypes;


public class BuilderSymbolTable implements PropagatingVisitor {

	String programName;
	boolean mainMethodFlag=false; //flag to mark that main method was seen

	public BuilderSymbolTable(String programName) {
		this.programName = programName;
	}

	public Object visit(Program program, SymbolTable parentTable) {
		TableOfTypes.TableOfTypesInit();//init main type table (the entire program)
		
		SymbolTable symbol_table = new SymbolTableProgram(programName); //new scope (symbol table) for program
		for (ICClass icClass : program.getClasses()){//go over each class in program
			Types classTypes = TableOfTypes.classType(icClass);	
			symbol_table.addEntity(icClass.getName(), new SymbolEntityClass(icClass.getName(), classTypes), icClass.getLine());//add new class entity to program's symbol table
			icClass.accept(this, symbol_table);//visit current class
		}
		if(!mainMethodFlag){//if after scanning all the program, we didn't see a main class
			try {
				throw new SemanticError(program.getLine(),
						"There is no 'main' method in the program");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		return symbol_table;
	}

	public Object visit(ICClass icClass, SymbolTable parentTable) {
		SymbolTableClass symbolTable = new SymbolTableClass(null,icClass.getName());//create a new class symbol table
		if(!icClass.hasSuperClass()){//no super class for class
			symbolTable.setParent(parentTable);//program is parent
		}
		else{
			//checks if the class extends itself
			if(icClass.getName().equals(icClass.getSuperClassName())){
				try {
					throw new SemanticError(icClass.getLine(),
							"In class '"
									+ icClass.getName()
									+ "' : a class cannot extend itself");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}

			}
			if (icClass.getSuperClassName().equals("Library")){
				try {
					throw new SemanticError(icClass.getLine(),
							"In class '"
									+ icClass.getName()+
							"': class 'Library' cannot be extended");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			SymbolTable classParent = ((SymbolTableProgram)parentTable).searchForClassTableByName(icClass.getSuperClassName());//get parent symbol table 
			if(classParent!=null){//class inherit from another class
				symbolTable.setParent(classParent);//parent is the symbol table of that class
			}
			else{
				try {
					throw new SemanticError(icClass.getLine(),
							"In class '"
									+ icClass.getSuperClassName()
									+ "': a class must be declared before being extended");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
		}
		symbolTable.getParent().addChildScope(icClass.getName(), symbolTable);//add class's scope to parent's scope as child
		for (Field field : icClass.getFields()){//visit class's fields
			field.accept(this, symbolTable);
		}
		for (Method method : icClass.getMethods()){//visit class's methods
			method.accept(this, symbolTable);
		}
		return symbolTable;
	}

	public Object visit(VirtualMethod method,SymbolTable parentTable) {
		if(method.getName().equals("main")){//if main is Virtual - Error.
			try {
				throw new SemanticError(method.getLine(),
						"The method 'main' must be declared static");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		parentTable.addEntity(method.getName(),new SymbolEntityMethod(method.getName(), SymbolCategories.VIRTUAL_METHOD, TableOfTypes.methodType(method)), method.getLine());//add method to parent as entity
		SymbolTableMethod symbolTable = new SymbolTableMethod(parentTable,method.getName(),SymbolTableCategories.VIRTUAL_METHOD);//create new scope for method, with class as parent
		parentTable.addChildScope(method.getName(), symbolTable);//add method's scope to parent scope as child
		method.getType().accept(this, symbolTable);//visit type
		for (Formal formal : method.getFormals()){//visit method's formals
			formal.accept(this, symbolTable);
		}

		for (Stmt statement : method.getStmts()){//visit methods stmts
			statement.accept(this, symbolTable);
		}
		return symbolTable;
	}

	public Object visit(StaticMethod method, SymbolTable parentTable) {
		TypeMethod mType;
		if(!(method.getName().compareTo("main") == 0)){// Checks if this isn't the main method
			mType = TableOfTypes.methodType(method);
		}
		else{//this is the main
			if(mainMethodFlag){//already found one main
				try {
					throw new SemanticError(method.getLine(),
							"There can't be more than one 'main' method");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			if(!method.getType().getName().equals("void")){//check if main is "void"
				try {
					throw new SemanticError(method.getLine(),
							"The 'main' method must return a value of type 'void'");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			if (method.getFormals().size() !=1){//check if main args is an array
				try {
					throw new SemanticError(method.getLine(),
							"The 'main' method must have only one argument");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			Formal mainParameter = method.getFormals().get(0);
			if(!mainParameter.getType().getName().equals("string") || mainParameter.getType().getDimension() != 1){//check that args are: String[]
				try {
					throw new SemanticError(method.getLine(),
							"The 'main' method's argument type must be string[]");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
			mType = TableOfTypes.getMainMethodType();
			mainMethodFlag = true;//flag on, main found

		}

		parentTable.addEntity(method.getName(),new SymbolEntityMethod(method.getName(), SymbolCategories.STATIC_METHOD, mType), method.getLine());//add method to parent as child
		SymbolTableMethod symbolTable = new SymbolTableMethod(parentTable,method.getName(),SymbolTableCategories.STATIC_METHOD);//open new scope for method
		parentTable.addChildScope(method.getName(), symbolTable);//add method's scope to parent scope as child
		method.getType().accept(this, symbolTable);
		for (Formal formal : method.getFormals()){//visit method's formals
			formal.accept(this, symbolTable);
		}
		for (Stmt statement : method.getStmts()){//visit method's stmts
			statement.accept(this, symbolTable);
		}
		return symbolTable;
	}

	public Object visit(StmtsBlock statementsBlock,SymbolTable parentTable) {
		SymbolTableStmtBlock symbolTable = new SymbolTableStmtBlock(parentTable,"statement block in "+ parentTable.getTableName());//new scope for the stmts block
		for (Stmt statement : statementsBlock.getStmts()){//visit block's stmts
			statement.accept(this, symbolTable);
		}
		parentTable.addChildScope(symbolTable.getTableName(), symbolTable);//add stmt scope to parent's scope
		return symbolTable;
	}
	
	public Object visit(IFStmt ifStatement,SymbolTable parentTable) {//if stmt
		ifStatement.getCondExpr().accept(this,parentTable);//visit
		ifStatement.getOpStmt().accept(this,parentTable);//visit
		if (ifStatement.hasElse()){
			ifStatement.getElseStmt().accept(this, parentTable);//visit
		}
		return null;
	}

	public Object visit(WhileStmt whileStatement,SymbolTable parentTable) {//while stmt
		whileStatement.getCondExpr().accept(this, parentTable);//visit
		whileStatement.getOpStmt().accept(this,parentTable);//visit
		return null;
	}

	public Object visit(BreakStmt breakStatement,SymbolTable parentTable) {
		return null;
	}

	public Object visit(ContinueStmt continueStatement, SymbolTable parentTable) {

		return null;
	}

	public Object visit(LocalVariable localVariable, SymbolTable parentTable) {//local variable
		if(localVariable.getType().getClass() == PrimitiveType.class){//localVariable is PrimitiveType
			parentTable.addEntity(localVariable.getName(), new SymbolEntityLocalVariable(localVariable.getName(),this.getTypesForPrimitiveType((PrimitiveType) localVariable.getType())), localVariable.getLine());//add type entity to parent
		}
		else{
			if(localVariable.getType().getDimension()==0)//localVariable is a class
				parentTable.addEntity(localVariable.getName(), new SymbolEntityLocalVariable(localVariable.getName(),new TypeClass(localVariable.getType().getName())),localVariable.getLine());//add class entity to parent
			else//localVariable is an Array
				parentTable.addEntity(localVariable.getName(), new SymbolEntityLocalVariable(localVariable.getName(), arrayTypeHelper(localVariable.getType())), localVariable.getLine());//add array entity to parent
		}
		localVariable.getType().accept(this,parentTable);//visit type
		if (localVariable.hasInitValue()) {//visit localVariable's value
			localVariable.getInitValue().accept(this,parentTable);
		}		
		if (localVariable.getInitValue() != null){
			//System.out.print(localVariable.getName()+" "+String.valueOf(localVariable.getInitValue())+"\n");
			parentTable.addAssignment(localVariable.getName());
		}
		return null;
	}

	public Object visit(Formal formal, SymbolTable parentTable) {//Formal
		if(formal.getType().getClass() == PrimitiveType.class){//formal is a primitive type
			parentTable.addEntity(formal.getName(), new SymbolEntityParameter(formal.getName(),this.getTypesForPrimitiveType((PrimitiveType) formal.getType())), formal.getLine());//add type entity to parent
		}
		else{	
			if(formal.getType().getDimension()==0)//formal is a class
				parentTable.addEntity(formal.getName(), new SymbolEntityParameter(formal.getName(),new TypeClass(formal.getType().getName())), formal.getLine());//add class entity to parent
			else//formal is an array
				parentTable.addEntity(formal.getName(), new SymbolEntityParameter(formal.getName(), arrayTypeHelper(formal.getType())), formal.getLine());//add array entity to parent
		}
		formal.getType().accept(this, parentTable);//visit formal's type
		formal.getEnclosingScope().addAssignment(formal.getName());
		return null;
	}


	public Object visit(Field field, SymbolTable parentTable) {//Field
		if(field.getType().getClass() == PrimitiveType.class){//field is a primitive type
			parentTable.addEntity(field.getName(), new SymbolEntityField(field.getName(),this.getTypesForPrimitiveType((PrimitiveType) field.getType())), field.getLine());//add type entity to parent
		}
		else{
			if(field.getType().getDimension()==0)//field is a class
				parentTable.addEntity(field.getName(), new SymbolEntityField(field.getName(),new TypeClass(field.getType().getName())), field.getLine());//add class entity to parent
			else//field is an array
				parentTable.addEntity(field.getName(), new SymbolEntityField(field.getName(), arrayTypeHelper(field.getType())), field.getLine());//add array entity to parent
		}
		field.getType().accept(this, parentTable);//visit field's type
		field.getEnclosingScope().addAssignment(field.getName());
		return null;
	}


	public Object visit(LibraryMethod method, SymbolTable parentTable) {
		if(method.getName().equals("main")){
			try {
				throw new SemanticError(method.getLine(),
						"The 'Library' must not contain a 'main' method");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		parentTable.addEntity(method.getName(),new SymbolEntityMethod(method.getName(), SymbolCategories.STATIC_METHOD, TableOfTypes.methodType(method)), method.getLine());
		SymbolTableMethod symbolTable = new SymbolTableMethod(parentTable,method.getName(),SymbolTableCategories.STATIC_METHOD);
		parentTable.addChildScope(method.getName(), symbolTable);
		method.getType().accept(this, symbolTable);
		for (Formal formal : method.getFormals()){
			formal.accept(this, symbolTable);
		}
		for (Stmt statement : method.getStmts()){
			statement.accept(this, symbolTable);
		}
		return symbolTable;
	}

	public Object visit(AssignStmt assignment, SymbolTable parentTable) {//Assignment
		assignment.getLocation().accept(this,parentTable);//visit location
		assignment.getExpr().accept(this, parentTable);//visit expr
		assignment.getEnclosingScope().addAssignment(assignment.getLocation().toString());
		return null;
	}
	
	public Object visit(LocationVar location, SymbolTable parentTable) {//VariableLocation by reference
		if(location.isPointer())
			location.getLocationExpr().accept(this, parentTable);
		return null;
	}

	public Object visit(LocationArray location, SymbolTable parentTable) {//ArrayLocation
		location.getArrayExpr().accept(this, parentTable);
		location.getIndexExpr().accept(this, parentTable);
		return null;
	}
	

	public Object visit(CallStmt callStatement, SymbolTable parentTable) {//call stmt
		callStatement.getCall().accept(this,parentTable);
		return null;
	}


	public Object visit(ReturnStmt returnStatement, SymbolTable table) {//Return stmt
		if(returnStatement.hasReturnExpr()){
			returnStatement.gerReturnExpr().accept(this, table);}//visit return expr
		return null;
	}	


	public Object visit(StaticCall call, SymbolTable parentTable) {
		for (Expr argument : call.getExprList())
			argument.accept(this,parentTable);
		return null;
	}
	
	public Object visit(VirtualCall call, SymbolTable parentTable) {
		if (call.hasLocationExpr())
			call.getLocationExpr().accept(this,parentTable);
		for (Expr argument : call.getExprList())
			argument.accept(this,parentTable);
		return null;
	}
	
	public Object visit(StaticCallStmt call, SymbolTable parentTable) {
		call.getCall().accept(this,parentTable);
		return null;
	}
	
	public Object visit(VirtualCallStmt call, SymbolTable parentTable) {
		call.getCall().accept(this,parentTable);
		return null;
	}

	public Object visit(ThisExpr thisExpression, SymbolTable parentTable) {
		return null;
	}

	public Object visit(NewClass newClass, SymbolTable parentTable) {//new Class
		return null;
	}

	public Object visit(NewArrayExpr newArray, SymbolTable parentTable) {//new ArrayExpr
		newArray.getType().accept(this,parentTable);
		for (Expr expr: newArray.getSize()){
			expr.accept(this,parentTable);
		}
		return null;
	}

	public Object visit(Length length, SymbolTable parentTable) {
		length.getArray().accept(this,parentTable);
		return null;
	}

	public Object visit(MathematicalBinaryExpr binaryOp, SymbolTable parentTable) {
		binaryOp.getFirstExpr().accept(this,parentTable);
		binaryOp.getSecondExpr().accept(this,parentTable);
		return null;
	}

	public Object visit(LogicalBinaryExpr binaryOp, SymbolTable parentTable) {
		binaryOp.getFirstExpr().accept(this,parentTable);
		binaryOp.getSecondExpr().accept(this,parentTable);
		return null;
	}

	public Object visit(MathematicalUnaryExpr unaryOp, SymbolTable parentTable) {
		unaryOp.getExpr().accept(this,parentTable);
		return null;
	}

	public Object visit(LogicalUnaryExpr unaryOp, SymbolTable parentTable) {
		unaryOp.getExpr().accept(this,parentTable);
		return null;
	}

	public Object visit(LiteralExpr literal, SymbolTable parentTable) {
		return null;
	}

	public Object visit(PrimitiveType type, SymbolTable table) {
		return null;
	}

	public Object visit(UserType type, SymbolTable table) {
		return null;
	}

	private Types getTypesForPrimitiveType(PrimitiveType type){
		if(type.getDimension()>0){
			return arrayTypeHelper(type);
		}
		else{
			if(type.getName()=="int")
				return TableOfTypes.typeInt;
			else if(type.getName()=="string")
				return TableOfTypes.typeString;
			else if(type.getName()=="boolean")
				return TableOfTypes.typeBool;
			else 
				return TableOfTypes.typeVoid;
		}
	}


	private Types arrayTypeHelper(Type array){
		String typeName = array.getName();
		if(array.getClass().equals(PrimitiveType.class)){
			if(typeName.equals("int")){
				for(int i=1;i<array.getDimension();i++){
					PrimitiveType type = new PrimitiveType(-1, DataType.INT);
					type.setDimention(i);
					TableOfTypes.arrayType(type);
				}
			}
			else if(typeName.equals("string")){
				for(int i=1;i<array.getDimension();i++){
					PrimitiveType type = new PrimitiveType(-1, DataType.STRING);
					type.setDimention(i);
					TableOfTypes.arrayType(type);
				}
			}
			else if(typeName.equals("boolean")){
				for(int i=1;i<array.getDimension();i++){
					PrimitiveType type = new PrimitiveType(-1, DataType.BOOLEAN);
					type.setDimention(i);
					TableOfTypes.arrayType(type);
				}
			}
			else{
				try {
					throw new SemanticError(array.getLine(),
							"Invalid array type '"
									+ TableOfTypes.typeToTypesConverter(array).toString()
									+ "': array cannot be of the type 'void'");
				}
				catch (SemanticError e) {
					System.out.println(e.getErrorMessage());
					System.exit(-1);
				}
			}
		}
		else if(array.getClass().equals(UserType.class)){
			for(int i=1;i<array.getDimension();i++){
				UserType type = new UserType(-1, typeName);
				type.setDimention(i);
				TableOfTypes.arrayType(type);
			}

		}
		return TableOfTypes.arrayType(array);
	}
}
