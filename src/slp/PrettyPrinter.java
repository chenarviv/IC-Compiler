package slp;
import SymbolTable.SymbolTable;

import java.util.LinkedList;

/**
 * Pretty-prints an SLP AST.
 */
public class PrettyPrinter implements Visitor {
	protected final ASTNode root;

	private int depth = 0;
	private String FilePath;

	private Object indent(ASTNode node) {
		System.out.print("\n");
		for (int i = 0; i < depth; ++i)
			System.out.print(" ");
		if (node != null)
			System.out.print(node.getLine() + ": ");
		return null;
	}

	/**
	 * Constructs a printin visitor from an AST.
	 * 
	 * @param root
	 *            The root of the AST.
	 */
	public PrettyPrinter(ASTNode root, String FilePath) {
		this.root = root;
		this.FilePath = FilePath;
	}

	/**
	 * Prints the AST with the given root.
	 */
	public Object print() {
		root.accept(this);
		return null;
	}

	public Object visit(Program prg) {
		System.out.println();
		System.out.print("Abstract Syntax Tree: " + FilePath + "\n");

		for (ICClass icClass : prg.ic_classes) {

			if (icClass.name.compareTo("Library") != 0) {
				visit(icClass);
			}
		}
		return null;
	}

	public Object visit(ICClass icClass) {
		if (icClass.name.compareTo("Library") == 0) {
			indent(icClass);
		}

		indent(icClass);

		System.out.print("Declaration of class: " + icClass.name);
		if (icClass.hasSuperClass())
			System.out.print(", extends class " + icClass.superClass);
		depth += 2;
		for (Field n : icClass.getFields()) {
			n.accept(this);
		}
		for (Method n : icClass.getMethods()) {
			n.accept(this);
		}
		depth -= 2;
		return null;
	}

	public Object visit(Field field) {
		indent(field);
		System.out.print("Declaration of field: " + field.getName());
		++depth;
		field.getType().accept(this);
		--depth;
		return null;
	}

