package SymbolTable;

public enum SymbolTableCategories {

	GLOBAL("Global Symbol Table"),
	CLASS("Class Symbol Table"),
	VIRTUAL_METHOD("Virtual Method Symbol Table"),
	STATIC_METHOD("Static Method Symbol Table"),
	STATEMENT_BLOCK("Statement Block Symbol Table"); 
	
	
	private String cat;
	
	private SymbolTableCategories(String cat) {
		this.cat = cat;
	}
	
	public String getCategory() {
		return this.cat;
	}

}
