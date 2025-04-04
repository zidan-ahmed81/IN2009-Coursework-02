package ast.util;

import ast.*;

public interface Visitor<T> {

    T visit(ExpTimes expTimes);

    T visit(ExpPlus expPlus);

    T visit(ExpMinus expMinus);

    T visit(ExpDiv expDiv);

    T visit(ExpLessThan expLessThan);

    T visit(ExpLessThanEqual expLessThanEqual);

    T visit(ExpEqual expEqual);

    T visit(ExpAnd expAnd);

    T visit(ExpOr expOr);

    T visit(ExpInt expInt);

    T visit(ExpNot expNot);

    T visit(ExpVar expVar);

    T visit(Program program);

    T visit(StmAssign stmAssign);

    T visit(StmBlock stmBlock);

    T visit(StmIf stmIf);

    T visit(StmNewline stmNewline);

    T visit(StmPrint stmPrint);

    T visit(StmPrintChar stmPrintChar);

    T visit(StmPrintln stmPrintln);

    T visit(StmSwitch stmSwitch);

    T visit(StmWhile stmWhile);

    T visit(Type type);

    T visit(TypeInt typeInt);

    T visit(VarDecl varDecl);
}
