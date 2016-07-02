package slp;
import SymbolTable.SymbolTable;

import java.util.LinkedList;

import lir.PropagatingVisitorLIR;

/** An AST node for binary expressions.
 */
public class StaticCall extends Call {
	
	public final String class_id;

	public StaticCall(int line, String cl_id, String idName, LinkedList<Expr> expList) {
		super(line, idName, expList);
		this.class_id=cl_id;
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
	
	public String getClassId(){
		return this.class_id;
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
}

