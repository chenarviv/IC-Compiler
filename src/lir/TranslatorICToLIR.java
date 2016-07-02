package lir;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.lang.StringBuilder;

import slp.*;
import SymbolTable.*;
import Types.*;
import Semantic.*;

import java.util.ArrayList;

public class TranslatorICToLIR implements PropagatingVisitorLIR{

	private SymbolTableProgram programSymTable; //Symbol table of the whole program
	private Map<String, String> mapStringLiterals;	//Map between literals and their names labels
	private Map<String,ClassOffsetsTable> mapClassesOffsetsTables; // Map between class names and their layout tables
	private int strLiteralsCount;
	private StringBuilder transBuilder=new StringBuilder(); 
	private int labelNumber_false;
	private int labelNumber_true;
	private int labelNumber_end;
	private int whileNumber = -1;
	private boolean isReturnStmtExist = false;
	private String endLine = "\n";

	public TranslatorICToLIR(SymbolTableProgram program){
		this.programSymTable = program;
		this.mapStringLiterals = new HashMap<String, String>();
		this.mapClassesOffsetsTables = new HashMap<String, ClassOffsetsTable>();
		//init counters
		this.strLiteralsCount = 1;
		this.labelNumber_true = 1;
		this.labelNumber_false = 1;
		this.labelNumber_end = 1;
	}
 
	private boolean isTemporary(String returnStr){ //returns if the variable is in a temporary
		if(returnStr.startsWith("R")){
			return true;
		}
		else{
			return false;
		}
	}

	private int getTemporaryNumber(String tempName){
		return Integer.parseInt(tempName.substring(1,tempName.length())); //for example: temporary is R1, return 1 
	}

	public Object visit(Program program, int tempNum){
		for(ICClass icClass : program.getClasses()){

			if(!icClass.getName().equals("Library")){
		
				if(icClass.hasSuperClass()){ // If the current class has a super class
					String superClassName = icClass.getSuperClassName();
					ClassOffsetsTable superClassLayout = this.mapClassesOffsetsTables.get(superClassName);
					ClassOffsetsTable newClassLayout = new ClassOffsetsTable(icClass,superClassLayout);
					this.mapClassesOffsetsTables.put(icClass.getName(), newClassLayout);
					transBuilder.append(newClassLayout.translateClassOffsetsTable() + endLine);
				}	
				else{ // If the current class doesn't have a super class
					ClassOffsetsTable newClassLayout = new ClassOffsetsTable(icClass);
					this.mapClassesOffsetsTables.put(icClass.getName(), newClassLayout);
					transBuilder.append(newClassLayout.translateClassOffsetsTable() + endLine);
				}
			}
		}
		transBuilder.append(endLine);

		for(ICClass icClass : program.getClasses()) {//go over classes of the program
			if(icClass.getName() != "Library"){
				icClass.accept(this, tempNum);
			}
		}

		StringBuilder strLit = new StringBuilder();
		for (int i = 1; i <= this.mapStringLiterals.size(); i++) {
			String strName = "str" + i;  
			strLit.append(strName + ": " + this.mapStringLiterals.get(strName) +  endLine);
		}
		strLit.append(endLine + transBuilder.toString());
		transBuilder = strLit;

		return this.transBuilder.toString();
	}

	public Object visit(ICClass icClass, int tempNum) {
		for (Method method : icClass.getMethods()) {
			method.accept(this, tempNum);
		}
		return "succeed";
	}

	public Object visit(Field field, int tempNum) {
		return "succeed";
	}
	

	public Object visit(Formal formal, int tempNum){
		return "succeed";
	}

	public Object visit(VirtualMethod method, int tempNum) {
		methodTranslator(method,tempNum);
		transBuilder.append(endLine);
	/*	SymbolTableMethod methodSymTable;
		for(Formal formal: method.formals){
			SymbolTable symTable = formal.getEnclosingScope();
			methodSymTable = (SymbolTableMethod) symTable;
			methodSymTable.getInputParametersLirName(formal.getName());
		}*/
		return "succeed";
	}

	public Object visit(StaticMethod method, int tempNum) {
		String methodName = method.getName();
	/*	SymbolTableMethod methodSymTable;
		for(Formal formal: method.formals){
			SymbolTable symTable = formal.getEnclosingScope();
			methodSymTable = (SymbolTableMethod) symTable;
			methodSymTable.getInputParametersLirName(formal.getName());
		}*/
		if(methodName.compareTo("main") != 0){//If this isn't main method
			methodTranslator(method, tempNum);
		}else{
			transBuilder.append("_ic_main:" + endLine);
			methodTranslator(method, tempNum);
			transBuilder.append("Library __exit(0),R" +  tempNum + endLine);
		}
		transBuilder.append(endLine);

		return "succeed";
	}

	public Object visit(LibraryMethod method, int tempNum){
		return "succeed";
	}

	public Object visit(PrimitiveType type, int tempNum){
		return "succeed";
	}

