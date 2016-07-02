package SymbolTable;

public enum SymbolCategories {

	CLASS("Class"),
	VIRTUAL_METHOD("Virtual Method"), 
	STATIC_METHOD("Static Method"), 
	FIELD("Field"), 
	PARAMETER("Parameter"),
	LOCAL_VARIABLE("Local Variable");
	
	private String cat;
	
	private SymbolCategories(String cat) {
		this.cat = cat;
	}
	
	public String getCategory() {
		return this.cat;
	}

}
