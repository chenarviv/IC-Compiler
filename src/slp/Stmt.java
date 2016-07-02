package slp;

/** The super class of all AST node for program statements.
 */
public abstract class Stmt extends ASTNode {
	/** Accepts a visitor object as part of the visitor pattern.
	 * @param visitor A visitor.
	 */
	
	public Stmt (int line)
	{
		super(line); 
	}
}