	public Object visit(UserType type, int tempNum) {
		return "succeed";
	}

	public LinkedList<String> TempHierarchy=new LinkedList<String>();//temp's hierarchy 

	public Object visit(AssignStmt assignment, int tempNum) {
		TempHierarchy.clear();
		boolean isField=false;
		String className = "";
		String locationName ="";
		String translateClass = "";
		String translateAssignVal="";
		SymbolTable currScope=null;
		if(assignment.getLocation() instanceof LocationArray){//for example: a[5]=x
			LocationArray arrLoc = (LocationArray) assignment.getLocation();
			String trArray = (String) arrLoc.getArrayExpr().accept(this, tempNum); //for exmaple: a[5] --> a
			String trIndex =(String) arrLoc.getIndexExpr().accept(this, tempNum); //for exmaple: a[5] --> 5 or R1 if R1<--5
			if(this.isTemporary(trIndex)){ // Check if index is saved in register
				tempNum=getTemporaryNumber(trIndex)+1;
				TempHierarchy.clear();
			}
			if(!this.isTemporary(trArray)){
				transBuilder.append("Move " + trArray + ",R" + tempNum + endLine); //Move a,R3 --> move a to R3
				TempHierarchy.add(trArray);
				TempHierarchy.add("R" + tempNum);
				trArray="R"+tempNum;	
			}
			else{
				tempNum=getTemporaryNumber(trArray);
			}
			TempHierarchy.add("R" + tempNum + "[" + trIndex + "]");
			translateAssignVal = (String) assignment.getExpr().accept(this, tempNum+1);//for example: a[5]=x --> x 
			TempHierarchy.add(translateAssignVal);
			String src=null;
			String dst=null;
			for (int i=TempHierarchy.size()-1; i>0; i-=2){//Reverse location
				src=TempHierarchy.get(i);
				dst=TempHierarchy.get(i-1);
				if (src.contains("[")|| dst.contains("[")){
					transBuilder.append("MoveArray " + src + "," + dst + endLine);
				}
				else if (src.contains(".")|| dst.contains(".")){
						transBuilder.append("MoveField " + src + "," + dst + endLine);
				}
				else{
					transBuilder.append("Move " + src + "," + dst + endLine);
				}
			}
		}else if(assignment.getLocation() instanceof LocationVar){ //for example: x=5 or this.x=5
			LocationVar varLoc = (LocationVar)assignment.getLocation(); //get x or this.x
			locationName = varLoc.getID();
			translateAssignVal = (String) assignment.getExpr().accept(this, tempNum); //for example: 5 or R3 if R3<--5 
			
			if(this.isTemporary(translateAssignVal)){ //if R3
				tempNum = this.getTemporaryNumber(translateAssignVal)+1; //returns 4
				TempHierarchy.clear();
			}

			if(varLoc.isPointer()){//if this.x
				isField = true;
				Types classType = (Types)varLoc.getLocationExpr().accept(new SemanticChecker(this.programSymTable));//get the class of the field (asuume class is A)
				className = classType.getName();
				translateClass = (String) varLoc.getLocationExpr().accept(this, tempNum);//this.x --> this
				if(!this.isTemporary(translateClass)){//if 'this' not in reg
					transBuilder.append("Move " + translateClass + ",R" + tempNum + endLine);//assume A in R1 --> Move this,R4
					transBuilder.append("#__checkNullRef("+translateClass+")"+endLine);  // RunTime Check
					TempHierarchy.add(translateClass);
					TempHierarchy.add("R" + tempNum);
					translateClass = "R" + tempNum;//R4
				}
			}
			else{//if x
				currScope = varLoc.getEnclosingScope();
				//changes under this comment!

					while(!(currScope.getTableCategory().compareTo(SymbolTableCategories.CLASS) == 0)){
						currScope = currScope.getParent();
					}

					className = currScope.getTableName();//get class A

					if(this.mapClassesOffsetsTables.get(className).getMapFieldNameToFIeld().containsKey(locationName)){//if x is a field in class A 
						isField = true;
						transBuilder.append("Move this,R" + tempNum + endLine);//Move this,R4
						TempHierarchy.add("this");
						TempHierarchy.add("R" + tempNum);
						translateClass = "R" + tempNum; //R4
					}
				}
			
			if(isField){//x is a field in A
				ClassOffsetsTable classOffsetTbl = this.mapClassesOffsetsTables.get(className);
				int fieldOffset = classOffsetTbl.getFieldOffsetFromFieldName(locationName);//get x offset in A
				transBuilder.append("MoveField " + translateAssignVal + "," + translateClass + "." + fieldOffset + endLine); //Move 5,R4.3 (assume in A: a,b,x fields)
				String src=null;
				String dst=null;
				for (int i=TempHierarchy.size()-1; i>0; i-=2){//Reverse location
					src=TempHierarchy.get(i);
					dst=TempHierarchy.get(i-1);
					if (src.contains("[")|| dst.contains("[")){
						transBuilder.append("MoveArray " + src + "," + dst + endLine);
					}
					else if (src.contains(".")|| dst.contains(".")){
							transBuilder.append("MoveField " + src + "," + dst + endLine);
					}
					else{
						transBuilder.append("Move " + src + "," + dst + endLine);
					}
				}
					
			}else{ //x local var
				String lirName= null;
				currScope = varLoc.getEnclosingScope();
				
				while (!(currScope.getTableCategory().compareTo(SymbolTableCategories.VIRTUAL_METHOD) == 0) && (!(currScope.getTableCategory().compareTo(SymbolTableCategories.STATIC_METHOD) == 0))){
					currScope = currScope.getParent();
				}
				
				if (currScope instanceof SymbolTableMethod){
					SymbolTableMethod currScopeMethod = (SymbolTableMethod) currScope; 
					if (currScopeMethod.isParameter(locationName)){
					  lirName = currScopeMethod.getInputParametersLirName(locationName);
						
					}else{
						 lirName = currScope.getLocalVariableLirName(locationName);					
					}
				}
				transBuilder.append("Move " + translateAssignVal + ",R" + tempNum + endLine);
				transBuilder.append("Move R" + tempNum + "," + lirName + endLine);
			}
		}
		return "succeed";
	}

