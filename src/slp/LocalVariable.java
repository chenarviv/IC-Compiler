package slp;
import lir.PropagatingVisitorLIR;
import SymbolTable.SymbolTable;

	public class LocalVariable extends Stmt {

		private String name;
		public Type type; 

		private Expr initValue = null;

		@Override
		public Object accept(Visitor visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public Object accept(PropagatingVisitor visitor, SymbolTable table) {
			this.setEnclosingScope(table);
			return visitor.visit(this, table);
		}
		
	
		public LocalVariable(int line, Type type, String name) {
			super(line);
			this.name = name;
			this.type=type;
		}


		public LocalVariable(int line, Type type, String name, Expr initValue) {
			this(line, type, name);
			this.initValue = initValue;
			//System.out.print(type.getName()+"\n");
			//System.out.print(type.getDimension()+"\n");
			//System.out.print(initValue.toString()+"\n");
		}



		public String getName() {
			return name;
		}

		public boolean hasInitValue() {
			return (initValue != null);
		}

		public Expr getInitValue() {
			return initValue;
		}
		
		public Type getType() {
			return type;
		}
		
		
		@Override
		public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
			return visitor.visit(this, regNum);
		}
		

	}


