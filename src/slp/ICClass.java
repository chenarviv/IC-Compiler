package slp;
import SymbolTable.SymbolTable;

import java.util.LinkedList;

import lir.PropagatingVisitorLIR;

/** An AST node for a list of expressions.
 */
public class ICClass extends ASTNode {
	
	public final String name;
	public LinkedList<Field> fields_List =new LinkedList<Field>();
	public LinkedList<Method> methods_List =new LinkedList<Method>();
	public String superClass = null;
	
	public ICClass (int line, String name){
		super(line);
		this.name=name;
	}
	
	public ICClass(int line, String name,LinkedList<ASTNode> fields_methods_List,String superClass) {
		this(line, name);
		this.superClass=superClass;
		for (ASTNode f_m: fields_methods_List){
			if(f_m instanceof Field)
				fields_List.add((Field) f_m);
			else
				methods_List.add((Method) f_m);
		}
	}
	
	public ICClass(int line, String name,LinkedList<Method> methods_List) {//for library build
		this(line,name);
		this.methods_List=methods_List;
	}
	

	/** Adds a statement to the tail of the list.
	 * 
	 * @param stmt A program statement.
	 */

	/** Accepts a visitor object as part of the visitor pattern.
	 * @param visitor A visitor.
	 */
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
	
	public boolean hasSuperClass()
	{
		return (null != superClass);
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getSuperClassName(){
		return this.superClass;
	}
	
	public LinkedList<Field> getFields(){
		return this.fields_List;
	}
	public LinkedList<Method> getMethods(){
		return this.methods_List;
	}
	
	@Override
	public Object accept(PropagatingVisitorLIR visitor, int regNum) {	
		return visitor.visit(this, regNum);
	}
}