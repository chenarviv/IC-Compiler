package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

/** An AST node for program variables.
 */
public class LocationArray extends Location {
	public final Expr ArrayExpr;
	public final Expr IndexExpr;
	
	public LocationArray(int line, Expr expr1, Expr expr2) {
		super(line);
		this.ArrayExpr=expr1;
		this.IndexExpr=expr2;
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
	
	public Expr getArrayExpr(){
		return this.ArrayExpr;
	}
	
	public Expr getIndexExpr(){
		return this.IndexExpr;
	}
	
	public String toString(){
		String locStr=this.ArrayExpr+"["+this.IndexExpr+"]";
		return locStr;
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
}