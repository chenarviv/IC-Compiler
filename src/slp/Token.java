package slp;

import java_cup.runtime.Symbol;

/** Adds line number and name information to scanner symbols.
 */
public class Token extends Symbol {
	private final String name;

	public Token(int line, String name, int id, Object value) {
		super(id,++line,0, value);
		this.name = name;
	}
	
	public Token(int line, String name, int id) {
		super(id,++line,0, null);
		this.name = name;
	}


	public String toString() {
		String val = value != null ? "(" + value + ")" : "";
		return name +  val;
	}
	
	public int getLine() {
		return super.left;
	}
	
}