	public Object visit(StaticMethod method) {
		indent(method);
		System.out.print("Declaration of static method: " + method.getName());
		depth += 2;
		method.getType().accept(this);
		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Stmt statement : method.stmts)
			statement.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(VirtualMethod method) {
		indent(method);
		System.out.print("Declaration of virtual method: " + method.getName());
		depth += 2;
		method.getType().accept(this);
		for (Formal formal : method.getFormals())
			formal.accept(this);
		for (Stmt statement : method.stmts)
			statement.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(Formal f) {
		indent(f);
		System.out.print("Parameter: " + f.name);
		++depth;
		f.type.accept(this);
		--depth;
		return null;
	}

	public Object visit(PrimitiveType type) {
		indent(type);
		System.out.print("Primitive data type: ");
		if (type.getDimension() > 0)
			System.out.print(type.getDimension() + "-dimensional array of ");
		System.out.print(type.getName());
		return null;
	}

	public Object visit(UserType type) {
		indent(type);
		System.out.print("User-defined data type: ");
		if (type.getDimension() > 0)
			System.out.print(type.getDimension() + "-dimensional array of ");
		System.out.print(type.getName());
		return null;
	}

	public Object visit(StaticCall call) {
		indent(call);
		System.out.print("Call to static method: " + call.id + ", in class "
				+ call.class_id);
		depth += 2;
		for (Expr argument : call.expList)
			argument.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(VirtualCall call) {
		indent(call);
		System.out.print("Call to virtual method: ");
		if (call.hasLocationExpr()) {
			call.location_expr.accept(this);
			System.out.print("." + call.id);
		} else {
			System.out.print(call.id);
		}
		depth += 2;
		for (Expr argument : call.expList)
			argument.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(CallStmt call) {
		throw new UnsupportedOperationException(
				"Unexpected visit of CallStmt abstract class");
	}

	public Object visit(StaticCallStmt call) {
		indent(call);
		System.out.print("Method call statement");
		++depth;
		call.call.accept(this);
		--depth;
		return null;
	}

	public Object visit(VirtualCallStmt call) {
		indent(call);
		System.out.print("Method call statement");
		++depth;
		call.call.accept(this);
		--depth;
		return null;
	}

	public Object visit(StmtsBlock sl) {
		indent(sl);
		System.out.print("Block of statements");
		depth++;
		for (Stmt s : sl.stmts_list) {
			s.accept(this);
		}
		depth--;
		return null;
	}

	public Object visit(ReturnStmt stmt) {
		indent(stmt);
		System.out.print("Return statement");
		if (stmt.hasReturnExpr()) {
			++depth;
			stmt.expr.accept(this);
			--depth;
		}
		return null;
	}
	
	public Object visit(ThisExpr expr) {
		indent(expr);
		System.out.print("this");
		++depth;	
		return null;
	}

	public Object visit(Length expr) {
		indent(expr);
		System.out.print(expr.toString() + ".length");
		++depth;
		return null;
	}

	public Object visit(AssignStmt stmt) {
		indent(stmt);
		System.out.print("Assignment statement: ");
		depth += 2;
		if (stmt.rhs != null) {
			stmt.rhs.accept(this);
		}
		stmt.loc.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(IFStmt stmt) {
		indent(stmt);
		System.out.print("If statement");
		if (stmt.hasElse())
			System.out.print(", with Else operation");
		depth += 2;
		stmt.expr.accept(this);
		stmt.stmt.accept(this);
		if (stmt.hasElse())
			stmt.elseStmt.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(WhileStmt stmt) {
		indent(stmt);
		System.out.print("While statement ");
		depth += 2;
		stmt.expr.accept(this);
		stmt.stmt.accept(this);
		depth -= 2;
		return null;
	}
	
	public Object visit(LocationVar loc) {
		indent(loc);
		System.out.print("Reference to variable: " + loc.toString());
		++depth;
		if (loc.isPointer()){
			loc.exprLocation.accept(this);
		}
		--depth;
		return null;
	}

	public Object visit(LocationArray loc) {
		indent(loc);
		System.out.print("Reference to array: " + loc.toString());
		++depth;
		loc.ArrayExpr.accept(this);
		loc.IndexExpr.accept(this);
		--depth;
		return null;
	}

	public Object visit(LogicalBinaryExpr binaryOp) {
		indent(binaryOp);
		System.out.print("Logical binary operation: " + binaryOp.op);
		depth += 2;
		binaryOp.lhs.accept(this);
		binaryOp.rhs.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(MathematicalUnaryExpr unaryOp) {
		indent(unaryOp);
		System.out.print("Mathematical unary operation: " + unaryOp.op);
		++depth;
		unaryOp.operand.accept(this);
		--depth;
		return null;
	}

	public Object visit(LogicalUnaryExpr unaryOp) {
		indent(unaryOp);
		System.out.print("Logical unary operation: " + unaryOp.op);
		++depth;
		unaryOp.operand.accept(this);
		--depth;
		return null;
	}

	public Object visit(MathematicalBinaryExpr binaryOp) {
		indent(binaryOp);
		System.out.print("Mathematical binary operation: " + binaryOp.op);
		depth += 2;
		binaryOp.lhs.accept(this);
		binaryOp.rhs.accept(this);
		depth -= 2;
		return null;
	}

	public Object visit(NewClass c) {

		System.out.print("new " + c.class_id + "()");
		return null;

	}

	public Object visit(NewArrayExpr e) {

		//System.out.print("new " + e.type.getName() + "[" + e.expr + "]");
		//e.expr.accept(this);
		return null;

	}

	public Object visit(BreakStmt stmt) {
		indent(stmt);
		System.out.print("Break statement");
		return null;
	}

	public Object visit(ContinueStmt stmt) {
		indent(stmt);
		System.out.print("Continue statement");
		return null;
	}

	public Object visit(LocalVariable var) {
		indent(var);
		System.out.print("Declaration of local variable: " + var.getName());
		if (var.hasInitValue()) {
			System.out.print(", with initial value");
			++depth;
		}
		++depth;
		var.type.accept(this);
		if (var.hasInitValue()) {
			var.getInitValue().accept(this);
			--depth;
		}
		--depth;
		return null;
	}


	public Object visit(LiteralExpr literal) {
		indent(literal);
		StringBuffer output = new StringBuffer();
		output.append(literal.getType().getDescription() + ": " + literal.getType().toFormattedString(literal.getValue()));
		System.out.print(output.toString());
		return null;
		
	}

	public Object visit(LibraryMethod method) {
		StringBuffer output = new StringBuffer();
		indent(method);
		output.append("Declaration of library method: " + method.getName() + "    (Enclosing Scope: "+ method.getEnclosingScope().getTableName() + ", " + method.getEnclosingScope().getTableCategory().getCategory() +")");
		depth += 2;
		output.append(method.getType().accept(this));
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this));
		depth -= 2;
		System.out.print(output.toString());
		return null;
	}


}