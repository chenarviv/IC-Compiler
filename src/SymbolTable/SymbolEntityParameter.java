package SymbolTable;
import Types.*;

public class SymbolEntityParameter extends SymbolEntity {
	public SymbolEntityParameter(String id, Types symbolType) {
		super(id, SymbolCategories.PARAMETER, symbolType);
	}
}
