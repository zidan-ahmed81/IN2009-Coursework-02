package handbuilt;

import ast.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;
import java.io.IOException;

public class Ex_e {

    public static Program buildAST() {
        List<VarDecl> decls = new ArrayList<>();
        decls.add(new VarDecl(new TypeInt(), "x"));
        decls.add(new VarDecl(new TypeInt(), "zz"));

        List<Stm> stms = new ArrayList<>();
        stms.add(new StmAssign("x", new ExpMinus(new ExpVar("x"), new ExpInt(1))));
        stms.add(new StmAssign("zz", new ExpInt(55)));

        Exp switchExp = new ExpVar("x");

        List<StmSwitch.Case> cases = new ArrayList<>();
        cases.add(new StmSwitch.Case(7, new StmPrintln(new ExpInt(99))));
        cases.add(new StmSwitch.Case(-1, new StmPrintln(new ExpPlus(new ExpVar("x"), new ExpVar("zz")))));

        Stm defaultCase = new StmPrintln(new ExpVar("x"));

        stms.add(new StmSwitch(switchExp, defaultCase, cases));

        return new Program(decls, stms);
    }

    public static void main(String[] args) {
        Program program = buildAST();
        System.out.println(program);
        program.compile();
    }
}
