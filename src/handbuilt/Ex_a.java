package handbuilt;

import ast.*;
import compile.SymbolTable;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Ex_a {

    public static Program buildAST() {
        List<VarDecl> varDecls = new ArrayList<>();
        varDecls.add(new VarDecl(new TypeInt(), "x"));

        List<Stm> stms = new ArrayList<>();
        stms.add(new StmAssign("x", new ExpInt(3)));
        stms.add(new StmPrint(new ExpTimes(new ExpVar("x"), new ExpInt(9))));

        return new Program(varDecls, stms);
    }

    public static void main(String[] args) throws IOException {
        Program program = buildAST();
        System.out.println(program);
        program.compile();
    }
}
