package slp;

/** An AST node for program variables.
 */
public abstract class Location extends Expr {
	
	public Location(int line) {
		super(line);
	}
	
}