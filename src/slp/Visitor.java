package slp;

/** An interface for AST visitors.
 */
public interface Visitor {
	public Object visit(AssignStmt stmt);
	public Object visit(ReturnStmt stmt);
	public Object visit(BreakStmt stmt);
	public Object visit(ContinueStmt stmt);
	public Object visit(ThisExpr expr);
	public Object visit(IFStmt stmt);
	public Object visit(WhileStmt stmt);
	public Object visit(CallStmt call);
	public Object visit(StaticCall call);
	public Object visit(VirtualCall call);
	public Object visit(StaticCallStmt call);
	public Object visit(VirtualCallStmt call);
	public Object visit(LocationVar loc);
	public Object visit(LocationArray loc);
	public Object visit(PrimitiveType type);
	public Object visit(UserType type);
	public Object visit(StaticMethod method);
	public Object visit(Field field);
	public Object visit(Formal formal);
	public Object visit(ICClass ic);
	public Object visit(Program prg);
	public Object visit(VirtualMethod v);
	public Object visit(StmtsBlock s);
	public Object visit(NewClass c);
	public Object visit(NewArrayExpr e);
	public Object visit(Length e);
	public Object visit(LogicalBinaryExpr e);
	public Object visit(LogicalUnaryExpr e); 
	public Object visit(MathematicalBinaryExpr e);
	public Object visit(MathematicalUnaryExpr e);
	public Object visit(LocalVariable var);
	public Object visit(LiteralExpr e);
	public Object visit(LibraryMethod m);

}