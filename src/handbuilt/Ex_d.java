package handbuilt;

import ast.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;
import java.io.IOException;

public class Ex_d {

    public static Program buildAST() {
        List<VarDecl> decls = new ArrayList<>();
        decls.add(new VarDecl(new TypeInt(), "x"));

        List<Stm> stms = new ArrayList<>();
        stms.add(new StmAssign("x", new ExpInt(20)));

        Exp condition1 = new ExpLessThan(new ExpVar("x"), new ExpInt(20));

        List<Stm> thenStms1 = new ArrayList<>();
        thenStms1.add(new StmAssign("x", new ExpMinus(new ExpVar("x"), new ExpInt(7))));
        Stm thenBlock1 = new StmBlock(thenStms1);

        Exp condition2 = new ExpLessThan(new ExpVar("x"), new ExpInt(30));
        Stm thenBranch2 = new StmPrintln(new ExpInt(77));
        Stm elseBranch2 = new StmPrintln(new ExpInt(88));
        Stm nestedIf = new StmIf(condition2, thenBranch2, elseBranch2);

        List<Stm> elseStms1 = new ArrayList<>();
        elseStms1.add(nestedIf);
        Stm elseBlock1 = new StmBlock(elseStms1);

        Stm ifStatement = new StmIf(condition1, thenBlock1, elseBlock1);
        stms.add(ifStatement);

        stms.add(new StmPrintln(new ExpVar("x")));

        return new Program(decls, stms);
    }

    public static void main(String[] args) throws IOException {
        Program program = buildAST();
        System.out.println(program);
        program.compile();
        }
}
