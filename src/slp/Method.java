package slp;
import java.util.LinkedList; 

public abstract class Method extends ASTNode {

	public Type type;
	public String name;
	public LinkedList<Formal> formals = new LinkedList<Formal>();
	public LinkedList<Stmt> stmts = new LinkedList<Stmt>();;

	
	public Method (int line, Type type, String name, LinkedList<Formal> formals, LinkedList<Stmt> stmts) {
		super(line);
		this.type = type;
		this.name = name;
		this.formals = formals;
		this.stmts = stmts;
	}
	
	public Method (int line, Type type, String name, LinkedList<Formal> formals) {
		super(line);
		this.type = type;
		this.name = name;
		this.formals = formals;
	}
	/**
	 * Constructs a new method node. Used by subclasses.
	 * 
	 * @param type
	 *            Data type returned by method.
	 * @param name
	 *            Name of method.
	 * @param formals
	 *            List of method parameters.
	 * @param statements
	 *            List of method's statements.
	 */

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public LinkedList<Formal> getFormals() {
	  return formals;
	}

	public LinkedList<Stmt> getStmts() {
		return stmts;
	}
}


