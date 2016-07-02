package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

public class Formal extends ASTNode {

		public Type type;
		public String name;
		
		public Formal(int line, Type type, String name) {
			super(line); 
			this.type = type;
			this.name = name;
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
		
		
		public Type getType() {
			return type;
		}

		public String getName() {
			return name;
		}
		
		@Override
		public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
			return visitor.visit(this, regNum);
		}

}
