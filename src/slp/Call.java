package slp;
import java.util.LinkedList;

	/** An AST node for binary expressions.
	 */
	public abstract class Call extends Expr {
		public final LinkedList<Expr> expList;
		public final String id;
		
		
		public Call(int line, String id, LinkedList<Expr> expList) {
			super(line);
			this.id=id;
			this.expList = expList;
		}
		
		public String getID() {
			return this.id;
		}	
		
		public LinkedList<Expr> getExprList() {
			return this.expList;
		}
	}
