package handbuilt;

import ast.*;
import compile.SymbolTable;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Ex_b {

    public static Program buildAST() {
        List<VarDecl> decls = new ArrayList<>();
        decls.add(new VarDecl(new TypeInt(), "part"));
        decls.add(new VarDecl(new TypeInt(), "y"));

        List<Stm> stms = new ArrayList<>();
        stms.add(new StmAssign("part", new ExpPlus(new ExpInt(6), new ExpVar("y"))));
        stms.add(new StmAssign("y", new ExpMinus(new ExpVar("part"), new ExpInt(3))));
        stms.add(new StmPrintChar(new ExpTimes(new ExpPlus(new ExpVar("part"), new ExpVar("y")), new ExpInt(10))));

        return new Program(decls, stms);
    }

    public static void main(String[] args) throws IOException {
        Program program = buildAST();
        System.out.println(program);
        program.compile();
    }
}
