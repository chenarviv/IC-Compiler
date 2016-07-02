package slp;

import SymbolTable.SymbolTable;


/** An interface for a propagating AST visitor.
 * The visitor passes down objects of type <code>DownType</code>
 * and propagates up objects of type <code>Object</code>.
 */
public interface PropagatingVisitor{
	public Object visit(ReturnStmt stmt, SymbolTable table);
	public Object visit(AssignStmt stmt, SymbolTable table);
	public Object visit(BreakStmt stmt, SymbolTable table);
	public Object visit(ContinueStmt stmt, SymbolTable table);
	public Object visit(ThisExpr expr, SymbolTable table);
	public Object visit(IFStmt stmt, SymbolTable table);
	public Object visit(WhileStmt stmt, SymbolTable table);
	public Object visit(CallStmt call, SymbolTable table);
	public Object visit(StaticCall call, SymbolTable table);
	public Object visit(VirtualCall call, SymbolTable table);
	public Object visit(StaticCallStmt call, SymbolTable table);
	public Object visit(VirtualCallStmt call, SymbolTable table);
	public Object visit(LocationVar loc,SymbolTable table);
	public Object visit(LocationArray loc,SymbolTable table);
	public Object visit(PrimitiveType type, SymbolTable table);
	public Object visit(UserType type, SymbolTable table);
	public Object visit(StaticMethod method, SymbolTable table);
	public Object visit(VirtualMethod method, SymbolTable table);
	public Object visit(Field field, SymbolTable table);
	public Object visit(Formal formal, SymbolTable table);
	public Object visit(ICClass ic, SymbolTable table);
	public Object visit(Program prg, SymbolTable table);
	public Object visit(StmtsBlock s, SymbolTable table);
	public Object visit(NewClass c, SymbolTable table);
	public Object visit(NewArrayExpr e, SymbolTable table);
	public Object visit(LogicalBinaryExpr e, SymbolTable table);
	public Object visit(LogicalUnaryExpr e, SymbolTable table); 
	public Object visit(MathematicalBinaryExpr e, SymbolTable table);
	public Object visit(MathematicalUnaryExpr e, SymbolTable table);
	public Object visit(LocalVariable var, SymbolTable table);
	public Object visit(Length e, SymbolTable table);
	public Object visit(LiteralExpr e,SymbolTable table);
	public Object visit(LibraryMethod m,SymbolTable table);	
}