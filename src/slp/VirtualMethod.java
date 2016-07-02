package slp;
import SymbolTable.SymbolTable;

import java.util.LinkedList; 

import lir.PropagatingVisitorLIR;

public class VirtualMethod extends Method{
		
		public VirtualMethod(int line, Type type, String name, LinkedList<Formal> formals, LinkedList<Stmt> stmts) {
			super(line, type, name, formals, stmts);
		}
		
		@Override
		public Object accept(Visitor visitor) {
			return visitor.visit(this);
		}

		@Override
		public Object accept(PropagatingVisitor visitor, SymbolTable table) {
			this.setEnclosingScope(table);
			return visitor.visit(this, table);
		}	

		@Override
		public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
			return visitor.visit(this, regNum);
		}
		
	}
