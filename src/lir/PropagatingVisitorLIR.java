package lir;

import slp.*;

public interface PropagatingVisitorLIR {

	public Object visit(Program program , int regNum);

	public Object visit(ICClass icClass, int regNum);

	public Object visit(Field field, int regNum);

	public Object visit(VirtualMethod method, int regNum);

	public Object visit(StaticMethod method, int regNum);

	public Object visit(LibraryMethod method, int regNum);

	public Object visit(Formal formal, int regNum);

	public Object visit(PrimitiveType type, int regNum);

	public Object visit(UserType type, int regNum);

	public Object visit(AssignStmt assignment, int regNum);

	public Object visit(CallStmt callStatement, int regNum);

	public Object visit(ReturnStmt returnStatement, int regNum);

	public Object visit(IFStmt ifStatement, int regNum);

	public Object visit(WhileStmt whileStatement, int regNum);

	public Object visit(BreakStmt breakStatement, int regNum);

	public Object visit(ContinueStmt continueStatement, int regNum);

	public Object visit(StmtsBlock statementsBlock, int regNum);

	public Object visit(LocalVariable localVariable, int regNum);

	public Object visit(LocationVar location, int regNum);

	public Object visit(LocationArray location, int regNum);

	public Object visit(StaticCall call, int regNum);

	public Object visit(VirtualCall call, int regNum);

	public Object visit(ThisExpr thisExpression, int regNum);

	public Object visit(NewClass newClass, int regNum);

	public Object visit(NewArrayExpr newArray, int regNum);

	public Object visit(Length length, int regNum);

	public Object visit(MathematicalBinaryExpr binaryOp, int regNum);

	public Object visit(LogicalBinaryExpr binaryOp, int regNum);

	public Object visit(MathematicalUnaryExpr unaryOp, int regNum);

	public Object visit(LogicalUnaryExpr unaryOp, int regNum);

	public Object visit(LiteralExpr literal, int regNum);

	public Object visit(StaticCallStmt call, int regNum);

	public Object visit(VirtualCallStmt call, int regNum);


}
