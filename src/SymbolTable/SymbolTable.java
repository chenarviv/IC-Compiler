package SymbolTable;
import java.util.HashMap;
import java.util.HashSet;

import Types.*;


public abstract class SymbolTable {

	private SymbolTableCategories tableCategory;
	private SymbolTable parentSymbolTable;
	private String tableEntityName;
	private HashSet<String> assignmentTable = new HashSet<String>();
	private HashMap<String,String> localVariablesLirNames = new HashMap<String,String>();
	private int localVariableCounter=0;

	public SymbolTable(SymbolTableCategories tableCat, SymbolTable parentTable, String tableEntity){
		this.tableCategory = tableCat;
		this.parentSymbolTable = parentTable;
		this.tableEntityName = tableEntity;
	}

	public abstract void addEntity(String idName, SymbolEntity newEntrySym, int line);//checks legality of entity and than adds it to the table

	protected abstract void addChildScope(String child_name,SymbolTable child_table);//adds a symbol table child to the symbol table

	protected abstract boolean isIdentifierExist(SymbolEntity symbol);//checks legality of identifier in the current scope

	public String getTableName(){
		return tableEntityName;
	}
	public SymbolTableCategories getTableCategory() {
		return this.tableCategory;
	}
	
	public void setParent(SymbolTable perant) {
		this.parentSymbolTable=perant;
	}
	
	public SymbolTable getParent() {
		return this.parentSymbolTable;
	}
	
	public void addAssignment(String id){//add initialization
		if (!this.assignmentTable.contains(id)){
			this.assignmentTable.add(id);
		}
	}
	
	public boolean isInitializied(String id){//check initialization
		if (this.assignmentTable.contains(id)){
			return true;
		}
		else{
			return false;
		}
	}
	
	public String getLocalVariableLirName(String icName){//get lir name
		if (!this.localVariablesLirNames.containsKey(icName)){
			localVariableCounter++;
			String lirName="localVariable_"+localVariableCounter+"_"+icName;
			this.localVariablesLirNames.put(icName,lirName);
		}
		return this.localVariablesLirNames.get(icName);
	}
	
	
	public abstract void setVariableType(String varName, Types type); //sets the Types field of the entity with the given name
	
	public abstract Object searchEntityInThisTable(String name, SymbolCategories symbolKind);//search for an entity with these (name, category) in the table
	
	public abstract Object getEntityByName(String name, SymbolCategories symbolKind);//return the entity (if exists) with the given name
	
	public abstract SymbolEntity searchForVariableIdentifier(String id,int line);//searching for declaration of local variable
	
}
