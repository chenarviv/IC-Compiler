package SymbolTable;
import Types.*;


public class SymbolEntityField extends SymbolEntity {
	
	public SymbolEntityField(String id, Types symbolType) {
		super(id, SymbolCategories.FIELD, symbolType);
	}
}