	public Object visit(CallStmt callStatement, int tempNum) {
		callStatement.getCall().accept(this, tempNum);
		return "succeed";
	}

	public Object visit(ReturnStmt returnStatement, int tempNum) {
		this.isReturnStmtExist = true;
		String translateReturnVal = (String) returnStatement.gerReturnExpr().accept(this, tempNum);
		transBuilder.append("Return " + translateReturnVal + endLine);
		return translateReturnVal;
	}

	public Object visit(IFStmt ifStatement, int tempNum) {
		transBuilder.append("#start if" + endLine);
		if(ifStatement.hasElse()){
			String trCond = (String) ifStatement.getCondExpr().accept(this, tempNum);
			if (!isTemporary(trCond)){
				transBuilder.append("Move " + trCond + ",R" + tempNum + endLine); 
				trCond="R"+tempNum;				
			}
			transBuilder.append("Compare 1," + trCond + endLine);
			transBuilder.append("JumpFalse _false_label" + this.labelNumber_false + endLine);
			ifStatement.getOpStmt().accept(this,tempNum);
		
			transBuilder.append("Jump _end_label"+ this.labelNumber_end + endLine);
			transBuilder.append("_false_label"+ this.labelNumber_false +":" + endLine);
			ifStatement.getElseStmt().accept(this, tempNum);
	
			transBuilder.append("_end_label" + this.labelNumber_end +": " + endLine);
			this.labelNumber_false++;
			this.labelNumber_end++;
		}
		else{
			String trCond = (String) ifStatement.getCondExpr().accept(this, tempNum);
			if (!isTemporary(trCond)){
				transBuilder.append("Move " + trCond + ",R" + tempNum + endLine); 
				trCond="R"+tempNum;				
			}
			transBuilder.append("Compare 1," + trCond + endLine);
			transBuilder.append("JumpFalse _end_label" + this.labelNumber_end + endLine);
			ifStatement.getOpStmt().accept(this,tempNum);
			transBuilder.append("_end_label" + this.labelNumber_end +": " + endLine);
			this.labelNumber_end++;
		}
		transBuilder.append("#end if" + endLine);
		return null;
	}

	public Object visit(WhileStmt whileStatement, int tempNum){
		this.whileNumber=this.labelNumber_end;
		String labelTest = "_test_label" + this.whileNumber;
		String labelEnd = "_end_label" + this.whileNumber;
		
		this.labelNumber_false++;
		this.labelNumber_end++;

		transBuilder.append(labelTest + ":" + endLine);
		String trCond = (String) whileStatement.getCondExpr().accept(this, tempNum); //translate loop's condition
		if (!isTemporary(trCond)){
			transBuilder.append("Move " + trCond + ",R" + tempNum + endLine); 
			trCond="R"+tempNum;				
		}
		transBuilder.append("Compare 0," + trCond + endLine);
		transBuilder.append("JumpTrue " + labelEnd + endLine);

		whileStatement.getOpStmt().accept(this, tempNum); //translate while statements
		transBuilder.append("Jump " + labelTest + endLine); //jump back to the loop condition
		transBuilder.append(labelEnd  + ":" + endLine); 	// label of loop's end
		
		return "succeed";
	}

	public Object visit(BreakStmt breakStatement, int tempNum){
		String loopEndLabel = "_end_label" + this.whileNumber;
		transBuilder.append("Jump " + loopEndLabel + endLine);
		return "succeed";
	}

	public Object visit(ContinueStmt continueStatement, int tempNum){
		String loopTestLabel = "_test_label" + this.whileNumber;
		transBuilder.append("Jump " + loopTestLabel + endLine);
		return "succeed";
	}

