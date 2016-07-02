package SymbolTable;
import Types.*;

import java.util.HashMap;

import Semantic.*;


public class SymbolTableClass extends SymbolTable {
	private HashMap<String, SymbolEntity> fieldEntities = new HashMap<String, SymbolEntity>();
	private HashMap<String, SymbolEntity> staticMethodEntities = new HashMap<String, SymbolEntity>();
	private HashMap<String, SymbolEntity> virtualMethodEntities = new HashMap<String, SymbolEntity>();
	private HashMap<String,SymbolTableClass> tablesOfChildrenClasses = new HashMap<String,SymbolTableClass>();
	private HashMap<String,SymbolTableMethod> tablesOfChildrenMethods = new HashMap<String,SymbolTableMethod>();

	public SymbolTableClass(SymbolTable ParentTable, String tableEntity) {
		super(SymbolTableCategories.CLASS, ParentTable, tableEntity);

	}

	@Override
	public void addEntity(String idName, SymbolEntity newEntrySym, int line) {//add entity to class table
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
		if(newEntrySym.getSymbolCategory().compareTo(SymbolCategories.FIELD) == 0){
			fieldEntities.put(idName, newEntrySym);
		}
		else if (newEntrySym.getSymbolCategory().compareTo(SymbolCategories.VIRTUAL_METHOD) == 0){
			virtualMethodEntities.put(idName,newEntrySym);
		}
		else if (newEntrySym.getSymbolCategory().compareTo(SymbolCategories.STATIC_METHOD) == 0){
			staticMethodEntities.put(idName, newEntrySym);
		}
	}


	@Override
	protected void addChildScope(String childName, SymbolTable childTable) {//add a child scope (class or method)
		if(childTable.getTableCategory().compareTo(SymbolTableCategories.CLASS) == 0){
			tablesOfChildrenClasses.put(childName, (SymbolTableClass) childTable);
		}
		else if(childTable.getTableCategory().compareTo(SymbolTableCategories.STATIC_METHOD) == 0){
			tablesOfChildrenMethods.put(childName, (SymbolTableMethod) childTable);
		}	

		else if(childTable.getTableCategory().compareTo(SymbolTableCategories.VIRTUAL_METHOD) == 0){
			tablesOfChildrenMethods.put(childName,(SymbolTableMethod) childTable);
		}	
	}

	@Override
	protected boolean isIdentifierExist(SymbolEntity entry) {//check if id already exists in the class table or in his parents table
		if(fieldEntities.containsKey(entry.getId()) || virtualMethodEntities.containsKey(entry.getId()) ||
				staticMethodEntities.containsKey(entry.getId())) //check if the id already exist in the the current class scope 
			return true;
		if(getParent().getTableCategory().compareTo(SymbolTableCategories.CLASS)==0){//check if the id exists in his parents tables
			if( this.getParent().isIdentifierExist(entry) &&
					entry.getSymbolCategory().equals(SymbolCategories.FIELD))
				return true;
		}
		return false;

	}
 
	public boolean isLegalMethod(SymbolEntity methodEntry){//check if the method doesn't overload another method
		SymbolEntity foundedEntry;
		if(methodEntry.getSymbolCategory().equals(SymbolCategories.STATIC_METHOD)){
			foundedEntry=(SymbolEntity) this.getParent().getEntityByName(methodEntry.getId(), SymbolCategories.VIRTUAL_METHOD);
			if(foundedEntry!=null)
				return false;
		}
		if(methodEntry.getSymbolCategory().equals(SymbolCategories.VIRTUAL_METHOD)){
			foundedEntry=(SymbolEntity) this.getParent().getEntityByName(methodEntry.getId(), SymbolCategories.STATIC_METHOD);
			if(foundedEntry!=null)
				return false;
		}
		foundedEntry = (SymbolEntity) this.getParent().getEntityByName(methodEntry.getId(), methodEntry.getSymbolCategory());
		if(foundedEntry==null)
			return true;
		else{
			return methodEntry.getType().isSubtypeOf(foundedEntry.getType());//method is of the same type as the method in the parent's table 
		}
	}
	
	public boolean isNotOverloadingField(SymbolEntity methodEntry){//check if the method isn't overloading a field
		SymbolEntity foundedEntry;
		foundedEntry=(SymbolEntity) this.getParent().getEntityByName(methodEntry.getId(), SymbolCategories.FIELD);
		if(foundedEntry!=null){
			return false;
		}
		foundedEntry = (SymbolEntity) this.getParent().getEntityByName(methodEntry.getId(), methodEntry.getSymbolCategory());
		if(foundedEntry==null){
			return true;
		}
		return true;
	}

	@Override
	public Object searchEntityInThisTable(String name, SymbolCategories symbolCat){//search entity in thos table
		if(symbolCat.getCategory().compareTo(SymbolCategories.VIRTUAL_METHOD.getCategory())==0){
			if(virtualMethodEntities.containsKey(name)){
				return virtualMethodEntities.get(name);
			}
		}
		else if(symbolCat.getCategory().compareTo(SymbolCategories.STATIC_METHOD.getCategory())==0){
			if(staticMethodEntities.containsKey(name)){
				return staticMethodEntities.get(name);
			}
		}
		else if(symbolCat.getCategory().compareTo(SymbolCategories.FIELD.getCategory())==0){
			if(fieldEntities.containsKey(name)){
				return fieldEntities.get(name);
			}
		}
		return null;
	}

	@Override
	public Object getEntityByName(String name, SymbolCategories symbolKind) {
		Object FoundEntity = searchEntityInThisTable(name, symbolKind);
		if (FoundEntity!=null){
			return FoundEntity;
		}
		else{
			return this.getParent().getEntityByName(name, symbolKind);
		}
	}

	@Override
	public void setVariableType(String fieldName, Types type) {
		fieldEntities.get(fieldName).setType(type);			
	}

	@Override	
	public SymbolEntity searchForVariableIdentifier(String id, int line){//search for an entity with the same id, which appear in the current class symbol table, or in one of its superclass (the closer one)
		if(this.fieldEntities.containsKey(id))
			return fieldEntities.get(id);
		return this.getParent().searchForVariableIdentifier(id , line);//recursive search going from this scope to the parents 
	}
	
	public SymbolTableClass getClassTable(String className){//returns the child class symbol table, with the given name (or null, if it doesn't exist)
		return tablesOfChildrenClasses.get(className);
	}
	
	public SymbolTableMethod getMethodTable(String methodName){//returns the child method symbol table, with the given name (or null, if it doesn't exist)
		return this.tablesOfChildrenMethods.get(methodName);
	}
	
}
