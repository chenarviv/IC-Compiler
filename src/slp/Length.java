package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

/** An AST node for program variables.
 */
public class Length extends Expr {
	public final Expr expr;
	
	public Length(int line, Expr expr) {
		super(line);
		this.expr = expr;
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
	
	public String toString() {
		return expr.toString();
	}	
	
	public Expr getArray() {
		return this.expr;
	}	
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
}