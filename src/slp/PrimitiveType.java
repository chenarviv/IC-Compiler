package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

public class PrimitiveType extends Type {

	private DataType type;

	public PrimitiveType(int line, DataType type) {
		super(line);
		this.type = type;
	}

	public String getName() {
		return type.getDescription();
	}
	
	public DataType getType(){
		return type;
	}
	

	@Override
	public Object accept(PropagatingVisitor visitor, SymbolTable table) {
		this.setEnclosingScope(table);
		return visitor.visit(this, table);
	}
	
	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
}