	public Object visit(StmtsBlock statementsBlock, int tempNum){
		for(Stmt stmt : statementsBlock.getStmts()){
			stmt.accept(this, tempNum);
		}

		return "succeed";
	}

	public Object visit(LocalVariable localVariable, int tempNum){//int x=y;
		SymbolTable currScope = localVariable.getEnclosingScope();
		if(localVariable.hasInitValue()){
			String trInitVal = (String) localVariable.getInitValue().accept(this, tempNum); //visit y
			if(!this.isTemporary(trInitVal)){
				transBuilder.append("Move " + trInitVal+ ",R" + tempNum + endLine); //Move y,R3
				trInitVal="R"+tempNum;
			}
			String lirName = currScope.getLocalVariableLirName(localVariable.getName());
			transBuilder.append("Move " + trInitVal + "," + lirName + endLine); //Move R3,x
		}

		return  "succeed";
	}

	public Object visit(LocationVar location, int tempNum){
		boolean isField = false;
		String locationName = location.getID();
		String className = "";
		String translateClass = "";		
		if(location.isPointer()){//location is a.x
			isField = true;//location is field
			Types classType = (Types)location.getLocationExpr().accept(new SemanticChecker(this.programSymTable)); //gets method's class name	
			className = classType.getName();
			translateClass = (String) location.getLocationExpr().accept(this, tempNum); // gets the class of the field
			if(!this.isTemporary(translateClass)){
				transBuilder.append("Move " +translateClass + ",R" + tempNum + endLine);
				TempHierarchy.add(translateClass);
				TempHierarchy.add(",R" + tempNum);
				translateClass = "R" + tempNum;
			}
			else{
				tempNum=getTemporaryNumber(translateClass)+1;
				}
		}
		else { //location is x	(local variable or input parameter)
			SymbolTable currScope = location.getEnclosingScope();
			while (!(currScope.getTableCategory().compareTo(SymbolTableCategories.CLASS) == 0)){
				if (currScope.getTableCategory().compareTo(SymbolTableCategories.STATEMENT_BLOCK) == 0){
					SymbolTableStmtBlock stmtBlock=(SymbolTableStmtBlock) currScope;
					if (stmtBlock.isIdentifierExistByName(locationName)){
						if (currScope.getEntityByName(locationName,	SymbolCategories.LOCAL_VARIABLE) != null){//x is a local variable in a stmt block (like in an IF scope)
							String lirLocalVariableName = currScope.getLocalVariableLirName(locationName);				
							return lirLocalVariableName;
						}
					}
				}
				if (currScope.getTableCategory().compareTo(	SymbolTableCategories.STATIC_METHOD) == 0 || currScope.getTableCategory().compareTo(SymbolTableCategories.VIRTUAL_METHOD) == 0){
				 SymbolTableMethod symTblMethod=(SymbolTableMethod) currScope;
				 if (symTblMethod.isIdentifierExistByName(locationName)){
					if (currScope.getEntityByName(locationName,	SymbolCategories.LOCAL_VARIABLE) != null){//x is local variable in a method
						String lirLocalVariableName = currScope.getLocalVariableLirName(locationName);				
						return lirLocalVariableName;
						
					}else if (currScope.getEntityByName(locationName, SymbolCategories.PARAMETER) != null){//x is input parameter of method				  
						  SymbolTableMethod symbolTableMethod = (SymbolTableMethod) currScope;
							String lirParameterName = symbolTableMethod.getInputParametersLirName(locationName);		
							return lirParameterName;
					}
				 }
				}				
				currScope = currScope.getParent();
			}

			if (currScope.getTableCategory().compareTo(SymbolTableCategories.CLASS) == 0){
			className = currScope.getTableName();
				if (this.mapClassesOffsetsTables.get(className).getMapFieldNameToFIeld().containsKey(locationName)){ //if x is a field go to x offset
					isField = true;
					transBuilder.append("Move this,R" + tempNum + endLine);
					TempHierarchy.add("this");
					TempHierarchy.add("R" + tempNum);
					translateClass = "R" + tempNum;
				}
			}
			else { //identifier is a local variable
				String lirName = currScope.getLocalVariableLirName(locationName);				
				return lirName;
			}
		}
		if(isField){ 
			ClassOffsetsTable classOffsetTbl = this.mapClassesOffsetsTables.get(className);
			int fieldOffset = classOffsetTbl.getFieldOffsetFromFieldName(locationName); 
			transBuilder.append("MoveField " + translateClass + "." + fieldOffset + "," + translateClass + endLine);
			TempHierarchy.add(translateClass + "." + fieldOffset);
			TempHierarchy.add(translateClass);
		}
		return "R" + tempNum;
	}

