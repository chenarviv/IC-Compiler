package SymbolTable;
import Types.*;

import java.util.HashMap;

import Semantic.SemanticError;


public class SymbolTableMethod extends SymbolTable {

	public HashMap<String, SymbolEntity> localVariablesEntities = new HashMap<String, SymbolEntity>();
	private HashMap<String, Integer> localVariableDeclerationLine = new HashMap<String, Integer>();
	private HashMap<String, SymbolEntity> parameterEntities = new HashMap<String, SymbolEntity>();
	private HashMap<Integer, SymbolTableStmtBlock> tableOfStmtBlockChildren = new HashMap<Integer,SymbolTableStmtBlock>();
	
	private HashMap<Integer,String> parametersOrder = new HashMap<Integer,String>();
	private int parametersOrderCounter = 0;
	private HashMap<String,String> inputParametersLirName = new HashMap<String,String>();
	private int inputParametersCounter = 0;
	private int statementBlockCounter = 0;

	public SymbolTableMethod(SymbolTable parentTable, String tableEntity, SymbolTableCategories cat) {
		super(cat, parentTable, tableEntity);
	}

	@Override
	public void addEntity(String idName, SymbolEntity newEntrySym, int line) {
		if(isIdentifierExist(newEntrySym)){
			try {
				throw new SemanticError(line,
						"Duplicate declaration for '"
								+ idName
								+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}
		if(newEntrySym.getSymbolCategory().compareTo(SymbolCategories.PARAMETER) == 0){
			parameterEntities.put(idName, newEntrySym);
			parametersOrder.put(parametersOrderCounter, idName);
			parametersOrderCounter++;
			
		}
		else if (newEntrySym.getSymbolCategory().compareTo(SymbolCategories.LOCAL_VARIABLE) == 0){
			localVariableDeclerationLine.put(idName, line);
			localVariablesEntities.put(idName, newEntrySym);
		}
	}


	@Override
	protected void addChildScope(String childName, SymbolTable childTable) {
		tableOfStmtBlockChildren.put(statementBlockCounter, (SymbolTableStmtBlock) childTable);
		statementBlockCounter++;
	}

	@Override
	protected boolean isIdentifierExist(SymbolEntity symbol) {
		if(localVariablesEntities.containsKey(symbol.getId()) || parameterEntities.containsKey(symbol.getId()))
			return true;
		else
			return false;
	}

	@Override
	public Object searchEntityInThisTable(String name, SymbolCategories symbolCategory){
		if(symbolCategory.getCategory().compareTo(SymbolCategories.LOCAL_VARIABLE.getCategory())==0){
			if(localVariablesEntities.containsKey(name)){
				return localVariablesEntities.get(name);
			}
		}
		else if(symbolCategory.getCategory().compareTo(SymbolCategories.PARAMETER.getCategory())==0){
			if(parameterEntities.containsKey(name)){
				return parameterEntities.get(name);
			}
		}

		return null;
	}

	@Override
	public Object getEntityByName(String name, SymbolCategories symbolCategory) {
		Object FoundEntity = searchEntityInThisTable(name, symbolCategory);
		if (FoundEntity!=null){
			return FoundEntity;
		}
		else{
			return this.getParent().getEntityByName(name, symbolCategory);
		}
	
	}

	public boolean isParameter (String name) {
	return (parameterEntities.containsKey(name));
	}

	@Override
	public void setVariableType(String fieldName, Types type) {
		if(localVariablesEntities.containsKey(fieldName)){
			localVariablesEntities.get(fieldName).setType(type);
		}
		else if(parameterEntities.containsKey(fieldName)){
			parameterEntities.get(fieldName).setType(type);
		}

	}

	@Override
	public SymbolEntity searchForVariableIdentifier(String id, int line){
		if(localVariablesEntities.containsKey(id)){
			if(localVariableDeclerationLine.get(id) < line)
				return localVariablesEntities.get(id);
			}
		else if(this.parameterEntities.containsKey(id))
			return parameterEntities.get(id);
		return this.getParent().searchForVariableIdentifier(id, line);
	}
	
	public HashMap<Integer,String> getParametersOrder(){
		return this.parametersOrder;
	}
	
	public String getInputParametersLirName(String icName){//get lir name

		if (!this.inputParametersLirName.containsKey(icName)){
			inputParametersCounter++;
			String lirName="inputParameter_"+inputParametersCounter+"_"+icName;
			this.inputParametersLirName.put(icName,lirName);
		}
		return this.inputParametersLirName.get(icName);
	}
	
	public boolean isIdentifierExistByName (String entry) {
		if (!localVariablesEntities.containsKey(entry) && !parameterEntities.containsKey(entry)){ //key wasn't found in current scope
			return false; 
			}
		return true;
	}
}
