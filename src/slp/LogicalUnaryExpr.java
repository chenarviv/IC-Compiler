package slp;

import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

public class LogicalUnaryExpr extends UnaryOpExpr {

	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public Object accept(PropagatingVisitor visitor, SymbolTable table) {
		this.setEnclosingScope(table);
		return visitor.visit(this, table);
	}

	public LogicalUnaryExpr(int line, Expr operand, Operator operator) {
		super(line, operand, operator);
	}

	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
}
