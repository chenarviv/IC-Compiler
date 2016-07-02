package SymbolTable;
import Types.*;

public class SymbolEntityLocalVariable extends SymbolEntity{
	public SymbolEntityLocalVariable(String id, Types symbolType) {
		super(id, SymbolCategories.LOCAL_VARIABLE, symbolType);
	}
}