	public Object visit(LocationArray location, int tempNum){
		String translateArr = (String)location.getArrayExpr().accept(this, tempNum);//a[4]->a
		if(!this.isTemporary(translateArr)){
			transBuilder.append("Move " + translateArr + ",R" + tempNum + endLine);//Move a,R1
			TempHierarchy.add(translateArr);
			TempHierarchy.add("R" + tempNum);
			translateArr = "R" + tempNum;	
			String translateIndex = (String)location.getIndexExpr().accept(this, tempNum+1);
			tempNum++;
			transBuilder.append("MoveArray " + translateArr +"[" + translateIndex  +"],R" + tempNum + endLine);//Move R1[1],R2
			TempHierarchy.add(translateArr +"[" + translateIndex  +"]");
			TempHierarchy.add("R" + tempNum);
			translateArr = "R" + tempNum;
			return translateArr;//return R2
		}
		else{	
			tempNum = getTemporaryNumber(translateArr)+1;
			String translateIndex = (String)location.getIndexExpr().accept(this, tempNum+1);
			transBuilder.append("MoveArray " + translateArr +"[" + translateIndex  +"],R" + tempNum + endLine);//Move R2[1],R3
			TempHierarchy.add(translateArr +"[" + translateIndex  +"]");
			TempHierarchy.add("R" + tempNum);
			translateArr = "R" + tempNum;
			return translateArr;//return R3
		}		
	}

	public Object visit(StaticCall call, int tempNum){
		StringBuilder libTrans = new StringBuilder();
		String className = call.getClassId();
		String methodName = call.getID();
		String resultTemp;
		if(className.compareTo("Library") == 0){ // if it's a library method
			libTrans.append("Library __" + methodName + "(");
			int paramsCount = 1;		
			for(Expr paramExpr : call.getExprList()){//go over libMethod's parameters				
				String translateParamExpr = (String) paramExpr.accept(this, tempNum);
				if(!this.isTemporary(translateParamExpr)){
					transBuilder.append("Move " + translateParamExpr +",R" + tempNum + endLine);
					translateParamExpr="R" + tempNum;
					libTrans.append(translateParamExpr);
					tempNum++;		
				}else{
					libTrans.append(translateParamExpr);
					tempNum=getTemporaryNumber(translateParamExpr)+1;
				}
				if(paramsCount < call.getExprList().size()){
					libTrans.append(",");
				}
				paramsCount++;
			}
			resultTemp = "R" + tempNum;
			libTrans.append(")," + resultTemp + endLine);
		}
		else{ // not a Library method
			libTrans.append("StaticCall _" + className + "_" + methodName + "(");
			SymbolTableClass classSymbolTable = this.programSymTable.getChildScope(className);
			SymbolTableMethod methodSymbolTable = classSymbolTable.getMethodTable(methodName);
			HashMap<Integer, String> formals = methodSymbolTable.getParametersOrder();
			int formalsCounter = 1;
			for(Expr paramExpr : call.getExprList()){//go over method's parameters
				String formalName = formals.get(formalsCounter-1);
				formalName = methodSymbolTable.getInputParametersLirName(formalName);
				libTrans.append(formalName + "=");
				String translateParamExpr = (String)paramExpr.accept(this, tempNum);
				if(!this.isTemporary(translateParamExpr)){
					libTrans.append(translateParamExpr);
				}else{
					libTrans.append("R" + tempNum);
					tempNum++;
				}
				if(formalsCounter < formals.size()){
					libTrans.append(",");
				}
				formalsCounter++;	
			}
			resultTemp = "R" + tempNum;
			libTrans.append(")," + resultTemp + endLine); 
		}
		transBuilder.append(libTrans.toString());
		return resultTemp;
	}

	public Object visit(VirtualCall call, int tempNum) {
		String locTemp = "";
		String	className = "";	
		if(call.hasLocationExpr()){//call is of form a.x (a an instance of class B) 
			Types classType = (Types)call.getLocationExpr().accept(new SemanticChecker(this.programSymTable));//get a
			className = classType.getName();//get B
			locTemp = (String)call.getLocationExpr().accept(this, tempNum);  			
			if(!this.isTemporary(locTemp)){
				transBuilder.append("Move " + locTemp + ",R" + tempNum + endLine);
				transBuilder.append("#__checkNullRef("+locTemp+")"+endLine); // RunTime Check
				locTemp = "R" + tempNum;
			}
			else{
				tempNum=getTemporaryNumber(locTemp)+1;
			}
		}else{
			SymbolTable currScope = call.getEnclosingScope();
			while(!(currScope.getTableCategory().compareTo(SymbolTableCategories.CLASS) == 0)){//find class of a
				currScope = currScope.getParent();
			}
			className = currScope.getTableName();//get A's name
			transBuilder.append("Move this,R" + tempNum + endLine);
			locTemp = "R" + tempNum;
		}

		ClassOffsetsTable classOffsetTbl = this.mapClassesOffsetsTables.get(className);//table for B
		int methodOffset = classOffsetTbl.getMethodOffsetFromName(call.id);
		String resultTemp;	
		Method mObj = classOffsetTbl.getMethodFromName(call.id);
		
		SymbolTableMethod methodSymTbl=null;
		
		if (!mObj.getFormals().isEmpty()){
			methodSymTbl=(SymbolTableMethod) mObj.getFormals().getFirst().getEnclosingScope();
		}
		
		SymbolTable methodTable = call.getEnclosingScope();
		
		while (!(methodTable instanceof SymbolTableMethod)){
			methodTable = call.getEnclosingScope().getParent();
		}
		
		List<Formal> formalsList = mObj.getFormals();
		int formalsCounter = 0;

		LinkedList<String> formalFieldHelper = new LinkedList<String>();
		
		for(Expr paramExpr : call.getExprList()){	
			String trActualParam = (String) paramExpr.accept(this, tempNum+1);//adding the values to formalFieldHelper array list
			formalFieldHelper.add(trActualParam); 
		}
		
		transBuilder.append("VirtualCall " + locTemp + "." + methodOffset + "(");
			
		for(Expr paramExpr : call.getExprList()){//go over call inputs and methods formals
			String formalName = formalsList.get(formalsCounter).getName();
			String formalLirName = methodSymTbl.getInputParametersLirName(formalName);
			transBuilder.append(formalLirName + "=");	
			String trActualParam = formalFieldHelper.getFirst();
			transBuilder.append(trActualParam);	
			formalFieldHelper.removeFirst();
			if(formalsCounter < (formalsList.size()-1)){
				transBuilder.append(",");
			}
			formalsCounter++;
		}
		resultTemp = "R" + tempNum;
		transBuilder.append(")" + "," + resultTemp + endLine);
		return resultTemp;
	}
	
