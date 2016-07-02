package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

public class LiteralExpr extends Expr {

	private LiteralTypes type;

	private Object value;
	/**
	 * Constructs a new literal node.
	 * 
	 * @param line
	 *            Line number of the literal.
	 * @param type
	 *            Literal type.
	 */
	public LiteralExpr(int line, LiteralTypes type) {
		super(line);
		this.type = type;
		value = type.getValue();
	}
	/**
	 * Constructs a new literal node, with a value.
	 * 
	 * @param line
	 *            Line number of the literal.
	 * @param type
	 *            Literal type.
	 * @param value
	 *            Value of literal.
	 */
	public LiteralExpr(int line, LiteralTypes type, Object value) {
		this(line, type);
		this.value = value;
	}
	
	/** Accepts a visitor object as part of the visitor pattern.
	 * @param visitor A visitor.
	 */
	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}
	
	/** Accepts a propagating visitor parameterized by two types.
	 * 
	 * @param <DownType> The type of the object holding the context.
	 * @param <UpType> The type of the result object.
	 * @param visitor A propagating visitor.
	 * @param context An object holding context information.
	 * @return The result of visiting this node.
	 */
	@Override
	public Object accept(PropagatingVisitor visitor, SymbolTable table) {
		this.setEnclosingScope(table);
		return visitor.visit(this, table);
	}
	
	public LiteralTypes getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value){
		this.value = value;
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}

}
