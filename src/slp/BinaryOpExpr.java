package slp;

/** An AST node for binary expressions.
 */
public abstract class BinaryOpExpr extends Expr {
	public final Expr lhs;
	public final Expr rhs;
	public final Operator op;
	
	public BinaryOpExpr(int line, Expr lhs, Expr rhs, Operator op) {
		super(line);
		this.lhs = lhs;
		this.rhs = rhs;
		this.op = op;
	}
	
	public String toString() {
		return lhs.toString() + op + rhs.toString();
	}	
	
	public Expr getFirstExpr(){
		return this.lhs;
	}
	
	public Expr getSecondExpr(){
		return this.rhs;
	}
	
	public Operator getOperator(){
		return this.op;
	}
}