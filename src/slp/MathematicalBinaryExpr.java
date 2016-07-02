package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

public class MathematicalBinaryExpr extends BinaryOpExpr {


	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}
		
		@Override
		public Object accept(PropagatingVisitor visitor, SymbolTable table) {
			this.setEnclosingScope(table);
			
			return visitor.visit(this, table);
		}


		public MathematicalBinaryExpr(int line, Expr lhs, Operator operator, Expr rhs) {
			super(line, lhs,rhs,operator);
		}

		@Override
		public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
			return visitor.visit(this, regNum);
		}

}