	public Object visit(StaticCallStmt call, int tempNum) {
		return call.call.accept(this, tempNum);
	}

	public Object visit(VirtualCallStmt call, int tempNum) {
		return call.call.accept(this, tempNum);
	}

	public Object visit(ThisExpr thisExpression, int tempNum) {
		transBuilder.append("Move this," + "R" + tempNum + endLine);
		return "R" + tempNum;
	}

	public Object visit(NewClass newClass, int tempNum) {
		ClassOffsetsTable currClassLayout = this.mapClassesOffsetsTables.get(newClass.getName()); 
		int classTransTableSize = currClassLayout.getFieldsOffsetsTableSize();
		transBuilder.append("Library __allocateObject(" + classTransTableSize + "),R"+ tempNum + endLine);
		transBuilder.append("MoveField _DV_" + currClassLayout.getClassName() + ",R" + tempNum + ".0" + endLine);
		return "R" + tempNum;
	}
	
	public String getTotalSizeForAlloc(LinkedList<Expr> arraySize, int tempNum){
		String sizeTemp="R" + tempNum;
		transBuilder.append("Move 0," + sizeTemp + endLine);//Move 2,R1
		for (Expr aDim: arraySize){//for a[5][6] go over 5,6,... 
			String arrSize = (String) aDim.accept(this, tempNum+1);
			if (!isTemporary(arrSize)){
				tempNum++;
				transBuilder.append("Move " +arrSize+",R" + tempNum + endLine);//Move 2,R1
				arrSize="R"+tempNum;
			}
			else{
				tempNum=getTemporaryNumber(arrSize);
			}
			transBuilder.append("Add " +arrSize+"," + sizeTemp + endLine);//Move 2,R1
		}
		return sizeTemp;
	}

	public Object visit(NewArrayExpr newArray, int tempNum) {//build for a[5][6] recursive over 5,6,...	
		Expr aDim=newArray.getSize().getFirst();
		String arrSize = (String) aDim.accept(this, tempNum);
		String allocSize = getTotalSizeForAlloc(newArray.getSize(), tempNum);
		tempNum = getTemporaryNumber(allocSize)+1;
		//String currentTemp = "R" + tempNum;
		//transBuilder.append("Move " + allocSize+"," + currentTemp + endLine);//Move 2,R1
		transBuilder.append("Mul 4," + allocSize + endLine);// mull 4,R1
		transBuilder.append("Library __allocateArray(" + allocSize + "),"
				+ allocSize + endLine);// alloc to R1
		if (newArray.getSize().size() == 1) {
			return allocSize;// alloc to R1
		} else {
			int regNum=tempNum+1;
			for (int i = 0; i < Integer.valueOf(arrSize); i++) {
				LinkedList<Expr> smallerDim = (LinkedList<Expr>) newArray
						.getSize().clone();
				smallerDim.removeFirst();// [5][6]-->[6]
				NewArrayExpr currentArray = new NewArrayExpr(
						newArray.getLine(), newArray.getType(), smallerDim);// new
																			// a[6]
				String reg;
				reg = (String) currentArray.accept(this, regNum);
				transBuilder.append("MoveArray " + reg + "," + allocSize + "["
						+ i + "]" + endLine);// move 4,R2
			}
			
			return "R" + tempNum;
		}
	}

	public Object visit(Length length, int tempNum) {
		String translateLength = (String)length.getArray().accept(this, tempNum);
		transBuilder.append("ArrayLength " + translateLength + ",R" + tempNum + endLine);
		transBuilder.append("#__checkNullRef("+length.getArray()+")"+endLine); // RunTime Check
		return "R" + tempNum;
	}


