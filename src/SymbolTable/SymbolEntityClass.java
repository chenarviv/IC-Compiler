package SymbolTable;
import Types.*;

public class SymbolEntityClass extends SymbolEntity {
	
	public SymbolEntityClass(String id, Types symbolType) {
		super(id,SymbolCategories.CLASS,symbolType);
	}
	
}
