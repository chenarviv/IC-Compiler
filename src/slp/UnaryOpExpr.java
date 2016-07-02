package slp;

/** An AST node for unary expressions.
 */
public abstract class UnaryOpExpr extends Expr {
	public final Operator op;
	public final Expr operand;
	
	public UnaryOpExpr(int line,Expr operand, Operator op) {
		super(line);
		this.operand = operand;
		this.op = op;
	}
	
	public String toString() {
		return op + operand.toString();
	}
	
	public Expr getExpr() {
		return this.operand;
	}
	
	public Operator getOperator() {
		return this.op;
	}
}