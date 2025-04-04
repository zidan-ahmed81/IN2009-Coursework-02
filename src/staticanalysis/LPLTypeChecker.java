package staticanalysis;

import ast.*;
import ast.util.VisitorAdapter;
import compile.StaticAnalysisException;
import compile.SymbolTable;
import parse.LPLParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LPLTypeChecker extends VisitorAdapter<Type> {

    private SymbolTable st;

    public LPLTypeChecker(SymbolTable st) {
        this.st = st;
    }

    public Type type(Exp exp) {
        return exp.accept(this);
    }

    public void typeCheck(Stm stm) {
        stm.accept(this);
    }

    public void typeCheck(Program program) {
        program.accept(this);
    }

    public static void typeCheck(String filePath) throws IOException {
        LPLParser parser = new LPLParser();
        Program program = parser.parse(filePath);
        LPLTypeChecker typeChecker = new LPLTypeChecker(new SymbolTable(program));
        typeChecker.typeCheck(program);
    }

    public static void main(String[] args) throws IOException {
        typeCheck(args[0]);
        System.out.println("Type-check succeeded.");
    }
}
