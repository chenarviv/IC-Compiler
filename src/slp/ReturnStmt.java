package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

/** An AST node for print statements.
 */
public class ReturnStmt extends Stmt {
	public Expr expr=null;
	
	public ReturnStmt(int line, Expr expr) {
		super(line);
		this.expr = expr;
	}
	
	public ReturnStmt(int line) {
		super(line);
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
	
	public boolean hasReturnExpr(){
		return (this.expr!=null);
	}
	
	public Expr gerReturnExpr(){
		return this.expr;
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
}