package ast;

import compile.SymbolTable;

public class StmPrintln extends Stm {

    public final Exp exp;

    public StmPrintln(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        exp.compile(st);
        emit("sysc OUT_DEC");
        emit("push 10");
        emit("sysc 2");
    }


    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
