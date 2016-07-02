package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

	public class UserType extends Type {

		private String name;
	
		public UserType(int line, String name) {
			super(line);
			this.name = name;
		}

		public String getName() {
			return name;
		}
		

		@Override
		public Object accept(PropagatingVisitor visitor, SymbolTable table) {
			this.setEnclosingScope(table);
			return visitor.visit(this, table);
		}
		
		@Override
		public Object accept(Visitor visitor) {
			return visitor.visit(this);
		}

		@Override
		public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
			return visitor.visit(this, regNum);
		}
		
	}

