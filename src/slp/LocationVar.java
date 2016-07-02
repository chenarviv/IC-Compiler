package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

/** An AST node for program variables.
 */
public class LocationVar extends Location {
	public final String id;
	public Expr exprLocation=null;
	
	public LocationVar(int line, String id, Expr expr1) {
		super(line);
		this.id = id;
		this.exprLocation=expr1;
	}
	
	public LocationVar(int line, String id) {
		super(line);
		this.id = id;
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
	
	public boolean isPointer() {
		return (this.exprLocation!=null);
	}
	
	public String getID(){
		return this.id;
	}

	public Expr getLocationExpr(){
		return this.exprLocation;
	}
	
	public String toString(){
		String locStr=null;;
		if (this.id!=null && this.exprLocation==null){
			locStr=this.id;
		}
		else {
			locStr=this.exprLocation+"."+this.id;
		}
		return locStr;
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
}