package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

public class Field extends ASTNode{
	
	private Type type;

	private String name;

	public Object accept(Visitor visitor) {
		 return visitor.visit(this);
	}
	
	
	public Field(int line, Type type, String name) {
		super(line); 
		this.type = type;
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public Object accept(PropagatingVisitor visitor, SymbolTable table) {
		this.setEnclosingScope(table);
		return visitor.visit(this, table);
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}

}

