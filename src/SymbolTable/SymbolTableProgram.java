package SymbolTable;
import Types.*;
import java.util.HashMap;
import java.util.LinkedList;
import Semantic.SemanticError;


public class SymbolTableProgram extends SymbolTable{

	private HashMap<String, SymbolEntityClass> classEntities = new HashMap<String, SymbolEntityClass>();
	private HashMap<String,SymbolTableClass> mapOfAllTheClassesTables = new HashMap<String,SymbolTableClass>();

	public SymbolTableProgram(String programName) {
		super(SymbolTableCategories.GLOBAL,null,programName);

	}

	@Override
	public void addEntity(String idName, SymbolEntity newClassSym, int line) {
		if(!isIdentifierExist(newClassSym)){
			classEntities.put(idName, (SymbolEntityClass)newClassSym);
		}
		else{
			try {
				throw new SemanticError(line,
						"Duplicate declaration for class '"
								+ idName
								+ "'");
			}
			catch (SemanticError e) {
				System.out.println(e.getErrorMessage());
				System.exit(-1);
			}
		}

	}

	@Override
	protected void addChildScope(String childName, SymbolTable childTable) {
		mapOfAllTheClassesTables.put(childName, (SymbolTableClass) childTable);
	}

	@Override
	protected boolean isIdentifierExist(SymbolEntity entry) {
		return classEntities.containsKey(entry.getId());
	}


	public SymbolTableClass getChildScope(String childName) { //returns the child symbol table with this specific name
		return mapOfAllTheClassesTables.get(childName);
	}

	@Override
	public Object searchEntityInThisTable(String name, SymbolCategories symbolCategory){
		if(symbolCategory.getCategory().compareTo(SymbolCategories.CLASS.getCategory())==0){
			if(classEntities.containsKey(name)){
				return classEntities.get(name);
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
			return null;
		}
	}



	@Override
	public void setVariableType(String varName, Types type) {
		return;
	}

	@Override
	public SymbolEntity searchForVariableIdentifier(String id, int line) {
		return null;
	}

	 //search for the symbol table of class with the specific name

	public SymbolTableClass searchForClassTableByName(String name){
		SymbolEntityClass classEntry = (SymbolEntityClass) this.searchEntityInThisTable(name, SymbolCategories.CLASS);
		if(classEntry==null)
			return null;
		String className = name;
		LinkedList<String> hierarchyList = new LinkedList<String>();
		hierarchyList.addFirst(name);
		while(this.getChildScope(className)==null){
			className = ((TypeClass) classEntry.getType()).getClassObject().getSuperClassName();
			hierarchyList.addFirst(className);
			classEntry = (SymbolEntityClass) this.searchEntityInThisTable(className, SymbolCategories.CLASS);
		}
		SymbolTableClass currentTable = this.getChildScope(hierarchyList.getFirst());
		for(int i=1;i<hierarchyList.size();i++){
			currentTable = currentTable.getClassTable(hierarchyList.get(i));
		}
		return currentTable;
	}

}
