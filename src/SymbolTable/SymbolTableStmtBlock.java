package SymbolTable;
import Types.*;
import java.util.HashMap;
import Semantic.SemanticError;


public class SymbolTableStmtBlock extends SymbolTable {

	private HashMap<String, SymbolEntity> localVariablesEntities = new HashMap<String, SymbolEntity>();
	private HashMap<String, Integer> localVariableDeclerationLine = new HashMap<String, Integer>();
	private HashMap<Integer, SymbolTableStmtBlock> tableOfStmtBlockChildren = new HashMap<Integer,SymbolTableStmtBlock>();
	private SymbolTable parentTable; 


	private int statementBlockCounter = 0;

	public SymbolTableStmtBlock(SymbolTable ParentTable, String tableEntity) {
		super(SymbolTableCategories.STATEMENT_BLOCK, ParentTable, tableEntity);
		this.parentTable=ParentTable;
	}

	@Override
	public void addEntity(String idName, SymbolEntity newLocalVarSym, int line) {

		if(!isIdentifierExist(newLocalVarSym)){
			localVariableDeclerationLine.put(idName, line);
			localVariablesEntities.put(idName, newLocalVarSym);
		}
		else{
			try {
				throw new SemanticError(line,
						"Duplicate declaration for identifier '"
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
		tableOfStmtBlockChildren.put(statementBlockCounter, (SymbolTableStmtBlock) childTable);
		statementBlockCounter++;

	}
	@Override
	protected boolean isIdentifierExist(SymbolEntity entry){
		return localVariablesEntities.containsKey(entry.getId());
	}
	
	public boolean isIdentifierExistByName (String entry) {
		if (!localVariablesEntities.containsKey(entry)){ //key wasn't found in current stmt block
			return false;
			}
		return true;
		}
		

	@Override
	public Object searchEntityInThisTable(String name, SymbolCategories symbolCat){
		if(symbolCat.getCategory().compareTo(SymbolCategories.LOCAL_VARIABLE.getCategory())==0){
			if(localVariablesEntities.containsKey(name)){
				return localVariablesEntities.get(name);
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
	public void setVariableType(String varName, Types type) {
		localVariablesEntities.get(varName).setType(type);

	}

	@Override
	public SymbolEntity searchForVariableIdentifier(String id, int line){
		if(localVariablesEntities.containsKey(id)){
			if(localVariableDeclerationLine.get(id) < line)
				return localVariablesEntities.get(id);
			}
		return this.getParent().searchForVariableIdentifier(id,line);
	}

}
