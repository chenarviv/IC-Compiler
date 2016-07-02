package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

/**
 * An AST node for assignment statements.
 */
public class AssignStmt extends Stmt {
	public final Location loc;
	public Expr rhs=null;
	
	public AssignStmt(int line, Location loc) {
		super(line);
		this.loc = loc;
	}

	public AssignStmt(int line, Location loc, Expr rhs) {
		this(line, loc);
		this.rhs = rhs;
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
	
	public Location getLocation(){
		return this.loc;
	}
	public Boolean hasExpr(){
		return (this.rhs!=null);
	}
	public Expr getExpr(){
		return this.rhs;
	}

	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
}