	public Object visit(MathematicalBinaryExpr binaryOp, int tempNum) {
		
		String transFirstExpr = (String)binaryOp.getFirstExpr().accept(this, tempNum);
		if(!this.isTemporary(transFirstExpr)){
			transBuilder.append("Move " + transFirstExpr + ",R" + tempNum + endLine);
			transFirstExpr = "R" + tempNum;
		}else{
			tempNum=getTemporaryNumber(transFirstExpr);
		}
		
		String transSecondExpr;
		transSecondExpr = (String)binaryOp.getSecondExpr().accept(this, ++tempNum);
		if(!this.isTemporary(transSecondExpr)){
			transBuilder.append("Move " + transSecondExpr + ",R" + tempNum + endLine);
			transSecondExpr = "R" + tempNum;
		}
		else{
			tempNum=getTemporaryNumber(transSecondExpr);
		}		
//	/*	if(binaryOp.getOperator().compareTo(Operator.MINUS) != 0 && binaryOp.getOperator().compareTo(Operator.DIVIDE) != 0 && binaryOp.getOperator().compareTo(Operator.MOD) != 0){
//			if(!this.isTemporary(transSecondExpr)){
//				transBuilder.append("Move " + transSecondExpr + ",R" + tempNum + endLine);
//			}
//		}
//		if(binaryOp.getOperator().compareTo(Operator.MINUS) == 0 || binaryOp.getOperator().compareTo(Operator.DIVIDE) == 0 || binaryOp.getOperator().compareTo(Operator.MOD) == 0){						
//			if(!this.isTemporary(transFirstExpr)){
//				transBuilder.append("Move " + transFirstExpr + ",R" + tempNum + endLine);
//			}
//		}*/
		switch (binaryOp.getOperator()) {
		case MINUS:
			transBuilder.append("Sub " + transSecondExpr + "," + transFirstExpr + endLine);
			return transFirstExpr;
		case DIVIDE:
			transBuilder.append("Div " + transSecondExpr + "," + transFirstExpr + endLine);
			transBuilder.append("#__checkZero("+transSecondExpr+")"+endLine);  // RunTime Check
			return transFirstExpr;
		case MOD:
			transBuilder.append("Mod " + transSecondExpr + "," + transFirstExpr + endLine);
			return transFirstExpr;
		case PLUS:
			Types opExprType = (Types)binaryOp.getFirstExpr().accept(new SemanticChecker(this.programSymTable));
			if(opExprType.isSubtypeOf(TableOfTypes.typeInt)){
				if(this.isTemporary(transFirstExpr)){
					transBuilder.append("Add " + transSecondExpr +"," + transFirstExpr + endLine);
					return transFirstExpr;
				}
				else{
					transBuilder.append("Add " + transFirstExpr +"," + transSecondExpr + endLine);
					return transSecondExpr;
				}
			}
			else if(opExprType.isSubtypeOf(TableOfTypes.typeString)){
				transBuilder.append("Library __stringCat(" + transFirstExpr + "," + transSecondExpr + "),R" + (++tempNum) + endLine);
			}
			return "R" + tempNum;
		case MULTIPLY:
			if(this.isTemporary(transFirstExpr)){
				transBuilder.append("Mul " + transSecondExpr +"," + transFirstExpr + endLine);
				return transFirstExpr;
			}
			else{
				transBuilder.append("Mul " + transFirstExpr +"," + transSecondExpr + endLine);
				return transSecondExpr;
			}

		default:
			System.out.println("Error occured during translation.");
			return "R" + tempNum;
		}
	}

