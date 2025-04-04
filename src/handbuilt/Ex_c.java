package handbuilt;

import ast.*;
import java.util.ArrayList;
import java.util.List;

public class Ex_c {

    public static Program buildAST() {
        TypeInt intType = new TypeInt();

        VarDecl varCount = new VarDecl(intType, "count");
        List<VarDecl> varDecls = new ArrayList<>();
        varDecls.add(varCount);

        ExpVar varCountExp = new ExpVar("count");
        StmAssign assignCount = new StmAssign("count", new ExpInt(3));

        ExpInt zero = new ExpInt(0);
        ExpPlus countPlusOne = new ExpPlus(varCountExp, new ExpInt(1));
        ExpLessThan condition = new ExpLessThan(zero, countPlusOne);

        StmPrintChar printChar = new StmPrintChar(new ExpInt(32));
        StmPrint printCount = new StmPrint(varCountExp);
        ExpMinus countMinusOne = new ExpMinus(varCountExp, new ExpInt(1));
        StmAssign decrementCount = new StmAssign("count", countMinusOne);

        List<Stm> bodyStatements = new ArrayList<>();
        bodyStatements.add(printChar);
        bodyStatements.add(printCount);
        bodyStatements.add(decrementCount);

        StmBlock whileBody = new StmBlock(bodyStatements);
        StmWhile whileStmt = new StmWhile(condition, whileBody);

        List<Stm> statements = new ArrayList<>();
        statements.add(assignCount);
        statements.add(whileStmt);

        return new Program(varDecls, statements);
    }

    public static void main(String[] args) {
        Program program = buildAST();
        System.out.println(program);
        program.compile();
    }
}
