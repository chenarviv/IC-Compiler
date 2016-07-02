package slp;
import SymbolTable.SymbolTable;

	/** An AST node for binary expressions.
	 */
	public abstract class CallStmt extends Stmt {
		
		public final Call call;
		
		public CallStmt(int line, Call call) {
			super(line);
			this.call=call;
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

		public Call getCall(){
			return this.call;
		}
			
	}
