package slp;
import SymbolTable.SymbolTable;

import java.util.LinkedList;

import lir.PropagatingVisitorLIR;

public class Program extends ASTNode {

	public final LinkedList<ICClass> ic_classes = new LinkedList<ICClass>();
	/**	 * Constructs a new program node.
	 * 
	 * @param classes
	 *            List of all classes declared in the program.
	 */
	public Program (int line, ICClass ic_class) {
		super(line);
		this.ic_classes.add(ic_class);
	}
	
	public void add(ICClass ic_class) {
		this.ic_classes.add(ic_class);
	}
		
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
	
	/**
	 * adds the library class AST to the IC program AST
	 * @param icClass - the Library class
	 */
	
	public void addLibraryClass(ICClass icClass) {
		this.ic_classes.add(icClass);
	}
	
	public LinkedList<ICClass> getClasses(){
		return this.ic_classes;
	}

	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
	
}