	public Object visit(LogicalBinaryExpr binaryOp, int tempNum) {
		String labelTrue = "_true_label" + this.labelNumber_true;
		this.labelNumber_true++;
		String labelFalse = "_false_label" + this.labelNumber_false;
		this.labelNumber_false++;
		String labelEnd = "_end_label" + this.labelNumber_end;
		this.labelNumber_end++;

		String transFirstExpr = (String)binaryOp.getFirstExpr().accept(this, tempNum);
		if(!this.isTemporary(transFirstExpr)){
			transBuilder.append("Move " + transFirstExpr + ",R" + tempNum + endLine);
			transFirstExpr = "R" + tempNum;
		}else{
			tempNum=getTemporaryNumber(transFirstExpr);
		}
		
		String transSecondExpr;
		transSecondExpr = (String)binaryOp.getSecondExpr().accept(this, ++tempNum);
		if(!this.isTemporary(transSecondExpr)){
			transBuilder.append("Move " + transSecondExpr + ",R" + tempNum + endLine);
			transSecondExpr = "R" + tempNum;
		}
		else{
			tempNum=getTemporaryNumber(transSecondExpr);
		}
		if(binaryOp.getOperator().compareTo(Operator.LOR) != 0 && binaryOp.getOperator().compareTo(Operator.LAND) != 0){
			if(this.isTemporary(transFirstExpr)){
				transBuilder.append("Compare " + transSecondExpr + "," + transFirstExpr + endLine);
			}
			else{
				transBuilder.append("Compare " + transFirstExpr + "," + transSecondExpr + endLine);
			}
		}
		switch (binaryOp.getOperator()) {
		case EQUAL:
			transBuilder.append("JumpTrue " + labelTrue + endLine);
			break;
		case NEQUAL:
			transBuilder.append("JumpFalse " + labelTrue + endLine);
			break;
		case GT:
			transBuilder.append("JumpG " + labelTrue + endLine);
			break;
		case GTE:
			transBuilder.append("JumpGE " + labelTrue + endLine);
			break;
		case LT:
			transBuilder.append("JumpL " + labelTrue + endLine);
			break;
		case LTE:
			transBuilder.append("JumpLE " + labelTrue + endLine);
			break;
		case LAND:
			transBuilder.append("Compare 0,R" + tempNum + endLine);
			transBuilder.append("JumpTrue " + labelEnd + endLine);
			transBuilder.append("And " + transSecondExpr + ",R" + tempNum+ endLine);
			transBuilder.append(labelEnd + ":" + endLine);
			break;
		case LOR:
			transBuilder.append("Compare 1,R" + tempNum + endLine);
			transBuilder.append("JumpTrue " + labelEnd + endLine);
			transBuilder.append("Or " + transSecondExpr + ",R" + tempNum + endLine);
			transBuilder.append(labelEnd + ":" + endLine);
			break;
		default:
			System.out.println("Error occured during translation.");
			break;
		}
		if(binaryOp.getOperator().compareTo(Operator.LOR) != 0 && binaryOp.getOperator().compareTo(Operator.LAND) != 0){
			transBuilder.append(labelFalse + ":" + endLine);
			transBuilder.append("Move 0,R" + tempNum + endLine);
			transBuilder.append("Jump " + labelEnd + endLine);
			transBuilder.append(labelTrue + ":" + endLine);
			transBuilder.append("Move 1,R" + tempNum + endLine);
			transBuilder.append(labelEnd + ":" + endLine);
		}
		return "R" + tempNum;
	}

	public Object visit(MathematicalUnaryExpr unaryOp, int tempNum) {
		String transOnlyExpr = (String)unaryOp.getExpr().accept(this, tempNum);
		if(!this.isTemporary(transOnlyExpr)){
			transBuilder.append("Move " + transOnlyExpr + ",R" + tempNum + endLine);
			transOnlyExpr="R" + tempNum;
		}
		transBuilder.append("Neg " + transOnlyExpr + endLine);
		return transOnlyExpr;
	}

	public Object visit(LogicalUnaryExpr unaryOp, int tempNum) {
		String transOnlyExpr = (String)unaryOp.getExpr().accept(this, tempNum);
		if(!this.isTemporary(transOnlyExpr)){
			transBuilder.append("Move " + transOnlyExpr + ",R" + tempNum + endLine);
			transOnlyExpr="R" + tempNum;
		}
		transBuilder.append("Not " + transOnlyExpr + endLine);
		return transOnlyExpr;
	}
	
	public Object visit(LiteralExpr literal, int tempNum) {
		switch (literal.getType()) {
		case INTEGER: 
			return literal.getValue().toString();
			
		case STRING:
			String strVariable = literal.getValue().toString();	
			if(!this.mapStringLiterals.containsValue(strVariable)){
				mapStringLiterals.put("str" + this.strLiteralsCount,strVariable);
				this.strLiteralsCount++;
				return "str" + (this.strLiteralsCount-1);
			}
			else{
				for (String key: mapStringLiterals.keySet()){
					if (mapStringLiterals.get(key).equals(strVariable)){
						return key;
					}
				}
			}			
		case NULL:
			String nullStr = "\"null\"";
			if(!this.mapStringLiterals.containsValue(nullStr)){
				mapStringLiterals.put("str" + this.strLiteralsCount,nullStr);
				this.strLiteralsCount++;
			}
			return "str" + (this.strLiteralsCount-1);
			//return "0";
		case FALSE:
			return "0";
		case TRUE:
			return "1";
		}
		return "succeed";
	}
	
	private void methodTranslator(Method method, int tempNum){
		String methodName = method.getName();
		if(!methodName.equals("main")){ //check what with main
			SymbolTable methodScope = method.getEnclosingScope();
			String EnclosingClassName = methodScope.getTableName();
			transBuilder.append("_" + EnclosingClassName + "_" + methodName + ":" + endLine);
		}

		for (Stmt stmt : method.getStmts()){
			stmt.accept(this, tempNum);
		}
		
		if(!methodName.equals("main")){
			if(!this.isReturnStmtExist){ // If the method doesn't have a return statement - append return null
			transBuilder.append("Return 0" + endLine);
			}else{
				this.isReturnStmtExist = false;
			}
		}
	}


}
