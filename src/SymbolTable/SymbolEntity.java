package SymbolTable;
import Types.*;

public class SymbolEntity {
	private String id;//entity name
	private SymbolCategories symbolCategory;//category of entity (static method, virtual method, class...)
	private Types type;//entity type (Types)
	
	public SymbolEntity(String id, SymbolCategories symbolCategory, Types type) {
		this.symbolCategory = symbolCategory;
		this.type = type;	
		this.id = id;
	}
	
	
	public SymbolCategories getSymbolCategory() {
		return this.symbolCategory;
	}
	
	public Types getType() {
		return this.type;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setType(Types type) {
		 this.type = type;
	}
	
}
