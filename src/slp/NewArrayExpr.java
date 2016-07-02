package slp;
import java.util.LinkedList;

import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

public class NewArrayExpr extends Expr {

	public final Type type;
	public final LinkedList<Expr> init_size_expr;
	
	public NewArrayExpr(int line, Type type, LinkedList<Expr> expr) {
		super(line);
		this.type = type;
		this.init_size_expr = expr;
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
		
	public Type getType(){
		return this.type;
	}
	
	public LinkedList<Expr> getSize(){
		return this.init_size_expr;
	}
	
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
}
	
	
	
