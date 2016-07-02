package slp;
import SymbolTable.SymbolTable;

import java.util.LinkedList;

import lir.PropagatingVisitorLIR;

public class StmtsBlock extends Stmt{
	
	public final LinkedList<Stmt> stmts_list = new LinkedList<Stmt>();
	/**	 * Constructs a new program node.
	 * 
	 * @param classes
	 *            List of all classes declared in the program.
	 */

	public StmtsBlock (int line) {
		super(line);
	}
	
	public StmtsBlock (int line, Stmt stmt) {
		super(line);
		this.stmts_list.add(stmt);
	}
	
	public void add(Stmt stmt) {
		this.stmts_list.add(stmt);
	}
		
	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}
	
	public LinkedList<Stmt> getStmts() {
		return this.stmts_list;
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

	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}

}
