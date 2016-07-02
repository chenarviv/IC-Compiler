package slp;
import SymbolTable.SymbolTable;

import java.util.LinkedList;

import lir.PropagatingVisitorLIR;

/** An AST node for binary expressions.
 */
public class VirtualCall extends Call {
	
	public final Expr location_expr;
	
	public VirtualCall(int line, Expr location_expr, String idName, LinkedList<Expr> expList) {
		super(line, idName, expList);
		this.location_expr=location_expr;
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
	
	public boolean hasLocationExpr(){
		return (this.location_expr!=null);
	}
	
	public Expr getLocationExpr(){
		return this.location_expr;
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
}

