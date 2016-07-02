package SymbolTable;
import Types.*;

public class SymbolEntityStaticMethod extends SymbolEntityMethod {
	public SymbolEntityStaticMethod(String id, Types symbolType) {
		super(id, SymbolCategories.STATIC_METHOD, symbolType);
	}
}
