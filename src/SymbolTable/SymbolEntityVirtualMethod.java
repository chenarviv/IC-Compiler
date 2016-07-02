package SymbolTable;
import Types.*;

public class SymbolEntityVirtualMethod extends SymbolEntityMethod {
	public SymbolEntityVirtualMethod(String id, Types symbolType) {
		super(id, SymbolCategories.VIRTUAL_METHOD, symbolType);
	}
}
