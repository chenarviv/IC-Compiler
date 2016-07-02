package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

/**
 * An AST node for assignment statements.
 */
public class WhileStmt extends Stmt {
	public final Expr expr;
	public final Stmt stmt;
	
	public WhileStmt(int line, Expr exp, Stmt stm) {
		super(line);
		this.expr=exp;
		this.stmt=stm;
	}
		
	/**
	 * Accepts a visitor object as part of the visitor pattern.
	 * 
	 * @param visitor
	 *            A visitor.
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
	
	public Expr getCondExpr(){
		return this.expr;
	}
	
	public Stmt getOpStmt(){
		return this.stmt;
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
	
}